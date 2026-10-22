package dev.anaes.qrh.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

/**
 * A step or section number in a filled circle. The circle is at least [minSize] across and grows
 * with the text, so large font sizes and two-digit numbers aren't clipped.
 *
 * Screen readers read [spokenAs] ("Step 3") instead of the bare number.
 */
@Composable
fun NumberBadge(
    number: String,
    spokenAs: String,
    minSize: Dp,
    fontSize: TextUnit,
    background: Color,
    foreground: Color,
    modifier: Modifier = Modifier,
    isHeading: Boolean = false,
) {
    Box(
        // The circle and its screen-reader bounds are outside the square layout, so take its size
        modifier = modifier
            .clip(CircleShape)
            .background(background)
            .clearAndSetSemantics {
                contentDescription = spokenAs
                if (isHeading) heading()
            }
            .squareAroundContent(minSize),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number,
            color = foreground,
            fontSize = fontSize,
            // Not the inherited body line height, which would make the circle taller than minSize
            lineHeight = fontSize,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.padding(4.dp),
        )
    }
}

/** Lays the content out centred in a square as big as its larger side, and at least [minSize]. */
private fun Modifier.squareAroundContent(minSize: Dp): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
    val size = maxOf(minSize.roundToPx(), placeable.width, placeable.height)
        .coerceAtMost(minOf(constraints.maxWidth, constraints.maxHeight))
    layout(size, size) {
        placeable.place((size - placeable.width) / 2, (size - placeable.height) / 2)
    }
}
