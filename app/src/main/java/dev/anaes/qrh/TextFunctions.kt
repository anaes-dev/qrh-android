package dev.anaes.qrh

import android.os.Build
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.BulletSpan
import android.text.style.URLSpan
import android.text.util.Linkify
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import java.util.regex.Pattern

fun linkifyFunction(textView: TextView) {
    //Excuse fudge for MHRA website
    val patternURL = Pattern.compile(
        "((ht|f)tp(s?):\\/\\/|www\\.|yellowcard\\.)" + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
        Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL
    )

    val patternPhone = Pattern.compile("[0][0-9]{10}")

    val patternGuideline = Pattern.compile("[(]?[→][\\s]?[1-4][-][0-9]{1,2}[)]?")

    val linkGuideline = "qrh://"

    val transformFilter =
        Linkify.TransformFilter { _, url ->
            url.toString().replace("→ ", "").replace("→", "").replace(")", "").replace(
                "(",
                ""
            )
        }

    Linkify.addLinks(textView, patternURL, "http://")
    Linkify.addLinks(textView, patternPhone, "tel:")
    Linkify.addLinks(textView, patternGuideline, linkGuideline, null, transformFilter)
}

@Suppress("DEPRECATION")
fun htmlProcess(text: String, view: View): CharSequence {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val parseSpans =
            SpannableStringBuilder(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).trim())
        val bulletSpans = parseSpans.getSpans(0, parseSpans.length, BulletSpan::class.java)
        bulletSpans.forEach {
            val start = parseSpans.getSpanStart(it)
            val end = parseSpans.getSpanEnd(it)
            parseSpans.removeSpan(it)
            parseSpans.setSpan(
                ImprovedBullet(bulletRadius = dip(2.0, view), gapWidth = dip(7.5, view)),
                start,
                end,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }
        return parseSpans
    } else {
        val paraStrippedText = text.replace("<p>", "<br /><br />").replace("</p>", "")
        return Html.fromHtml(paraStrippedText, null,
            { opening, tag, output, _ ->
                if (tag == "br" && opening) output.append("\n")
                if (tag == "li" && opening) output.append("\n\n• ")
            }).trim()
    }
}

private fun dip(dp: Double, view: View): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        view.resources.displayMetrics
    ).toInt()
}

abstract class TextViewLinkHandler : LinkMovementMethod() {
    override fun onTouchEvent(
        widget: TextView,
        buffer: Spannable,
        event: MotionEvent
    ): Boolean {
        if (event.action != MotionEvent.ACTION_UP) return super.onTouchEvent(
            widget,
            buffer,
            event
        )
        var x = event.x.toInt()
        var y = event.y.toInt()
        x -= widget.totalPaddingLeft
        y -= widget.totalPaddingTop
        x += widget.scrollX
        y += widget.scrollY
        val layout: Layout = widget.layout
        val line: Int = layout.getLineForVertical(y)
        val off: Int = layout.getOffsetForHorizontal(line, x.toFloat())
        val link = buffer.getSpans(off, off, URLSpan::class.java)
        if (link.isNotEmpty()) {
            onLinkClick(link[0].url)
        }
        return true
    }

    abstract fun onLinkClick(url: String?)
}

