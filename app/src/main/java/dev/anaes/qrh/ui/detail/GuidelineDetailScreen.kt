package dev.anaes.qrh.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import dev.anaes.qrh.R
import dev.anaes.qrh.data.UserPreferences
import dev.anaes.qrh.model.Guideline
import dev.anaes.qrh.model.GuidelineCopyright
import dev.anaes.qrh.ui.components.BreadcrumbBar
import dev.anaes.qrh.ui.components.BreadcrumbEntry
import dev.anaes.qrh.ui.components.openExternalLink
import dev.anaes.qrh.ui.components.rememberExternalLinkHandler
import dev.anaes.qrh.ui.theme.qrhTopAppBarColors

// Header text sizes. Line heights are set explicitly so the expanded bar height can be computed
// from them (otherwise they inherit the app bar's headline line height).
private const val CODE_LINE_HEIGHT_RATIO = 1.15f
private val CollapsedCodeFontSize = 20.sp
private val TitleFontSize = 16.sp
private val TitleLineHeight = 20.sp
private val CollapsedTitleFontSize = 14.sp
private val CollapsedTitleLineHeight = 18.sp
private val VersionFontSize = 12.sp
private val VersionLineHeight = 16.sp

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidelineDetailScreen(
    guideline: Guideline,
    copyright: GuidelineCopyright?,
    breadcrumbs: List<BreadcrumbEntry>,
    preferences: UserPreferences,
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

    // Collapsible box state — saveable, so boxes the user opened stay open after following a
    // guideline link and coming back, or rotating
    val collapsedState = rememberSaveable(guideline.code, saver = CollapsedStateSaver) {
        mutableStateMapOf()
    }

    // Special case: guideline 0-3, first type-5 box starts expanded
    val firstType5Index = remember(guideline.code) {
        if (guideline.code == "0-3") {
            guideline.content.indexOfFirst { it.type == 5 }
        } else -1
    }

    val codeFontSize = when (guideline.code) {
        "3-2" -> 48.sp
        "3-3", "3-7", "3-11" -> 72.sp
        else -> 34.sp
    }

    // Tall enough for the code line, two lines of title and the version line, so large codes
    // and long titles aren't clipped (the Material default of 152dp only fits the 34sp code)
    val expandedHeight = with(LocalDensity.current) {
        CollapsedBarHeight +
            (codeFontSize * CODE_LINE_HEIGHT_RATIO).toDp() +
            (TitleLineHeight * 2).toDp() +
            VersionLineHeight.toDp() +
            ExpandedTitleBottomSpace
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    DetailTitle(
                        guideline = guideline,
                        codeFontSize = codeFontSize,
                        collapsedFraction = scrollBehavior.state.collapsedFraction,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { context.openExternalLink(guideline.url) }) {
                        Icon(
                            Icons.Filled.Download,
                            contentDescription = stringResource(R.string.download_pdf),
                        )
                    }
                    IconButton(onClick = onSwipeView) {
                        Icon(
                            Icons.Filled.ViewCarousel,
                            contentDescription = stringResource(R.string.title_cards),
                        )
                    }
                },
                expandedHeight = expandedHeight,
                colors = qrhTopAppBarColors(),
                scrollBehavior = scrollBehavior,
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                itemsIndexed(
                    items = guideline.content,
                    key = { index, _ -> "${guideline.code}-$index" }
                ) { index, item ->
                    val isCollapsed = when {
                        expandingDisabled -> false
                        index == firstType5Index -> collapsedState[index] ?: false
                        item.type in 5..9 -> collapsedState[index] ?: true
                        else -> false
                    }

                    ContentItemView(
                        item = item,
                        index = index,
                        isCollapsed = isCollapsed,
                        expandingDisabled = expandingDisabled,
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

                item(key = "${guideline.code}-footer") {
                    GuidelineFooter(version = guideline.version, copyright = copyright)
                }
            }
        }
    }
}

/**
 * Code, title and version for the large app bar.
 *
 * Material 3 draws this same content in both the expanded (large) row and the collapsed (64dp)
 * row, cross-fading between them, so the text shrinks as the bar collapses — otherwise the
 * collapsed row would show the full-size code clipped.
 */
@Composable
private fun DetailTitle(
    guideline: Guideline,
    codeFontSize: TextUnit,
    collapsedFraction: Float,
) {
    val codeSize = lerp(codeFontSize, CollapsedCodeFontSize, collapsedFraction)
    val nearlyCollapsed = collapsedFraction > 0.5f

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
            fontSize = lerp(TitleFontSize, CollapsedTitleFontSize, collapsedFraction),
            lineHeight = lerp(TitleLineHeight, CollapsedTitleLineHeight, collapsedFraction),
            maxLines = if (nearlyCollapsed) 1 else 2,
            overflow = TextOverflow.Ellipsis,
        )
        // Fades out while collapsing; dropped once fully collapsed to keep the 64dp row roomy
        if (collapsedFraction < 1f) {
            Text(
                text = stringResource(R.string.guideline_version, guideline.version),
                fontSize = VersionFontSize,
                lineHeight = VersionLineHeight,
                color = LocalContentColor.current.copy(alpha = 0.7f),
                modifier = Modifier.alpha(1f - collapsedFraction),
            )
        }
    }
}
