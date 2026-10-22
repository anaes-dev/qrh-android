package dev.anaes.qrh.ui.swipe

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anaes.qrh.R
import dev.anaes.qrh.model.ContentItem
import dev.anaes.qrh.model.ContentType
import dev.anaes.qrh.model.Guideline
import dev.anaes.qrh.model.branchCondition
import dev.anaes.qrh.ui.components.HtmlText
import dev.anaes.qrh.ui.components.NumberBadge
import dev.anaes.qrh.ui.components.rememberExternalLinkHandler
import dev.anaes.qrh.ui.detail.BoxTable
import dev.anaes.qrh.ui.detail.BranchHeader
import dev.anaes.qrh.ui.detail.spokenCondition
import dev.anaes.qrh.ui.theme.BoxColors
import dev.anaes.qrh.ui.theme.LocalIsDarkTheme
import dev.anaes.qrh.ui.theme.qrhTopAppBarColors
import kotlin.math.abs
import kotlinx.coroutines.launch
import coil3.compose.AsyncImage

/** Most dots shown at once; longer guidelines (up to 23 cards) get a sliding window. */
private const val MAX_VISIBLE_DOTS = 9

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeViewScreen(
    guideline: Guideline,
    onNavigateBack: () -> Unit,
    onGuidelineLink: (code: String) -> Unit,
) {
    val onExternalLink = rememberExternalLinkHandler()
    val filteredContent = remember(guideline.code) {
        // Banners (START) are boilerplate and branch conditions label their branch's cards, so
        // neither has a card of its own
        guideline.content.filter { it.type != ContentType.BANNER && it.type != ContentType.BRANCH }
    }
    val pagerState = rememberPagerState(pageCount = { filteredContent.size })

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(guideline.title, fontSize = 14.sp, maxLines = 1)
                        Text(
                            guideline.code,
                            fontSize = 12.sp,
                            color = LocalContentColor.current.copy(alpha = 0.7f),
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                colors = qrhTopAppBarColors(),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                pageSpacing = 16.dp,
                beyondViewportPageCount = 1,
            ) { pageIndex ->
                val item = filteredContent[pageIndex]
                val branchCondition = guideline.content.branchCondition(item)
                Column(
                    modifier = Modifier
                        .graphicsLayer {
                            val pageOffset = pagerState.getOffsetDistanceInPages(pageIndex)
                            alpha = 0.9f + (1f - abs(pageOffset)) * 0.1f
                        }
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                ) {
                    // In a branch, its condition above the card, so the two step 6s of 3-10 (in
                    // and not in cardiac arrest) can be told apart
                    if (branchCondition != null) {
                        BranchHeader(html = branchCondition, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        when (item.type) {
                            5, 6, 7, 8, 9 -> SwipeBoxPage(
                                item = item,
                                onGuidelineLink = onGuidelineLink,
                                onExternalLink = onExternalLink,
                            )
                            10 -> SwipeImagePage(item = item)
                            else -> SwipeStandardPage(
                                item = item,
                                branchCondition = branchCondition,
                                onGuidelineLink = onGuidelineLink,
                                onExternalLink = onExternalLink,
                            )
                        }
                    }
                }
            }

            if (filteredContent.isNotEmpty()) {
                // Page indicator
                Text(
                    text = stringResource(
                        R.string.card_position,
                        pagerState.currentPage + 1,
                        filteredContent.size,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 4.dp),
                )

                PageDots(
                    pagerState = pagerState,
                    pageCount = filteredContent.size,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
        }
    }
}

/**
 * Tappable page dots. Shows at most [MAX_VISIBLE_DOTS], sliding the window to keep the current
 * page in view; dots at a window edge with more pages beyond are drawn smaller as a hint.
 */
@Composable
private fun PageDots(
    pagerState: PagerState,
    pageCount: Int,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    val currentPage = pagerState.currentPage

    val visibleCount = minOf(pageCount, MAX_VISIBLE_DOTS)
    val firstVisible = (currentPage - visibleCount / 2).coerceIn(0, pageCount - visibleCount)
    val lastVisible = firstVisible + visibleCount - 1

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (index in firstVisible..lastVisible) {
            key(index) {
                val isSelected = index == currentPage
                val isEdgeHint = (index == firstVisible && index > 0) ||
                    (index == lastVisible && index < pageCount - 1)
                val scale by animateFloatAsState(
                    targetValue = when {
                        isSelected -> 1f
                        isEdgeHint -> 0.5f
                        else -> 0.75f
                    },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                    label = "dotScale",
                )
                val alpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.4f,
                    label = "dotAlpha",
                )
                val dotDescription = stringResource(R.string.go_to_card, index + 1)

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .semantics { contentDescription = dotDescription }
                        .clickable {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                            }
                            .clip(CircleShape)
                            .background(if (isSelected) activeColor else inactiveColor)
                    )
                }
            }
        }
    }
}

@Composable
private fun SwipeBoxPage(
    item: ContentItem,
    onGuidelineLink: (String) -> Unit,
    onExternalLink: (String) -> Unit,
) {
    val isDark = LocalIsDarkTheme.current
    val colors = BoxColors.forType(item.type, isDark)

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = colors.background),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
            ) {
                Text(
                    text = item.head,
                    color = colors.text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .semantics { heading() },
                )
                val bodyStyle = TextStyle(fontSize = 18.sp, lineHeight = 26.sp)
                if (item.table != null) {
                    BoxTable(
                        table = item.table,
                        style = bodyStyle,
                        headingColor = colors.text,
                        lineColor = colors.arrow,
                    )
                } else {
                    HtmlText(
                        html = item.body,
                        style = bodyStyle,
                        onGuidelineLink = onGuidelineLink,
                        onExternalLink = onExternalLink,
                    )
                }
            }

            // Top gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(colors.background, colors.background.copy(alpha = 0f))
                        )
                    )
            )

            // Bottom gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(colors.background.copy(alpha = 0f), colors.background)
                        )
                    )
            )
        }
    }
}

@Composable
private fun SwipeImagePage(item: ContentItem) {
    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = item.head,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .semantics { heading() },
            )
            AsyncImage(
                model = "file:///android_asset/${item.body}",
                contentDescription = item.head.ifEmpty { stringResource(R.string.guideline_image) },
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth,
            )
        }
    }
}

@Composable
private fun SwipeStandardPage(
    item: ContentItem,
    branchCondition: String?,
    onGuidelineLink: (String) -> Unit,
    onExternalLink: (String) -> Unit,
) {
    val isDark = LocalIsDarkTheme.current

    Card(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        ) {
            if (item.step.isNotBlank()) {
                // Large step number in a circle
                NumberBadge(
                    number = item.step,
                    spokenAs = if (branchCondition != null) {
                        stringResource(R.string.step_in_branch, item.step, spokenCondition(branchCondition))
                    } else {
                        stringResource(R.string.step_n, item.step)
                    },
                    minSize = 48.dp,
                    fontSize = 22.sp,
                    background = if (isDark) Color.White else Color.Black,
                    foreground = if (isDark) Color.Black else Color.White,
                    isHeading = item.head.isBlank(),
                )
                Box(modifier = Modifier.height(12.dp))
            }
            if (item.head.isNotBlank()) {
                HtmlText(
                    html = item.head,
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp),
                    onGuidelineLink = onGuidelineLink,
                    onExternalLink = onExternalLink,
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .semantics { heading() },
                )
            }
            if (item.body.isNotBlank()) {
                HtmlText(
                    html = item.body,
                    style = TextStyle(fontSize = 18.sp, lineHeight = 26.sp),
                    onGuidelineLink = onGuidelineLink,
                    onExternalLink = onExternalLink,
                )
            }
        }
    }
}
