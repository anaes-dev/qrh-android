package dev.anaes.qrh.ui.components

import android.text.Html
import android.text.Spanned
import android.text.style.BulletSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.regex.Pattern

private val URL_PATTERN = Pattern.compile(
    "((ht|f)tp(s?)://|www\\.|yellowcard\\.)" +
        "([\\w\\-]+\\.){1,}?([\\w\\-.~]+/?)*" +
        "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]*\$~@!:/{};']*",
    Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL
)
private val PHONE_PATTERN = Pattern.compile("[0][0-9]{10}")
private val GUIDELINE_PATTERN = Pattern.compile("[(]?[→][\\s]?[1-4][-][0-9]{1,2}[)]?")

/**
 * Renders HTML text with clickable links for guidelines, URLs, and phone numbers.
 * Uses ClickableText internally — will consume touch events.
 */
@Composable
fun HtmlText(
    html: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    onGuidelineLink: (code: String) -> Unit = {},
    onExternalLink: (url: String) -> Unit = {},
) {
    val linkColor = MaterialTheme.colorScheme.secondary
    // Use explicit style color if provided, otherwise fall back to LocalContentColor
    val textColor = style.color.takeIf { it != Color.Unspecified } ?: LocalContentColor.current

    val parsed = remember(html, linkColor, textColor) {
        parseHtml(html, linkColor, textColor)
    }

    if (parsed.bulletItems.isNotEmpty()) {
        // Render bullet list as structured layout for proper alignment
        Column(modifier = modifier) {
            if (parsed.preText != null) {
                ClickableTextBlock(
                    annotated = parsed.preText,
                    style = style.copy(color = textColor),
                    onGuidelineLink = onGuidelineLink,
                    onExternalLink = onExternalLink,
                )
            }
            for (bulletItem in parsed.bulletItems) {
                Row(modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp)) {
                    Text(
                        text = "\u2022",
                        style = style.copy(color = textColor),
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    ClickableTextBlock(
                        annotated = bulletItem,
                        style = style.copy(color = textColor),
                        onGuidelineLink = onGuidelineLink,
                        onExternalLink = onExternalLink,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            if (parsed.postText != null) {
                ClickableTextBlock(
                    annotated = parsed.postText,
                    style = style.copy(color = textColor),
                    onGuidelineLink = onGuidelineLink,
                    onExternalLink = onExternalLink,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    } else {
        ClickableTextBlock(
            annotated = parsed.fullText,
            style = style.copy(color = textColor),
            onGuidelineLink = onGuidelineLink,
            onExternalLink = onExternalLink,
            modifier = modifier,
        )
    }
}

/**
 * Non-clickable HTML text rendering. Does not consume touch events,
 * so parent clickable modifiers work properly.
 */
@Composable
fun HtmlTextStatic(
    html: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
) {
    val textColor = style.color.takeIf { it != Color.Unspecified } ?: LocalContentColor.current

    val annotated = remember(html, textColor) {
        buildSimpleAnnotatedString(html, textColor)
    }

    Text(
        text = annotated,
        modifier = modifier,
        style = style.copy(color = textColor),
    )
}

@Composable
private fun ClickableTextBlock(
    annotated: AnnotatedString,
    style: TextStyle,
    onGuidelineLink: (String) -> Unit,
    onExternalLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ClickableText(
        text = annotated,
        modifier = modifier,
        style = style,
        onClick = { offset ->
            annotated.getStringAnnotations("qrh", offset, offset).firstOrNull()?.let {
                onGuidelineLink(it.item)
                return@ClickableText
            }
            annotated.getStringAnnotations("url", offset, offset).firstOrNull()?.let {
                onExternalLink(it.item)
                return@ClickableText
            }
            annotated.getStringAnnotations("phone", offset, offset).firstOrNull()?.let {
                onExternalLink("tel:${it.item}")
                return@ClickableText
            }
        }
    )
}

private data class ParsedHtml(
    val fullText: AnnotatedString,
    val bulletItems: List<AnnotatedString> = emptyList(),
    val preText: AnnotatedString? = null,
    val postText: AnnotatedString? = null,
)

private fun parseHtml(
    html: String,
    linkColor: Color,
    textColor: Color,
): ParsedHtml {
    val spanned = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
    val bulletSpans = spanned.getSpans(0, spanned.length, BulletSpan::class.java)

    if (bulletSpans.isEmpty()) {
        return ParsedHtml(fullText = buildAnnotatedFromSpanned(spanned, linkColor, textColor))
    }

    // Extract bullet items as separate AnnotatedStrings
    val raw = spanned.toString()
    val bulletRanges = bulletSpans.map { span ->
        spanned.getSpanStart(span) to spanned.getSpanEnd(span)
    }.sortedBy { it.first }

    val items = mutableListOf<AnnotatedString>()
    var preText: AnnotatedString? = null
    var postText: AnnotatedString? = null

    // Text before first bullet
    val firstBulletStart = bulletRanges.first().first
    if (firstBulletStart > 0) {
        val pre = raw.substring(0, firstBulletStart).trimEnd()
        if (pre.isNotEmpty()) {
            preText = buildAnnotatedSubstring(spanned, 0, firstBulletStart, linkColor, textColor)
        }
    }

    // Each bullet item
    for ((start, end) in bulletRanges) {
        items.add(buildAnnotatedSubstring(spanned, start, end, linkColor, textColor))
    }

    // Text after last bullet
    val lastBulletEnd = bulletRanges.last().second
    if (lastBulletEnd < raw.length) {
        val post = raw.substring(lastBulletEnd).trim()
        if (post.isNotEmpty()) {
            postText = buildAnnotatedSubstring(spanned, lastBulletEnd, raw.length, linkColor, textColor)
        }
    }

    return ParsedHtml(
        fullText = buildAnnotatedFromSpanned(spanned, linkColor, textColor),
        bulletItems = items,
        preText = preText,
        postText = postText,
    )
}

private fun buildAnnotatedSubstring(
    spanned: Spanned,
    rangeStart: Int,
    rangeEnd: Int,
    linkColor: Color,
    textColor: Color,
): AnnotatedString {
    val raw = spanned.toString()
    val text = raw.substring(rangeStart, rangeEnd).trimEnd()
    if (text.isEmpty()) return AnnotatedString("")

    return buildAnnotatedString {
        append(text)

        for (span in spanned.getSpans(rangeStart, rangeEnd, Any::class.java)) {
            val spanStart = (spanned.getSpanStart(span) - rangeStart).coerceIn(0, text.length)
            val spanEnd = (spanned.getSpanEnd(span) - rangeStart).coerceIn(0, text.length)
            if (spanStart >= spanEnd) continue

            when (span) {
                is StyleSpan -> when (span.style) {
                    android.graphics.Typeface.BOLD -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), spanStart, spanEnd)
                    android.graphics.Typeface.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), spanStart, spanEnd)
                    android.graphics.Typeface.BOLD_ITALIC -> addStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic), spanStart, spanEnd)
                }
                is UnderlineSpan -> addStyle(SpanStyle(textDecoration = TextDecoration.Underline), spanStart, spanEnd)
                is URLSpan -> {
                    addStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline), spanStart, spanEnd)
                    addStringAnnotation("url", span.url, spanStart, spanEnd)
                }
            }
        }

        addAutoLinks(text, linkColor, spanned, rangeStart)
    }
}

private fun buildAnnotatedFromSpanned(
    spanned: Spanned,
    linkColor: Color,
    textColor: Color,
): AnnotatedString {
    val text = spanned.toString().trimEnd()

    return buildAnnotatedString {
        append(text)

        for (span in spanned.getSpans(0, spanned.length, Any::class.java)) {
            val start = spanned.getSpanStart(span).coerceAtMost(text.length)
            val end = spanned.getSpanEnd(span).coerceAtMost(text.length)
            if (start >= end) continue

            when (span) {
                is StyleSpan -> when (span.style) {
                    android.graphics.Typeface.BOLD -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                    android.graphics.Typeface.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                    android.graphics.Typeface.BOLD_ITALIC -> addStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic), start, end)
                }
                is UnderlineSpan -> addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
                is URLSpan -> {
                    addStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline), start, end)
                    addStringAnnotation("url", span.url, start, end)
                }
            }
        }

        addAutoLinks(text, linkColor, spanned, 0)
    }
}

private fun AnnotatedString.Builder.addAutoLinks(
    text: String,
    linkColor: Color,
    spanned: Spanned,
    baseOffset: Int,
) {
    val annotatedRanges = mutableListOf<IntRange>()

    // Collect existing URL span ranges
    for (span in spanned.getSpans(0, spanned.length, URLSpan::class.java)) {
        val s = (spanned.getSpanStart(span) - baseOffset).coerceIn(0, text.length)
        val e = (spanned.getSpanEnd(span) - baseOffset).coerceIn(0, text.length)
        if (s < e) annotatedRanges.add(s..e)
    }

    fun isAnnotated(pos: Int) = annotatedRanges.any { pos in it }

    // Auto-detect guideline cross-references
    val guidelineMatcher = GUIDELINE_PATTERN.matcher(text)
    while (guidelineMatcher.find()) {
        val start = guidelineMatcher.start()
        val end = guidelineMatcher.end()
        val matchText = text.substring(start, end)
        val code = matchText.replace("→ ", "").replace("→", "")
            .replace("(", "").replace(")", "").trim()
        addStyle(SpanStyle(color = linkColor, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline), start, end)
        addStringAnnotation("qrh", code, start, end)
        annotatedRanges.add(start..end)
    }

    // Auto-detect URLs
    val urlMatcher = URL_PATTERN.matcher(text)
    while (urlMatcher.find()) {
        val start = urlMatcher.start()
        val end = urlMatcher.end()
        if (isAnnotated(start)) continue
        val url = text.substring(start, end)
        val fullUrl = if (url.startsWith("http")) url else "http://$url"
        addStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline), start, end)
        addStringAnnotation("url", fullUrl, start, end)
        annotatedRanges.add(start..end)
    }

    // Auto-detect phone numbers
    val phoneMatcher = PHONE_PATTERN.matcher(text)
    while (phoneMatcher.find()) {
        val start = phoneMatcher.start()
        val end = phoneMatcher.end()
        if (isAnnotated(start)) continue
        val phone = text.substring(start, end)
        addStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline), start, end)
        addStringAnnotation("phone", phone, start, end)
    }
}

/**
 * Builds a simple AnnotatedString without link annotations (for non-interactive use).
 */
private fun buildSimpleAnnotatedString(
    html: String,
    textColor: Color,
): AnnotatedString {
    val spanned = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
    val text = spanned.toString().trimEnd()

    return buildAnnotatedString {
        append(text)

        for (span in spanned.getSpans(0, spanned.length, Any::class.java)) {
            val start = spanned.getSpanStart(span).coerceAtMost(text.length)
            val end = spanned.getSpanEnd(span).coerceAtMost(text.length)
            if (start >= end) continue

            when (span) {
                is StyleSpan -> when (span.style) {
                    android.graphics.Typeface.BOLD -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                    android.graphics.Typeface.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                    android.graphics.Typeface.BOLD_ITALIC -> addStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic), start, end)
                }
                is UnderlineSpan -> addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
            }
        }
    }
}
