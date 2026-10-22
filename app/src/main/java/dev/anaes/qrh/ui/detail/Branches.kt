package dev.anaes.qrh.ui.detail

import android.text.Html
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anaes.qrh.R
import dev.anaes.qrh.model.ContentBlock
import dev.anaes.qrh.ui.components.HtmlTextStatic

/**
 * Width from which a fork's branches are shown side by side rather than one above the other: phones
 * in landscape, and the steps column of a tablet in landscape. Narrower, as beside the list on a
 * tablet in portrait, each branch would be left too little width once its step circles are drawn.
 */
private val SIDE_BY_SIDE_MIN_WIDTH = 600.dp

// The rail beside a branch's items, in the gap left of their cards, running down from the left
// edge of the branch's header
private val RailInset = 10.dp
private val RailX = 12.dp
// As wide as the header's border, so the rail continues it
private val RailWidth = 2.dp

private val HeaderBorderWidth = RailWidth
private val HeaderCornerRadius = 4.dp

/**
 * Alternative paths through a guideline ("If circulatory arrest" / "If no circulatory arrest"),
 * all shown, nothing hidden: side by side when there's room (tablets, landscape), otherwise one
 * above the other. Each branch has its condition as a heading and a rail beside its items, so it's
 * clear where it ends.
 */
@Composable
fun BranchFork(
    fork: ContentBlock.Fork,
    /** The HTML of the branch header at a content index. */
    headerOf: (Int) -> String,
    itemView: @Composable (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    /** Extra modifier for the branch header at a content index (to bring a search match into view). */
    headerModifier: (Int) -> Modifier = { Modifier },
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        if (fork.branches.size > 1 && maxWidth >= SIDE_BY_SIDE_MIN_WIDTH) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (branch in fork.branches) {
                    BranchColumn(branch, headerOf, itemView, headerModifier, Modifier.weight(1f))
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (branch in fork.branches) {
                    BranchColumn(branch, headerOf, itemView, headerModifier)
                }
            }
        }
    }
}

@Composable
private fun BranchColumn(
    branch: ContentBlock.Branch,
    headerOf: (Int) -> String,
    itemView: @Composable (index: Int) -> Unit,
    headerModifier: (Int) -> Modifier,
    modifier: Modifier = Modifier,
) {
    val railColor = MaterialTheme.colorScheme.outline
    Column(modifier = modifier) {
        BranchHeader(
            html = headerOf(branch.headerIndex),
            modifier = headerModifier(branch.headerIndex),
            attachedRail = true,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    // On the start side, mirrored for right-to-left layouts as the padding is
                    val x = if (layoutDirection == LayoutDirection.Rtl) {
                        size.width - (RailX + RailWidth).toPx()
                    } else {
                        RailX.toPx()
                    }
                    // From under the header's border, so the two join; square where they meet
                    val top = -HeaderBorderWidth.toPx()
                    val radius = RailWidth.toPx() / 2
                    drawRoundRect(
                        color = railColor,
                        topLeft = Offset(x, top),
                        size = Size(RailWidth.toPx(), size.height - top),
                        cornerRadius = CornerRadius(radius),
                    )
                    drawRect(
                        color = railColor,
                        topLeft = Offset(x, top),
                        size = Size(RailWidth.toPx(), radius),
                    )
                }
                .padding(start = RailInset),
        ) {
            for (index in branch.itemIndices) {
                itemView(index)
            }
        }
    }
}

/** The condition starting a branch ("If circulatory arrest:"), read by screen readers as a heading. */
@Composable
fun BranchHeader(
    html: String,
    modifier: Modifier = Modifier,
    /** Whether the branch's rail runs down from this header, joined to its bottom-left corner. */
    attachedRail: Boolean = false,
) {
    val shape = if (attachedRail) {
        RoundedCornerShape(
            topStart = HeaderCornerRadius,
            topEnd = HeaderCornerRadius,
            bottomEnd = HeaderCornerRadius,
            bottomStart = 0.dp,
        )
    } else {
        RoundedCornerShape(HeaderCornerRadius)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = if (attachedRail) 0.dp else 4.dp)
            .border(HeaderBorderWidth, MaterialTheme.colorScheme.outline, shape)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .semantics(mergeDescendants = true) { heading() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(R.drawable.ic_call_split),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(8.dp))
        HtmlTextStatic(
            html = html,
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp),
        )
    }
}

/** A branch condition as plain text for screen readers: "if circulatory arrest". */
fun spokenCondition(html: String): String =
    Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString().trim().trimEnd(':')
        .replaceFirstChar { it.lowercase() }
