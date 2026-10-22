package dev.anaes.qrh.ui.components

import android.text.Html
import android.text.Spanned
import android.text.style.BulletSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.text.htmlEncode
import dev.anaes.qrh.search.SearchTerm
import dev.anaes.qrh.search.highlightRanges
import dev.anaes.qrh.ui.theme.LocalIsDarkTheme
import dev.anaes.qrh.ui.theme.SearchHighlightDark
import dev.anaes.qrh.ui.theme.SearchHighlightLight
import java.util.regex.Pattern

private val URL_PATTERN = Pattern.compile(
    "((ht|f)tp(s?)://|www\\.|yellowcard\\.)" +
        "([\\w\\-]+\\.){1,}?([\\w\\-.~]+/?)*" +
        "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]*\$~@!:/{};']*" +
        // Bare github.com paths (e.g. the source code link in the disclaimers)
        "|github\\.com/[\\w\\-./]*[\\w\\-/]",
    Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL
)
/**
 * Words from the search that opened the current guideline, highlighted wherever [HtmlText] and
 * [HtmlTextStatic] draw them (as qrh-ios does). Empty outside a guideline opened from search.
 */
val LocalSearchHighlight = staticCompositionLocalOf { emptyList<SearchTerm>() }

private val PHONE_PATTERN = Pattern.compile("[0][0-9]{10}")
private val GUIDELINE_PATTERN = Pattern.compile("[(]?[→][\\s]?[1-4][-][0-9]{1,2}[)]?")

/**
 * Renders HTML text with clickable links for guidelines, URLs, and phone numbers.
 * Uses LinkAnnotation API with standard Text composable — only link areas consume touches.
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
    val highlight = LocalSearchHighlight.current
    val highlightColor = searchHighlightColor()

    // Use rememberUpdatedState so cached AnnotatedStrings always invoke the latest callbacks
    val currentOnGuidelineLink = rememberUpdatedState(onGuidelineLink)
    val currentOnExternalLink = rememberUpdatedState(onExternalLink)

    val parsed = remember(html, linkColor, textColor, highlight, highlightColor) {
        parseHtml(
            html = html,
            linkColor = linkColor,
            onGuidelineLink = { code -> currentOnGuidelineLink.value(code) },
            onExternalLink = { url -> currentOnExternalLink.value(url) },
        ).highlighted(highlight, highlightColor)
    }

    val resolvedStyle = style.copy(color = textColor)

    if (parsed.bulletItems.isNotEmpty()) {
        Column(modifier = modifier) {
            parsed.preText?.let {
                Text(text = it, style = resolvedStyle)
            }
            for (bulletItem in parsed.bulletItems) {
                Row(modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp)) {
                    Text("\u2022", style = resolvedStyle, modifier = Modifier.padding(end = 8.dp))
                    Text(text = bulletItem, style = resolvedStyle, modifier = Modifier.weight(1f))
                }
            }
            parsed.postText?.let {
                Text(text = it, style = resolvedStyle, modifier = Modifier.padding(top = 2.dp))
            }
        }
    } else {
        Text(text = parsed.fullText, style = resolvedStyle, modifier = modifier)
    }
}

/**
 * Non-clickable HTML text. Does not add link annotations,
 * so parent clickable modifiers work properly.
 */
@Composable
fun HtmlTextStatic(
    html: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
) {
    val textColor = resolveTextColor(style)
    val highlight = LocalSearchHighlight.current
    val highlightColor = searchHighlightColor()

    val annotated = remember(html, highlight, highlightColor) {
        val spanned = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        buildAnnotatedFromSpanned(spanned, 0, spanned.length, linkColor = null)
            .highlighted(highlight, highlightColor)
    }

    Text(text = annotated, modifier = modifier, style = style.copy(color = textColor))
}

/**
 * Loads a string resource as HTML for [HtmlText], keeping its inline styling.
 *
 * `stringResource()` returns plain text, silently dropping any `<b>`, `<i>` or `<u>` in
 * strings.xml; this reads the styled text and converts it back to HTML (newlines become
 * paragraph and line breaks).
 */
@Composable
fun htmlStringResource(@StringRes id: Int): String {
    val resources = LocalResources.current
    return remember(id, resources) {
        when (val text = resources.getText(id)) {
            is Spanned -> Html.toHtml(text, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
            else -> text.toString().htmlEncode().replace("\n", "<br>")
        }
    }
}

@Composable
private fun resolveTextColor(style: TextStyle): Color =
    style.color.takeIf { it != Color.Unspecified } ?: LocalContentColor.current

@Composable
private fun searchHighlightColor(): Color =
    if (LocalIsDarkTheme.current) SearchHighlightDark else SearchHighlightLight

// --- Search highlighting ---

private fun AnnotatedString.highlighted(terms: List<SearchTerm>, color: Color): AnnotatedString {
    val ranges = highlightRanges(text, terms)
    if (ranges.isEmpty()) return this
    return buildAnnotatedString {
        append(this@highlighted)
        for (range in ranges) addStyle(SpanStyle(background = color), range.first, range.last + 1)
    }
}

private fun ParsedHtml.highlighted(terms: List<SearchTerm>, color: Color): ParsedHtml {
    if (terms.isEmpty()) return this
    return ParsedHtml(
        fullText = fullText.highlighted(terms, color),
        bulletItems = bulletItems.map { it.highlighted(terms, color) },
        preText = preText?.highlighted(terms, color),
        postText = postText?.highlighted(terms, color),
    )
}

// --- Parsing ---

private data class ParsedHtml(
    val fullText: AnnotatedString,
    val bulletItems: List<AnnotatedString> = emptyList(),
    val preText: AnnotatedString? = null,
    val postText: AnnotatedString? = null,
)

private fun parseHtml(
    html: String,
    linkColor: Color,
    onGuidelineLink: (String) -> Unit,
    onExternalLink: (String) -> Unit,
): ParsedHtml {
    val spanned = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
    val bulletSpans = spanned.getSpans(0, spanned.length, BulletSpan::class.java)

    if (bulletSpans.isEmpty()) {
        return ParsedHtml(
            fullText = buildAnnotatedFromSpanned(spanned, 0, spanned.length, linkColor, onGuidelineLink, onExternalLink)
        )
    }

    val raw = spanned.toString()
    val bulletRanges = bulletSpans.map { span ->
        spanned.getSpanStart(span) to spanned.getSpanEnd(span)
    }.sortedBy { it.first }

    val firstStart = bulletRanges.first().first
    val lastEnd = bulletRanges.last().second

    val preText = if (firstStart > 0 && raw.substring(0, firstStart).isNotBlank()) {
        buildAnnotatedFromSpanned(spanned, 0, firstStart, linkColor, onGuidelineLink, onExternalLink)
    } else null

    val items = bulletRanges.map { (start, end) ->
        buildAnnotatedFromSpanned(spanned, start, end, linkColor, onGuidelineLink, onExternalLink)
    }

    val postText = if (lastEnd < raw.length && raw.substring(lastEnd).isNotBlank()) {
        buildAnnotatedFromSpanned(spanned, lastEnd, raw.length, linkColor, onGuidelineLink, onExternalLink)
    } else null

    return ParsedHtml(
        fullText = buildAnnotatedFromSpanned(spanned, 0, spanned.length, linkColor, onGuidelineLink, onExternalLink),
        bulletItems = items,
        preText = preText,
        postText = postText,
    )
}

// --- Unified AnnotatedString builder ---

/**
 * Builds an AnnotatedString from a substring of a Spanned object.
 * @param linkColor If null, link annotations are not added (static mode).
 * @param onGuidelineLink Callback for guideline cross-references (only used when linkColor != null).
 * @param onExternalLink Callback for URLs and phone numbers (only used when linkColor != null).
 */
private fun buildAnnotatedFromSpanned(
    spanned: Spanned,
    rangeStart: Int,
    rangeEnd: Int,
    linkColor: Color?,
    onGuidelineLink: ((String) -> Unit)? = null,
    onExternalLink: ((String) -> Unit)? = null,
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
                is URLSpan -> if (linkColor != null && onExternalLink != null) {
                    val url = span.url
                    addLink(
                        LinkAnnotation.Clickable(
                            tag = "url",
                            styles = TextLinkStyles(
                                style = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline),
                            ),
                            linkInteractionListener = { onExternalLink(url) },
                        ),
                        start,
                        end,
                    )
                }
            }
        }

        // Auto-detect links (only in interactive mode)
        if (linkColor != null && onGuidelineLink != null && onExternalLink != null) {
            addAutoLinks(text, linkColor, spanned, rangeStart, onGuidelineLink, onExternalLink)
        }
    }
}

private fun AnnotatedString.Builder.addAutoLinks(
    text: String,
    linkColor: Color,
    spanned: Spanned,
    baseOffset: Int,
    onGuidelineLink: (String) -> Unit,
    onExternalLink: (String) -> Unit,
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
        if (isAnnotated(start)) continue
        val matchText = text.substring(start, end)
        val code = matchText.replace("\u2192 ", "").replace("\u2192", "")
            .replace("(", "").replace(")", "").trim()
        addLink(
            LinkAnnotation.Clickable(
                tag = "qrh",
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = linkColor,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                    ),
                ),
                linkInteractionListener = { onGuidelineLink(code) },
            ),
            start,
            end,
        )
        annotatedRanges.add(start..end)
    }

    val urlMatcher = URL_PATTERN.matcher(text)
    while (urlMatcher.find()) {
        val start = urlMatcher.start()
        val end = urlMatcher.end()
        if (isAnnotated(start)) continue
        val url = text.substring(start, end)
        val fullUrl = if (url.startsWith("http")) url else "http://$url"
        addLink(
            LinkAnnotation.Clickable(
                tag = "url",
                styles = TextLinkStyles(
                    style = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline),
                ),
                linkInteractionListener = { onExternalLink(fullUrl) },
            ),
            start,
            end,
        )
        annotatedRanges.add(start..end)
    }

    val phoneMatcher = PHONE_PATTERN.matcher(text)
    while (phoneMatcher.find()) {
        val start = phoneMatcher.start()
        val end = phoneMatcher.end()
        if (isAnnotated(start)) continue
        val phone = text.substring(start, end)
        addLink(
            LinkAnnotation.Clickable(
                tag = "phone",
                styles = TextLinkStyles(
                    style = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline),
                ),
                linkInteractionListener = { onExternalLink("tel:$phone") },
            ),
            start,
            end,
        )
    }
}
