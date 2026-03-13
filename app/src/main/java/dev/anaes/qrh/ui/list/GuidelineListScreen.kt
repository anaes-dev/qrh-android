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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anaes.qrh.QrhViewModel
import dev.anaes.qrh.R
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

    // Startup snackbar
    LaunchedEffect(viewModel.isStartup) {
        if (viewModel.isStartup) {
            viewModel.onStartupComplete()
            snackbarHostState.showSnackbar(
                message = "Unofficial adaptation of Quick Reference Handbook\n" +
                    "Not endorsed by the Association of Anaesthetists\n" +
                    "Untested and unregulated; not recommended for clinical use\n" +
                    "No guarantees of completeness, accuracy or performance\n" +
                    "Should not override your own knowledge and judgement",
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
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
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
                        ListItem(
                            headlineContent = {
                                Text(
                                    guideline.title,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            overlineContent = { Text(guideline.code) },
                            trailingContent = {
                                Text(
                                    "v.${guideline.version}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            modifier = Modifier
                                .animateItem()
                                .clickable { onGuidelineClick(guideline) },
                            tonalElevation = 0.dp,
                        )
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = filtered.isEmpty() && viewModel.searchQuery.isNotBlank(),
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
