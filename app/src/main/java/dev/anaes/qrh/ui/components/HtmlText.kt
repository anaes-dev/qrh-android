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
 * Note: Uses ClickableText which consumes touch events.
 * TODO: Migrate to LinkAnnotation API when stable.
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
    val textColor = resolveTextColor(style)

    val parsed = remember(html, linkColor, textColor) {
        parseHtml(html, linkColor)
    }

    val resolvedStyle = style.copy(color = textColor)

    if (parsed.bulletItems.isNotEmpty()) {
        Column(modifier = modifier) {
            parsed.preText?.let {
                ClickableTextBlock(it, resolvedStyle, onGuidelineLink, onExternalLink)
            }
            for (bulletItem in parsed.bulletItems) {
                Row(modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp)) {
                    Text("\u2022", style = resolvedStyle, modifier = Modifier.padding(end = 8.dp))
                    ClickableTextBlock(bulletItem, resolvedStyle, onGuidelineLink, onExternalLink, Modifier.weight(1f))
                }
            }
            parsed.postText?.let {
                ClickableTextBlock(it, resolvedStyle, onGuidelineLink, onExternalLink, Modifier.padding(top = 2.dp))
            }
        }
    } else {
        ClickableTextBlock(parsed.fullText, resolvedStyle, onGuidelineLink, onExternalLink, modifier)
    }
}

/**
 * Non-clickable HTML text. Does not consume touch events,
 * so parent clickable modifiers work properly.
 */
@Composable
fun HtmlTextStatic(
    html: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
) {
    val textColor = resolveTextColor(style)

    val annotated = remember(html) {
        val spanned = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        buildAnnotatedFromSpanned(spanned, 0, spanned.length, linkColor = null)
    }

    Text(text = annotated, modifier = modifier, style = style.copy(color = textColor))
}

@Composable
private fun resolveTextColor(style: TextStyle): Color =
    style.color.takeIf { it != Color.Unspecified } ?: LocalContentColor.current

@Composable
private fun ClickableTextBlock(
    annotated: AnnotatedString,
    style: TextStyle,
    onGuidelineLink: (String) -> Unit,
    onExternalLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    @Suppress("DEPRECATION")
    ClickableText(
        text = annotated,
        modifier = modifier,
        style = style,
        onClick = { offset ->
            annotated.getStringAnnotations("qrh", offset, offset).firstOrNull()?.let {
                onGuidelineLink(it.item); return@ClickableText
            }
            annotated.getStringAnnotations("url", offset, offset).firstOrNull()?.let {
                onExternalLink(it.item); return@ClickableText
            }
            annotated.getStringAnnotations("phone", offset, offset).firstOrNull()?.let {
                onExternalLink("tel:${it.item}"); return@ClickableText
            }
        }
    )
}

// --- Parsing ---

private data class ParsedHtml(
    val fullText: AnnotatedString,
    val bulletItems: List<AnnotatedString> = emptyList(),
    val preText: AnnotatedString? = null,
    val postText: AnnotatedString? = null,
)

private fun parseHtml(html: String, linkColor: Color): ParsedHtml {
    val spanned = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
    val bulletSpans = spanned.getSpans(0, spanned.length, BulletSpan::class.java)

    if (bulletSpans.isEmpty()) {
        return ParsedHtml(fullText = buildAnnotatedFromSpanned(spanned, 0, spanned.length, linkColor))
    }

    val raw = spanned.toString()
    val bulletRanges = bulletSpans.map { span ->
        spanned.getSpanStart(span) to spanned.getSpanEnd(span)
    }.sortedBy { it.first }

    val firstStart = bulletRanges.first().first
    val lastEnd = bulletRanges.last().second

    val preText = if (firstStart > 0 && raw.substring(0, firstStart).isNotBlank()) {
        buildAnnotatedFromSpanned(spanned, 0, firstStart, linkColor)
    } else null

    val items = bulletRanges.map { (start, end) ->
        buildAnnotatedFromSpanned(spanned, start, end, linkColor)
    }

    val postText = if (lastEnd < raw.length && raw.substring(lastEnd).isNotBlank()) {
        buildAnnotatedFromSpanned(spanned, lastEnd, raw.length, linkColor)
    } else null

    return ParsedHtml(
        fullText = buildAnnotatedFromSpanned(spanned, 0, spanned.length, linkColor),
        bulletItems = items,
        preText = preText,
        postText = postText,
    )
}

// --- Unified AnnotatedString builder ---

/**
 * Builds an AnnotatedString from a substring of a Spanned object.
 * @param linkColor If null, URL/link annotations are not added (static mode).
 */
private fun buildAnnotatedFromSpanned(
    spanned: Spanned,
    rangeStart: Int,
    rangeEnd: Int,
    linkColor: Color?,
): AnnotatedString {
    val raw = spanned.toString()
    val text = raw.substring(
        rangeStart.coerceIn(0, raw.length),
        rangeEnd.coerceIn(0, raw.length)
    ).trimEnd()
    if (text.isEmpty()) return AnnotatedString("")

    return buildAnnotatedString {
        append(text)

        // Apply Android spans
        for (span in spanned.getSpans(rangeStart, rangeEnd, Any::class.java)) {
            val start = (spanned.getSpanStart(span) - rangeStart).coerceIn(0, text.length)
            val end = (spanned.getSpanEnd(span) - rangeStart).coerceIn(0, text.length)
            if (start >= end) continue

            when (span) {
                is StyleSpan -> when (span.style) {
                    android.graphics.Typeface.BOLD ->
                        addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                    android.graphics.Typeface.ITALIC ->
                        addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                    android.graphics.Typeface.BOLD_ITALIC ->
                        addStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic), start, end)
                }
                is UnderlineSpan ->
                    addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
                is URLSpan -> if (linkColor != null) {
                    addStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline), start, end)
                    addStringAnnotation("url", span.url, start, end)
                }
            }
        }

        // Auto-detect links (only in interactive mode)
        if (linkColor != null) {
            addAutoLinks(text, linkColor, spanned, rangeStart)
        }
    }
}

private fun AnnotatedString.Builder.addAutoLinks(
    text: String,
    linkColor: Color,
    spanned: Spanned,
    baseOffset: Int,
) {
    val annotatedRanges = mutableListOf<IntRange>()

    for (span in spanned.getSpans(0, spanned.length, URLSpan::class.java)) {
        val s = (spanned.getSpanStart(span) - baseOffset).coerceIn(0, text.length)
        val e = (spanned.getSpanEnd(span) - baseOffset).coerceIn(0, text.length)
        if (s < e) annotatedRanges.add(s..e)
    }

    fun isAnnotated(pos: Int) = annotatedRanges.any { pos in it }

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
