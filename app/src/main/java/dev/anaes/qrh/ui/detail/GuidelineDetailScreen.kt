package dev.anaes.qrh.ui.detail

import android.text.Html
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anaes.qrh.R
import dev.anaes.qrh.data.UserPreferences
import dev.anaes.qrh.model.ContentBlock
import dev.anaes.qrh.model.ContentItem
import dev.anaes.qrh.model.ContentType
import dev.anaes.qrh.model.Guideline
import dev.anaes.qrh.model.GuidelineCopyright
import dev.anaes.qrh.model.blockIndexOf
import dev.anaes.qrh.model.branchCondition
import dev.anaes.qrh.model.contentBlocks
import dev.anaes.qrh.search.SearchTerm
import dev.anaes.qrh.search.SearchText
import dev.anaes.qrh.ui.components.BreadcrumbBar
import dev.anaes.qrh.ui.components.BreadcrumbEntry
import dev.anaes.qrh.ui.components.LocalSearchHighlight
import dev.anaes.qrh.ui.components.openExternalLink
import dev.anaes.qrh.ui.components.rememberExternalLinkHandler
import dev.anaes.qrh.ui.theme.qrhTopAppBarColors

// Header text sizes. Line heights are set explicitly so the expanded bar height can be computed
// from them (otherwise they inherit the app bar's headline line height).
private const val CODE_LINE_HEIGHT_RATIO = 1.15f
private val CodeFontSize = 34.sp
private val CollapsedCodeFontSize = 20.sp
private val TitleFontSize = 16.sp
private val TitleLineHeight = 20.sp
private val CollapsedTitleFontSize = 14.sp
private val CollapsedTitleLineHeight = 18.sp

/** Content types shown in the right-hand column of the two-column layout: boxes and images. */
private val SIDE_COLUMN_TYPES = 5..10

/** Share of the two-column layout's width taken by the steps, as on iPad. */
private const val MAIN_COLUMN_WEIGHT = 0.6f

/** Height of the collapsed (single-row) app bar, matching Material 3's default. */
private val CollapsedBarHeight = 64.dp

/** Room below the expanded title: Material 3's large-title bottom padding plus breathing space. */
private val ExpandedTitleBottomSpace = 36.dp

/** Collapsed/expanded state of each box (by content index), saved as [index, 0|1] pairs. */
private val CollapsedStateSaver = listSaver<SnapshotStateMap<Int, Boolean>, Int>(
    save = { map -> map.flatMap { (index, collapsed) -> listOf(index, if (collapsed) 1 else 0) } },
    restore = { saved ->
        mutableStateMapOf<Int, Boolean>().apply {
            saved.chunked(2).forEach { (index, collapsed) -> put(index, collapsed == 1) }
        }
    },
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GuidelineDetailScreen(
    guideline: Guideline,
    focusCard: Int,
    highlightQuery: String,
    copyright: GuidelineCopyright?,
    breadcrumbs: List<BreadcrumbEntry>,
    preferences: UserPreferences,
    /** Tablets: steps on the left, boxes and images in a column on the right (as on iPad). */
    twoColumns: Boolean,
    /** A single-row top bar that stays as it is, instead of a large one that collapses on scrolling. */
    compactTopBar: Boolean,
    showBack: Boolean,
    onNavigateBack: () -> Unit,
    onHomeClick: () -> Unit,
    onBreadcrumbClick: (index: Int) -> Unit,
    onGuidelineLink: (code: String) -> Unit,
    onSwipeView: () -> Unit,
) {
    val context = LocalContext.current
    val onExternalLink = rememberExternalLinkHandler()
    val expandingDisabled by preferences.expandingDisabled.collectAsState(initial = false)
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // Content grouped for layout, with any branches as forks: all of it in one column, or the
    // steps for the main column of two
    val allBlocks = remember(guideline.code) { contentBlocks(guideline.content) }
    val mainBlocks = remember(guideline.code) {
        contentBlocks(guideline.content) { guideline.content[it].type !in SIDE_COLUMN_TYPES }
    }
    // Boxes and images for the side column. A box repeated in each branch (3-10's Box A) shows
    // once; others show wherever they are, even if repeated (3-8's EMERGENCY HELP)
    val sideIndices = remember(guideline.code) {
        guideline.content.indices
            .filter { guideline.content[it].type in SIDE_COLUMN_TYPES }
            .distinctBy { sideColumnKey(guideline.content, it) }
    }
    // A guideline with no boxes or images (3-7) has the full width for its steps
    val useTwoColumns = twoColumns && sideIndices.isNotEmpty()
    // Boxes are always open beside the steps, as on iPad
    val boxesAlwaysOpen = expandingDisabled || useTwoColumns

    // Collapsible box state — saveable, so boxes the user opened stay open after following a
    // guideline link and coming back, or rotating
    val collapsedState = rememberSaveable(guideline.code, saver = CollapsedStateSaver) {
        mutableStateMapOf()
    }

    // Opened from a search match: the matching item to scroll to, once, and the words to highlight
    val listState = rememberLazyListState()
    val sideListState = rememberLazyListState()
    val highlightTerms = remember(highlightQuery) { SearchTerm.terms(highlightQuery) }
    var focusApplied by rememberSaveable(guideline.code) { mutableStateOf(false) }
    // The matching item, brought into view when it's inside a fork (which scrolls as one)
    val focusBringIntoView = remember { BringIntoViewRequester() }
    LaunchedEffect(guideline.code) {
        if (focusApplied || focusCard !in guideline.content.indices) return@LaunchedEffect
        focusApplied = true
        // Open the boxes the words are in
        guideline.content.forEachIndexed { index, item ->
            if (item.type in 5..9 && highlightTerms.any(searchText(item)::contains)) {
                collapsedState[index] = false
            }
        }
        // Collapse the large app bar, once its first layout has set how far it can collapse (the
        // compact one never sets it)
        withFrameNanos { }
        val collapsedOffset = scrollBehavior.state.heightOffsetLimit
        if (collapsedOffset > -Float.MAX_VALUE) scrollBehavior.state.heightOffset = collapsedOffset
        val focusItem = guideline.content[focusCard]
        val focusKey = sideColumnKey(guideline.content, focusCard)
        when {
            !useTwoColumns -> listState.animateScrollToItem(allBlocks.blockIndexOf(focusCard).coerceAtLeast(0))
            focusItem.type in SIDE_COLUMN_TYPES -> sideListState.animateScrollToItem(
                sideIndices.indexOfFirst { sideColumnKey(guideline.content, it) == focusKey }.coerceAtLeast(0)
            )
            else -> listState.animateScrollToItem(mainBlocks.blockIndexOf(focusCard).coerceAtLeast(0))
        }
        // A fork can be taller than the screen: show the matching item within it
        if (focusItem.branch.isNotEmpty()) focusBringIntoView.bringIntoView()
    }

    // Special case: guideline 0-3, first type-5 box starts expanded
    val firstType5Index = remember(guideline.code) {
        if (guideline.code == "0-3") {
            guideline.content.indexOfFirst { it.type == 5 }
        } else -1
    }

    // Tall enough for the code line and two lines of title, so long titles
    // aren't clipped (the Material default of 152dp only fits the code)
    val expandedHeight = with(LocalDensity.current) {
        CollapsedBarHeight +
            (CodeFontSize * CODE_LINE_HEIGHT_RATIO).toDp() +
            (TitleLineHeight * 2).toDp() +
            ExpandedTitleBottomSpace
    }

    Scaffold(
        topBar = {
            val navigationIcon: @Composable () -> Unit = {
                if (showBack) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                }
            }
            val actions: @Composable RowScope.() -> Unit = {
                IconButton(onClick = { context.openExternalLink(guideline.url) }) {
                    Icon(
                        painterResource(R.drawable.ic_download),
                        contentDescription = stringResource(R.string.download_pdf),
                    )
                }
                IconButton(onClick = onSwipeView) {
                    Icon(
                        painterResource(R.drawable.ic_view_carousel),
                        contentDescription = stringResource(R.string.title_cards),
                    )
                }
            }
            if (compactTopBar) {
                TopAppBar(
                    title = { DetailTitle(guideline = guideline) },
                    navigationIcon = navigationIcon,
                    actions = actions,
                    colors = qrhTopAppBarColors(),
                )
            } else {
                LargeTopAppBar(
                    title = { DetailTitle(guideline = guideline) },
                    navigationIcon = navigationIcon,
                    actions = actions,
                    expandedHeight = expandedHeight,
                    colors = qrhTopAppBarColors(),
                    scrollBehavior = scrollBehavior,
                )
            }
        },
        modifier = if (compactTopBar) Modifier else Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            BreadcrumbBar(
                entries = breadcrumbs,
                onHomeClick = onHomeClick,
                onEntryClick = onBreadcrumbClick,
            )

            // One content item
            val itemView: @Composable (index: Int, inSideColumn: Boolean) -> Unit = { index, inSideColumn ->
                val item = guideline.content[index]
                val isCollapsed = when {
                    boxesAlwaysOpen -> false
                    index == firstType5Index -> collapsedState[index] ?: false
                    item.type in 5..9 -> collapsedState[index] ?: true
                    else -> false
                }

                // Search words are highlighted in the guideline's own content, not the START
                // banner (or the footer, which is outside this), as on iOS
                val highlight = if (item.type == ContentType.BANNER) emptyList() else highlightTerms
                CompositionLocalProvider(LocalSearchHighlight provides highlight) {
                    ContentItemView(
                        modifier = if (index == focusCard) {
                            Modifier.bringIntoViewRequester(focusBringIntoView)
                        } else Modifier,
                        item = item,
                        index = index,
                        isCollapsed = isCollapsed,
                        expandingDisabled = boxesAlwaysOpen,
                        indentBoxes = !inSideColumn,
                        branchCondition = guideline.content.branchCondition(item),
                        onToggleCollapse = { idx ->
                            val current = when {
                                idx == firstType5Index -> collapsedState[idx] ?: false
                                else -> collapsedState[idx] ?: true
                            }
                            collapsedState[idx] = !current
                        },
                        onGuidelineLink = onGuidelineLink,
                        onExternalLink = onExternalLink,
                    )
                }
            }
            // Blocks of content: single items, and forks with their branches together
            val blockItems: LazyListScope.(blocks: List<ContentBlock>) -> Unit = { blocks ->
                items(
                    items = blocks,
                    key = { block ->
                        when (block) {
                            is ContentBlock.Single -> "${guideline.code}-${block.index}"
                            is ContentBlock.Fork -> "${guideline.code}-fork-${block.indices.first()}"
                        }
                    }
                ) { block ->
                    when (block) {
                        is ContentBlock.Single -> itemView(block.index, false)
                        // Branch conditions are highlighted too (the items do their own)
                        is ContentBlock.Fork -> CompositionLocalProvider(LocalSearchHighlight provides highlightTerms) {
                            BranchFork(
                                fork = block,
                                headerOf = { index -> guideline.content[index].body },
                                itemView = { index -> itemView(index, false) },
                                headerModifier = { index ->
                                    if (index == focusCard) {
                                        Modifier.bringIntoViewRequester(focusBringIntoView)
                                    } else Modifier
                                },
                            )
                        }
                    }
                }
            }
            val footer: LazyListScope.() -> Unit = {
                item(key = "${guideline.code}-footer") {
                    GuidelineFooter(
                        version = guideline.version,
                        copyright = copyright,
                        onExternalLink = onExternalLink,
                    )
                }
            }

            if (useTwoColumns) {
                Row(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(MAIN_COLUMN_WEIGHT)
                            .fillMaxHeight(),
                    ) {
                        blockItems(mainBlocks)
                        footer()
                    }
                    LazyColumn(
                        state = sideListState,
                        modifier = Modifier
                            .weight(1f - MAIN_COLUMN_WEIGHT)
                            .fillMaxHeight(),
                    ) {
                        items(
                            items = sideIndices,
                            key = { index -> "${guideline.code}-$index" }
                        ) { index ->
                            itemView(index, true)
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    blockItems(allBlocks)
                    footer()
                }
            }
        }
    }
}

/**
 * Code and title for the large app bar.
 *
 * Material 3 draws this same content in both the expanded (large) row and the collapsed (64dp)
 * row, cross-fading between them, so each row gets its own fixed size — otherwise the collapsed
 * row would show the full-size code clipped. The row is told apart by the text style Material
 * provides it (the large title style is bigger than the small one). Fixed sizes mean nothing here
 * is recomposed or laid out again while the bar collapses; Material fades the rows when drawing.
 */
@Composable
private fun DetailTitle(guideline: Guideline) {
    val expandedRow = LocalTextStyle.current.fontSize > MaterialTheme.typography.titleLarge.fontSize
    val codeSize = if (expandedRow) CodeFontSize else CollapsedCodeFontSize

    Column {
        Text(
            text = guideline.code,
            fontSize = codeSize,
            lineHeight = codeSize * CODE_LINE_HEIGHT_RATIO,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
        Text(
            text = guideline.title,
            fontSize = if (expandedRow) TitleFontSize else CollapsedTitleFontSize,
            lineHeight = if (expandedRow) TitleLineHeight else CollapsedTitleLineHeight,
            maxLines = if (expandedRow) 2 else 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** An item's head and body as plain text, for matching search words. */
private fun searchText(item: ContentItem): SearchText =
    SearchText(Html.fromHtml("${item.head} ${item.body}", Html.FROM_HTML_MODE_LEGACY).toString())

/**
 * What identifies a side-column item: a branched item by its content without the branch, so a box
 * repeated in each branch is shown once; anything else by its index.
 */
private fun sideColumnKey(content: List<ContentItem>, index: Int): Any {
    val item = content[index]
    return if (item.branch.isEmpty()) index else item.copy(branch = "")
}
