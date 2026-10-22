package dev.anaes.qrh.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.text.htmlEncode
import dev.anaes.qrh.model.ContentTable
import dev.anaes.qrh.ui.components.HtmlTextStatic

// The first column names each row, so it needs less room than the values
private const val FIRST_COLUMN_WEIGHT = 0.6f

private val LITRES = Regex("(\\d) ?L\\b")
private val BRACKETED_AFTER_TEXT = Regex(" (\\([^)]*\\))")

/**
 * A box's table (4-1's oxygen cylinder contents), drawn as text so it scales with the font size,
 * can be searched and highlighted, and is read by screen readers a row at a time.
 */
@Composable
fun BoxTable(
    table: ContentTable,
    style: TextStyle,
    headingColor: Color,
    lineColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Each row is read with its column headings, so the heading row itself isn't
        TableRow(
            cells = table.columns,
            style = style.copy(fontWeight = FontWeight.Bold, color = headingColor),
            modifier = Modifier.clearAndSetSemantics {},
        )
        HorizontalDivider(thickness = 1.5.dp, color = lineColor)
        table.rows.forEachIndexed { index, row ->
            if (index > 0) HorizontalDivider(color = lineColor)
            TableRow(
                cells = row,
                style = style,
                firstCellStyle = style.copy(fontWeight = FontWeight.Bold, color = headingColor),
                modifier = Modifier.clearAndSetSemantics { contentDescription = spokenRow(table, row) },
            )
        }
    }
}

@Composable
private fun TableRow(
    cells: List<String>,
    style: TextStyle,
    modifier: Modifier = Modifier,
    firstCellStyle: TextStyle = style,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        cells.forEachIndexed { index, cell ->
            HtmlTextStatic(
                html = cellHtml(cell),
                style = if (index == 0) {
                    firstCellStyle.copy(textAlign = TextAlign.Start)
                } else {
                    style.copy(textAlign = TextAlign.Center)
                },
                modifier = Modifier
                    .weight(if (index == 0) FIRST_COLUMN_WEIGHT else 1f)
                    .padding(horizontal = 2.dp, vertical = 6.dp),
            )
        }
    }
}

/**
 * A cell as HTML, with "25% full (34 bar)" as "25% full" / "(34 bar)" rather than breaking inside
 * the brackets. A line break, not non-breaking spaces, so at the largest text sizes it can still
 * wrap between words instead of mid-word.
 */
private fun cellHtml(cell: String): String =
    cell.htmlEncode().replace(BRACKETED_AFTER_TEXT) { "<br>${it.groupValues[1]}" }

/** A row as screen readers say it: "Size C: 25% full (34 bar), 43 litres; 50% full (69 bar), 85 litres; …" */
private fun spokenRow(table: ContentTable, row: List<String>): String {
    val values = table.columns.drop(1).zip(row.drop(1)) { column, value ->
        "$column, ${value.replace(LITRES, "$1 litres")}"
    }
    return "${table.columns.first()} ${row.first()}: ${values.joinToString("; ")}"
}
