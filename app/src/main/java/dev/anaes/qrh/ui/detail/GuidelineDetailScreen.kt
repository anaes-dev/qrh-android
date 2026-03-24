package dev.anaes.qrh.ui.detail

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anaes.qrh.data.UserPreferences
import dev.anaes.qrh.model.Guideline
import dev.anaes.qrh.ui.components.BreadcrumbBar
import dev.anaes.qrh.ui.components.BreadcrumbEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidelineDetailScreen(
    guideline: Guideline,
    breadcrumbs: List<BreadcrumbEntry>,
    preferences: UserPreferences,
    onNavigateBack: () -> Unit,
    onHomeClick: () -> Unit,
    onBreadcrumbClick: (index: Int) -> Unit,
    onGuidelineLink: (code: String) -> Unit,
    onSwipeView: () -> Unit,
) {
    val context = LocalContext.current
    val expandingDisabled by preferences.expandingDisabled.collectAsState(initial = false)
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // Collapsible box state
    val collapsedState = remember(guideline.code) { mutableStateMapOf<Int, Boolean>() }

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

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = guideline.code,
                            fontSize = codeFontSize,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Text(
                            text = guideline.title,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Text(
                            text = "v. ${guideline.version}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(guideline.url)))
                    }) {
                        Icon(
                            Icons.Filled.Download,
                            contentDescription = "Download PDF",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                    IconButton(onClick = onSwipeView) {
                        Icon(
                            Icons.Filled.ViewCarousel,
                            contentDescription = "Card view",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    scrolledContainerColor = MaterialTheme.colorScheme.primary,
                ),
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
                    key = { index, item -> "${guideline.code}-$index" }
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
                        onExternalLink = { url ->
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        },
                    )
                }
            }
        }
    }
}
