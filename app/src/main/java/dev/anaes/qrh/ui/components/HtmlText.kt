package dev.anaes.qrh.ui.components

import android.os.Build
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BulletSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
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
import java.util.regex.Pattern

private val URL_PATTERN = Pattern.compile(
    "((ht|f)tp(s?)://|www\\.|yellowcard\\.)" +
        "([\\w\\-]+\\.){1,}?([\\w\\-.~]+/?)*" +
        "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]*\$~@!:/{};']*",
    Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL
)
private val PHONE_PATTERN = Pattern.compile("[0][0-9]{10}")
private val GUIDELINE_PATTERN = Pattern.compile("[(]?[→][\\s]?[1-4][-][0-9]{1,2}[)]?")

@Composable
fun HtmlText(
    html: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    onGuidelineLink: (code: String) -> Unit = {},
    onExternalLink: (url: String) -> Unit = {},
) {
    val linkColor = MaterialTheme.colorScheme.secondary
    val textColor = LocalContentColor.current

    val annotated = remember(html, linkColor, textColor) {
        buildHtmlAnnotatedString(html, linkColor, textColor)
    }

    ClickableText(
        text = annotated,
        modifier = modifier,
        style = style.copy(color = textColor),
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

private fun buildHtmlAnnotatedString(
    html: String,
    linkColor: Color,
    textColor: Color,
): AnnotatedString {
    val spanned = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
    val text = spanned.toString().trimEnd()

    return buildAnnotatedString {
        append(text)

        // Convert Android spans to Compose SpanStyles
        if (spanned is Spanned) {
            for (span in spanned.getSpans(0, spanned.length, Any::class.java)) {
                val start = spanned.getSpanStart(span).coerceAtMost(text.length)
                val end = spanned.getSpanEnd(span).coerceAtMost(text.length)
                if (start >= end) continue

                when (span) {
                    is StyleSpan -> when (span.style) {
                        android.graphics.Typeface.BOLD -> addStyle(
                            SpanStyle(fontWeight = FontWeight.Bold), start, end
                        )
                        android.graphics.Typeface.ITALIC -> addStyle(
                            SpanStyle(fontStyle = FontStyle.Italic), start, end
                        )
                        android.graphics.Typeface.BOLD_ITALIC -> addStyle(
                            SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic),
                            start, end
                        )
                    }
                    is UnderlineSpan -> addStyle(
                        SpanStyle(textDecoration = TextDecoration.Underline), start, end
                    )
                    is URLSpan -> {
                        val url = span.url
                        addStyle(
                            SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline),
                            start, end
                        )
                        addStringAnnotation("url", url, start, end)
                    }
                    is BulletSpan -> {
                        // Bullet points — prefix with bullet character
                        // The HTML parser creates BulletSpan for <li> elements
                    }
                }
            }

            // Handle bullet spans: find lines that were list items and prefix with bullet
            val bulletSpans = spanned.getSpans(0, spanned.length, BulletSpan::class.java)
            // Bullets are already in the text from HTML parsing, just style them
        }

        // Auto-detect guideline cross-references: → X-YY patterns
        val guidelineMatcher = GUIDELINE_PATTERN.matcher(text)
        while (guidelineMatcher.find()) {
            val start = guidelineMatcher.start()
            val end = guidelineMatcher.end()
            val matchText = text.substring(start, end)
            val code = matchText.replace("→ ", "").replace("→", "")
                .replace("(", "").replace(")", "").trim()
            addStyle(
                SpanStyle(color = linkColor, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline),
                start, end
            )
            addStringAnnotation("qrh", code, start, end)
        }

        // Auto-detect URLs not already linked
        val urlMatcher = URL_PATTERN.matcher(text)
        while (urlMatcher.find()) {
            val start = urlMatcher.start()
            val end = urlMatcher.end()
            // Skip if already annotated (from <a href>)
            if (getStringAnnotations("url", start, start).isNotEmpty()) continue
            if (getStringAnnotations("qrh", start, start).isNotEmpty()) continue
            val url = text.substring(start, end)
            val fullUrl = if (url.startsWith("http")) url else "http://$url"
            addStyle(
                SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline),
                start, end
            )
            addStringAnnotation("url", fullUrl, start, end)
        }

        // Auto-detect phone numbers
        val phoneMatcher = PHONE_PATTERN.matcher(text)
        while (phoneMatcher.find()) {
            val start = phoneMatcher.start()
            val end = phoneMatcher.end()
            if (getStringAnnotations("url", start, start).isNotEmpty()) continue
            val phone = text.substring(start, end)
            addStyle(
                SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline),
                start, end
            )
            addStringAnnotation("phone", phone, start, end)
        }
    }
}
