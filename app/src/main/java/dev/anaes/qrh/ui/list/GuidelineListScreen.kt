package dev.anaes.qrh.ui.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anaes.qrh.QrhViewModel
import dev.anaes.qrh.R
import dev.anaes.qrh.SearchResult
import dev.anaes.qrh.model.Guideline
import kotlinx.coroutines.flow.filter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidelineListScreen(
    viewModel: QrhViewModel,
    onGuidelineClick: (Guideline) -> Unit,
    onAboutClick: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val filtered = viewModel.filteredGuidelines
    val searchResults = viewModel.searchResults
    val hasQuery = viewModel.searchQuery.isNotBlank()
    val snackLaunchText = stringResource(R.string.snackLaunch)

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

    // Build a lookup map for snippets when searching
    val snippetMap = remember(searchResults) {
        searchResults.associateBy { it.guideline.code }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QRH") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                actions = {
                    IconButton(onClick = onAboutClick) {
                        Icon(Icons.Filled.Info, contentDescription = "About")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TextField(
                value = viewModel.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search guidelines...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search)) },
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
                            onClick = { onGuidelineClick(guideline) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }

                AnimatedVisibility(
                    visible = filtered.isEmpty() && viewModel.searchQuery.isNotBlank() && !viewModel.isSearching,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.Center),
                ) {
                    Text(
                        text = stringResource(R.string.emptySearch),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val highlightColor = MaterialTheme.colorScheme.secondary

    val annotatedSnippet = remember(searchResult) {
        val snippet = searchResult?.snippet ?: return@remember null
        buildAnnotatedString {
            val start = searchResult.matchStart.coerceIn(0, snippet.length)
            val end = searchResult.matchEnd.coerceIn(start, snippet.length)
            append(snippet.substring(0, start))
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = highlightColor)) {
                append(snippet.substring(start, end))
            }
            append(snippet.substring(end))
        }
    }

    ListItem(
        headlineContent = {
            Text(
                guideline.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
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
            Text(
                "v.${guideline.version}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        modifier = modifier.clickable(onClick = onClick),
        tonalElevation = 0.dp,
    )
}
