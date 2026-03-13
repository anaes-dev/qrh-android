package dev.anaes.qrh.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import dev.anaes.qrh.model.ContentItem
import dev.anaes.qrh.ui.components.HtmlText
import dev.anaes.qrh.ui.components.HtmlTextStatic
import dev.anaes.qrh.ui.theme.BoxColors
import dev.anaes.qrh.ui.theme.LocalIsDarkTheme
import dev.anaes.qrh.ui.theme.SubtleCardBgDark
import dev.anaes.qrh.ui.theme.SubtleCardBgLight
import dev.anaes.qrh.ui.theme.RedBgDark
import dev.anaes.qrh.ui.theme.RedBgLight
import dev.anaes.qrh.ui.theme.RedTxtDark
import dev.anaes.qrh.ui.theme.RedTxtLight

@Composable
fun ContentItemView(
    item: ContentItem,
    index: Int,
    isCollapsed: Boolean,
    expandingDisabled: Boolean,
    onToggleCollapse: (Int) -> Unit,
    onGuidelineLink: (String) -> Unit,
    onExternalLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (item.type) {
        1 -> PreambleItem(item, onGuidelineLink, onExternalLink, modifier)
        2 -> StartMarker(item, modifier)
        3 -> StandardStepItem(item, onGuidelineLink, onExternalLink, modifier)
        4 -> OneLineStepItem(item, onGuidelineLink, onExternalLink, modifier)
        5, 6, 7, 8, 9 -> CollapsibleBoxItem(
            item = item,
            isCollapsed = if (expandingDisabled) false else isCollapsed,
            showArrow = !expandingDisabled,
            onToggle = { onToggleCollapse(index) },
            onGuidelineLink = onGuidelineLink,
            onExternalLink = onExternalLink,
            modifier = modifier,
        )
        10 -> ImageItem(item, modifier)
        11 -> VersionItem(item, modifier)
        12 -> EndDisclaimerItem(item, modifier)
    }
}

@Composable
private fun StepCircle(step: String) {
    if (step.isBlank()) return
    val isDark = LocalIsDarkTheme.current
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (isDark) Color.White else Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = step,
            color = if (isDark) Color.Black else Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SubtleCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val isDark = LocalIsDarkTheme.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) SubtleCardBgDark else SubtleCardBgLight,
        ),
        shape = RoundedCornerShape(8.dp),
    ) {
        content()
    }
}

@Composable
private fun PreambleItem(
    item: ContentItem,
    onGuidelineLink: (String) -> Unit,
    onExternalLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    SubtleCard(modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            if (item.head.isNotBlank()) {
                HtmlText(
                    html = item.head,
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp),
                    onGuidelineLink = onGuidelineLink,
                    onExternalLink = onExternalLink,
                )
            }
            if (item.body.isNotBlank()) {
                HtmlText(
                    html = item.body,
                    style = TextStyle(fontSize = 15.sp, lineHeight = 20.sp),
                    onGuidelineLink = onGuidelineLink,
                    onExternalLink = onExternalLink,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun StartMarker(
    item: ContentItem,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalIsDarkTheme.current
    val bgColor = if (isDark) Color.White else Color.Black
    val fgColor = if (isDark) Color.Black else Color.White
    Box(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        HtmlTextStatic(
            html = item.body,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = fgColor,
            ),
        )
    }
}

@Composable
private fun StandardStepItem(
    item: ContentItem,
    onGuidelineLink: (String) -> Unit,
    onExternalLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    SubtleCard(modifier = modifier) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StepCircle(item.step)
            Column(modifier = Modifier.weight(1f)) {
                if (item.head.isNotBlank()) {
                    HtmlText(
                        html = item.head,
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp),
                        onGuidelineLink = onGuidelineLink,
                        onExternalLink = onExternalLink,
                    )
                }
                if (item.body.isNotBlank()) {
                    HtmlText(
                        html = item.body,
                        style = TextStyle(fontSize = 15.sp, lineHeight = 20.sp),
                        onGuidelineLink = onGuidelineLink,
                        onExternalLink = onExternalLink,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun OneLineStepItem(
    item: ContentItem,
    onGuidelineLink: (String) -> Unit,
    onExternalLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    SubtleCard(modifier = modifier) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepCircle(item.step)
            HtmlText(
                html = item.body,
                style = TextStyle(fontSize = 15.sp, lineHeight = 20.sp),
                onGuidelineLink = onGuidelineLink,
                onExternalLink = onExternalLink,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CollapsibleBoxItem(
    item: ContentItem,
    isCollapsed: Boolean,
    showArrow: Boolean,
    onToggle: () -> Unit,
    onGuidelineLink: (String) -> Unit,
    onExternalLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalIsDarkTheme.current
    val colors = BoxColors.forType(item.type, isDark)
    val arrowRotation by animateFloatAsState(
        targetValue = if (isCollapsed) 0f else 180f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "arrowRotation",
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 36.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        colors = CardDefaults.cardColors(containerColor = colors.background),
    ) {
        // Header row — uses HtmlTextStatic so it doesn't consume clicks
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (showArrow) Modifier.clickable(onClick = onToggle) else Modifier)
                .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HtmlTextStatic(
                html = item.head,
                style = TextStyle(
                    color = colors.text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                ),
                modifier = Modifier.weight(1f),
            )
            if (showArrow) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.arrow),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (isCollapsed) "Expand" else "Collapse",
                        tint = colors.text,
                        modifier = Modifier.graphicsLayer { rotationZ = arrowRotation },
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = !isCollapsed,
            enter = expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                )
            ) + fadeIn(spring(stiffness = Spring.StiffnessMediumLow)),
            exit = shrinkVertically(
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            ) + fadeOut(spring(stiffness = Spring.StiffnessMedium)),
        ) {
            HtmlText(
                html = item.body,
                style = TextStyle(fontSize = 15.sp, lineHeight = 20.sp),
                onGuidelineLink = onGuidelineLink,
                onExternalLink = onExternalLink,
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
            )
        }
    }
}

@Composable
private fun ImageItem(
    item: ContentItem,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = "file:///android_asset/${item.body}",
            contentDescription = item.head,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth,
        )
        if (item.head.isNotBlank()) {
            Text(
                text = item.head,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun VersionItem(
    item: ContentItem,
    modifier: Modifier = Modifier,
) {
    Text(
        text = item.body,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )
}

@Composable
private fun EndDisclaimerItem(
    item: ContentItem,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalIsDarkTheme.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) RedBgDark else RedBgLight,
        ),
    ) {
        HtmlText(
            html = item.head,
            style = TextStyle(
                color = if (isDark) RedTxtDark else RedTxtLight,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            ),
            modifier = Modifier.padding(12.dp),
        )
    }
}
