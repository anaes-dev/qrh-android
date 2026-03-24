package dev.anaes.qrh.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BreadcrumbEntry(
    val code: String,
    val title: String
)

@Composable
fun BreadcrumbBar(
    entries: List<BreadcrumbEntry>,
    onHomeClick: () -> Unit,
    onEntryClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (entries.size <= 1) return

    val scrollState = rememberScrollState()

    LaunchedEffect(entries.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AssistChip(
            onClick = onHomeClick,
            label = { Text("Home", fontSize = 12.sp) },
            leadingIcon = {
                Icon(
                    Icons.Filled.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(16.dp)
                )
            },
        )

        entries.forEachIndexed { index, entry ->
            Icon(
                Icons.AutoMirrored.Filled.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (index < entries.lastIndex) {
                SuggestionChip(
                    onClick = { onEntryClick(index) },
                    label = { Text(entry.title, fontSize = 12.sp, maxLines = 1) },
                )
            } else {
                SuggestionChip(
                    onClick = { },
                    label = { Text(entry.title, fontSize = 12.sp, maxLines = 1) },
                    enabled = false,
                )
            }
        }
    }
}
