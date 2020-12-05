package dev.anaes.qrh

import android.os.Build
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BulletSpan
import android.util.TypedValue
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlin.coroutines.coroutineContext

class SwipeAdapter(
    fragment: Fragment,
    private var dataSource: ArrayList<DetailContent>
    ) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {

        var count: Int = 0
        for(Guideline in dataSource) {
            if(Guideline.type != 12) {
                count++
            }
        }


        return count
    }


    override fun createFragment(position: Int): Fragment {
        return SwipeItemStandard(
            dataSource[position].step,
            dataSource[position].head,
            dataSource[position].body,
            dataSource[position].type
        )
    }

    companion object {


        @Suppress("DEPRECATION")
    fun htmlProcess(text: String, view: View) : CharSequence {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val parseSpans = SpannableStringBuilder(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).trim())
            val bulletSpans = parseSpans.getSpans(0, parseSpans.length, BulletSpan::class.java)
            bulletSpans.forEach {
                val start = parseSpans.getSpanStart(it)
                val end = parseSpans.getSpanEnd(it)
                parseSpans.removeSpan(it)
                parseSpans.setSpan(
                    ImprovedBullet(bulletRadius = dip(
                        2.0,
                        view
                    ), gapWidth = dip(7.5, view)
                    ),
                    start,
                    end,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                )
            }
            return parseSpans
        } else {
            val paraStrippedText = text.replace("<p>","<br /><br />").replace("</p>","")
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

    }
}