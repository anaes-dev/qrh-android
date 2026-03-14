package dev.anaes.qrh.ui.swipe

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import dev.anaes.qrh.model.ContentItem
import dev.anaes.qrh.model.Guideline
import dev.anaes.qrh.ui.components.HtmlText
import dev.anaes.qrh.ui.theme.BoxColors
import dev.anaes.qrh.ui.theme.LocalIsDarkTheme
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeViewScreen(
    guideline: Guideline,
    onNavigateBack: () -> Unit,
    onGuidelineLink: (code: String) -> Unit,
) {
    val context = LocalContext.current
    val filteredContent = remember(guideline.code) {
        guideline.content.filter { it.type != 11 && it.type != 12 }
    }
    val pagerState = rememberPagerState(pageCount = { filteredContent.size })

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(guideline.title, fontSize = 14.sp, maxLines = 1)
                        Text(guideline.code, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
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
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            val pageOffset = pagerState.getOffsetDistanceInPages(pageIndex)
                            alpha = 0.9f + (1f - abs(pageOffset)) * 0.1f
                        }
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                ) {
                    when (item.type) {
                        5, 6, 7, 8, 9 -> SwipeBoxPage(
                            item = item,
                            onGuidelineLink = onGuidelineLink,
                            onExternalLink = { url ->
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            },
                        )
                        10 -> SwipeImagePage(item = item)
                        else -> SwipeStandardPage(
                            item = item,
                            onGuidelineLink = onGuidelineLink,
                            onExternalLink = { url ->
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            },
                        )
                    }
                }
            }

            // Page indicator
            Text(
                text = "${pagerState.currentPage + 1} of ${filteredContent.size}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 4.dp),
            )

            // Dot indicators
            Row(
                modifier = Modifier.padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(filteredContent.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == pagerState.currentPage) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == pagerState.currentPage)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
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
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                HtmlText(
                    html = item.body,
                    style = TextStyle(fontSize = 18.sp, lineHeight = 26.sp),
                    onGuidelineLink = onGuidelineLink,
                    onExternalLink = onExternalLink,
                )
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
                modifier = Modifier.padding(bottom = 12.dp),
            )
            AsyncImage(
                model = "file:///android_asset/${item.body}",
                contentDescription = item.head.ifEmpty { "Guideline image" },
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth,
            )
        }
    }
}

@Composable
private fun SwipeStandardPage(
    item: ContentItem,
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
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color.White else Color.Black),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = item.step,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.Black else Color.White,
                    )
                }
                Box(modifier = Modifier.height(12.dp))
            }
            if (item.type == 2) {
                // START marker in card view
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isDark) Color.White else Color.Black)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    HtmlText(
                        html = item.body,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            lineHeight = 26.sp,
                            color = if (isDark) Color.Black else Color.White,
                        ),
                        onGuidelineLink = onGuidelineLink,
                        onExternalLink = onExternalLink,
                    )
                }
            } else {
                if (item.head.isNotBlank()) {
                    HtmlText(
                        html = item.head,
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp),
                        onGuidelineLink = onGuidelineLink,
                        onExternalLink = onExternalLink,
                        modifier = Modifier.padding(bottom = 12.dp),
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
}
