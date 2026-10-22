package dev.anaes.qrh.ui.list

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anaes.qrh.QrhViewModel
import dev.anaes.qrh.R
import dev.anaes.qrh.model.Guideline
import dev.anaes.qrh.search.SearchResult
import dev.anaes.qrh.ui.theme.qrhTopAppBarColors
import kotlinx.coroutines.flow.filter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidelineListScreen(
    viewModel: QrhViewModel,
    onGuidelineClick: (Guideline, SearchResult?) -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** Tablets: the list is beside the open guidelines, the first of which is marked as selected. */
    selectedCode: String? = null,
    /** Whether back clears the search (on tablets, not while guidelines are open beside the list). */
    clearSearchOnBack: Boolean = true,
    /** Hoisted on tablets, so the scroll position survives the list pane being hidden. */
    listState: LazyListState = rememberLazyListState(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val filtered = viewModel.filteredGuidelines
    val searchResults = viewModel.searchResults
    val hasQuery = viewModel.searchQuery.isNotBlank()
    val snackLaunchText = stringResource(R.string.snackLaunch)

    // Back clears the search before it leaves the app
    val focusManager = LocalFocusManager.current
    BackHandler(enabled = clearSearchOnBack && viewModel.searchQuery.isNotEmpty()) {
        viewModel.onSearchQueryChange("")
        focusManager.clearFocus()
    }

    // Startup snackbar
    LaunchedEffect(viewModel.isStartup) {
        if (viewModel.isStartup) {
            viewModel.onStartupComplete()
            snackbarHostState.showSnackbar(
                message = snackLaunchText,
                duration = SnackbarDuration.Long,
            )
        }
    }

    // Dismiss snackbar on scroll
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemScrollOffset }
            .filter { it > 10 }
            .collect { snackbarHostState.currentSnackbarData?.dismiss() }
    }

    // Scroll to top when search results change
    LaunchedEffect(searchResults) {
        if (hasQuery) {
            listState.scrollToItem(0)
        }
    }

    // Build a lookup map for snippets when searching
    val snippetMap = remember(searchResults) {
        searchResults.associateBy { it.guideline.code }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = qrhTopAppBarColors(),
                actions = {
                    IconButton(onClick = onAboutClick) {
                        Icon(painterResource(R.drawable.ic_info), contentDescription = stringResource(R.string.title_about))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        // The search field and rows reach the screen edges, under a display cutout (as the app bar
        // does), with their content inset clear of it
        val layoutDirection = LocalLayoutDirection.current
        val contentInsets = PaddingValues(
            start = paddingValues.calculateStartPadding(layoutDirection),
            end = paddingValues.calculateEndPadding(layoutDirection),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding(), bottom = paddingValues.calculateBottomPadding())
        ) {
            TextField(
                value = viewModel.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = {
                    Icon(
                        painterResource(R.drawable.ic_search),
                        contentDescription = stringResource(R.string.search),
                        modifier = Modifier.padding(start = contentInsets.calculateStartPadding(layoutDirection)),
                    )
                },
                trailingIcon = if (viewModel.searchQuery.isNotEmpty()) {
                    {
                        IconButton(
                            onClick = { viewModel.onSearchQueryChange("") },
                            modifier = Modifier.padding(end = contentInsets.calculateEndPadding(layoutDirection)),
                        ) {
                            Icon(painterResource(R.drawable.ic_clear), contentDescription = stringResource(R.string.clear_search))
                        }
                    }
                } else null,
                singleLine = true,
            )

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(
                        items = filtered,
                        key = { it.code }
                    ) { guideline ->
                        val result = if (hasQuery) snippetMap[guideline.code] else null
                        GuidelineListItem(
                            guideline = guideline,
                            searchResult = result,
                            selected = selectedCode?.let { it == guideline.code },
                            onClick = { onGuidelineClick(guideline, result) },
                            contentInsets = contentInsets,
                            modifier = Modifier.animateItem(),
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = filtered.isEmpty() && viewModel.searchQuery.isNotBlank() && !viewModel.isSearching,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.Center),
                ) {
                    Text(
                        text = stringResource(R.string.emptySearch),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    )
                }
            }
        }
    }
}

@Composable
private fun GuidelineListItem(
    guideline: Guideline,
    searchResult: SearchResult?,
    /** Whether the row is the open guideline, or null when the list isn't beside one. */
    selected: Boolean?,
    onClick: () -> Unit,
    /** Space kept clear of a display cutout, inside the row's background. */
    contentInsets: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val highlightColor = MaterialTheme.colorScheme.secondary

    val annotatedTitle = remember(searchResult, guideline.title, highlightColor) {
        searchResult?.titleHighlights?.takeIf { it.isNotEmpty() }
            ?.let { highlighted(guideline.title, it, highlightColor) }
    }

    val annotatedSnippet = remember(searchResult, highlightColor) {
        val snippet = searchResult?.snippet ?: return@remember null
        highlighted(snippet, searchResult.snippetHighlights, highlightColor)
    }

    ListItem(
        headlineContent = {
            if (annotatedTitle != null) {
                Text(
                    text = annotatedTitle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Text(
                    guideline.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        overlineContent = { Text(guideline.code) },
        supportingContent = if (annotatedSnippet != null) {
            {
                Text(
                    text = annotatedSnippet,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    fontStyle = FontStyle.Italic,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else null,
        trailingContent = {
            val spokenVersion = stringResource(R.string.version_n, guideline.version)
            Text(
                stringResource(R.string.guideline_version, guideline.version),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics { contentDescription = spokenVersion },
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = modifier
            .background(if (selected == true) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .then(if (selected != null) Modifier.semantics { this.selected = selected } else Modifier)
            .padding(contentInsets),
        tonalElevation = 0.dp,
    )
}

/** [text] with the given `start until end` ranges in bold and [color]. */
private fun highlighted(text: String, ranges: List<IntRange>, color: Color): AnnotatedString =
    buildAnnotatedString {
        append(text)
        for (range in ranges) {
            val start = range.first.coerceIn(0, text.length)
            val end = (range.last + 1).coerceIn(start, text.length)
            addStyle(SpanStyle(fontWeight = FontWeight.Bold, color = color), start, end)
        }
    }

/** Tablets: what's beside the list when no guideline is open. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoGuidelineSelectedScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = {}, colors = qrhTopAppBarColors())
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painterResource(R.drawable.ic_list),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.no_guideline_selected),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp),
            )
            Text(
                text = stringResource(R.string.choose_guideline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
