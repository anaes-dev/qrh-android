package dev.anaes.qrh

import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.BulletSpan
import android.text.style.URLSpan
import android.text.util.Linkify
import android.text.util.Linkify.TransformFilter
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.util.regex.Pattern

class CardRecyclerAdapter(
    private var dataSource: ArrayList<DetailContent>,
    private val codePassed: String?,
    private val linkListener: (String) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface UpdateViewHolder {
        fun bindViews(detailContent: DetailContent, linkListener: (String) -> Unit)
    }

    //ViewHolder1 = all three text fields
    class ViewHolder1(itemView: View) : RecyclerView.ViewHolder(itemView),
        UpdateViewHolder {
        override fun bindViews(detailContent: DetailContent, linkListener: (String) -> Unit) {

            this.itemView.findViewById<TextView>(R.id.detail_head).text = htmlProcess(detailContent.head, itemView)

            this.itemView.findViewById<TextView>(R.id.detail_body).text = htmlProcess(detailContent.body, itemView)

            linkifyFunction(
                itemView.findViewById(
                    R.id.detail_body
                )
            )

            linkifyFunction(
                itemView.findViewById(
                    R.id.detail_head
                )
            )

            val tv = itemView.findViewById<TextView>(R.id.detail_body)

            tv.movementMethod = object : TextViewLinkHandler() {
                override fun onLinkClick(url: String?) {
                    linkListener(url.toString())
                }
            }


            itemView.findViewById<TextView>(R.id.detail_step).setText(detailContent.step).toString()
        }
    }

    //ViewHolder2 = collapsible boxes
    class ViewHolder2(itemView: View) : RecyclerView.ViewHolder(itemView),
        UpdateViewHolder {
        override fun bindViews(detailContent: DetailContent, linkListener: (String) -> Unit) {

            when (detailContent.type) {
                5 -> {
                    itemView.findViewById<CardView>(R.id.detail_card).setCardBackgroundColor(
                        Color.parseColor(
                            "#FBE9E7"
                        )
                    )
                    itemView.findViewById<TextView>(R.id.detail_head).setTextColor(
                        Color.parseColor(
                            "#E64A19"
                        )
                    )
                    itemView.findViewById<ImageView>(R.id.detail_arrow).setBackgroundColor(
                        Color.parseColor(
                            "#FFCCBC"
                        )
                    )
                }
                6 -> {
                    itemView.findViewById<CardView>(R.id.detail_card).setCardBackgroundColor(
                        Color.parseColor(
                            "#E1F5FE"
                        )
                    )
                    itemView.findViewById<TextView>(R.id.detail_head).setTextColor(
                        Color.parseColor(
                            "#1976D2"
                        )
                    )
                    itemView.findViewById<ImageView>(R.id.detail_arrow).setBackgroundColor(
                        Color.parseColor(
                            "#BBDEFB"
                        )
                    )
                }
                7 -> {
                    itemView.findViewById<CardView>(R.id.detail_card).setCardBackgroundColor(
                        Color.parseColor(
                            "#E8F5E9"
                        )
                    )
                    itemView.findViewById<TextView>(R.id.detail_head).setTextColor(
                        Color.parseColor(
                            "#388E3C"
                        )
                    )
                    itemView.findViewById<ImageView>(R.id.detail_arrow).setBackgroundColor(
                        Color.parseColor(
                            "#C8E6C9"
                        )
                    )
                }
                8 -> {
                    itemView.findViewById<CardView>(R.id.detail_card).setCardBackgroundColor(
                        Color.parseColor(
                            "#EDEDED"
                        )
                    )
                    itemView.findViewById<TextView>(R.id.detail_head).setTextColor(
                        Color.parseColor(
                            "#000000"
                        )
                    )
                    itemView.findViewById<ImageView>(R.id.detail_arrow).setBackgroundColor(
                        Color.parseColor(
                            "#D1D1D1"
                        )
                    )
                }
                9 -> {
                    itemView.findViewById<CardView>(R.id.detail_card).setCardBackgroundColor(
                        Color.parseColor(
                            "#EDE7F6"
                        )
                    )
                    itemView.findViewById<TextView>(R.id.detail_head).setTextColor(
                        Color.parseColor(
                            "#7B1FA2"
                        )
                    )
                    itemView.findViewById<ImageView>(R.id.detail_arrow).setBackgroundColor(
                        Color.parseColor(
                            "#D1C4E9"
                        )
                    )
                }
            }

            this.itemView.findViewById<TextView>(R.id.detail_head).text = htmlProcess(detailContent.head, itemView)

            this.itemView.findViewById<TextView>(R.id.detail_body).text = htmlProcess(detailContent.body, itemView)

            linkifyFunction(
                itemView.findViewById(
                    R.id.detail_body
                )
            )

            val tv = itemView.findViewById<TextView>(R.id.detail_body)

            tv.movementMethod = object : TextViewLinkHandler() {
                override fun onLinkClick(url: String?) {
                    linkListener(url.toString())
                }
            }

            val subCard = itemView.findViewById<TextView>(R.id.detail_body)
            val subArrow = itemView.findViewById<ImageView>(R.id.detail_arrow)

            if(detailContent.collapsed) {
                subCard.visibility = View.GONE
                subArrow.setImageResource(R.drawable.ic_arrow_down)
            } else {
                subCard.visibility = View.VISIBLE
                subArrow.setImageResource(R.drawable.ic_arrow_up)
            }

        }
    }

    //ViewHolder3 = images with glide (put path in 'main' in JSON)
    class ViewHolder3(itemView: View) : RecyclerView.ViewHolder(itemView),
        UpdateViewHolder {
        override fun bindViews(detailContent: DetailContent, linkListener: (String) -> Unit) {
            val detailImage = itemView.findViewById<ImageView>(R.id.detail_image)
            val imagePath = detailContent.body
            itemView.findViewById<TextView>(R.id.detail_caption).setText(detailContent.head).toString()
            Glide
                .with(itemView)
                .load(Uri.parse("file:///android_asset/$imagePath"))
                .into(detailImage)
        }
    }

    //ViewHolder4 = single text item
    class ViewHolder4(itemView: View) : RecyclerView.ViewHolder(itemView),
        UpdateViewHolder {
        override fun bindViews(detailContent: DetailContent, linkListener: (String) -> Unit) {
            itemView.findViewById<TextView>(R.id.detail_text).text = (Html.fromHtml(detailContent.body)).trim()
        }
    }

    //ViewHolder5 = end disclaimer card
    class ViewHolder5(itemView: View) : RecyclerView.ViewHolder(itemView),
        UpdateViewHolder {
        override fun bindViews(detailContent: DetailContent, linkListener: (String) -> Unit) {
            itemView.findViewById<TextView>(R.id.detail_text).text = (Html.fromHtml(detailContent.head)).trim()
        }
    }


    //Get view type
    override fun getItemViewType(position: Int): Int {
        return when (dataSource[position].type) {
            1 -> VIEW_ONE
            2 -> VIEW_TWO
            3 -> VIEW_THREE
            4 -> VIEW_FOUR
            5 -> VIEW_FIVE
            6 -> VIEW_SIX
            7 -> VIEW_SEVEN
            8 -> VIEW_EIGHT
            9 -> VIEW_NINE
            10 -> VIEW_TEN
            11 -> VIEW_ELEVEN
            12 -> VIEW_TWELVE
            else -> VIEW_THREE
            }
    }

    //Inflate item layout according to view type
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_ONE -> ViewHolder1(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.detail_item_preamble, parent, false)
            )
            VIEW_TWO -> ViewHolder4(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.detail_item_start, parent, false)
            )
            VIEW_THREE -> ViewHolder1(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.detail_item_standard, parent, false)
            )
            VIEW_FOUR -> ViewHolder1(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.detail_item_standard_oneline, parent, false)
            )
            VIEW_FIVE, VIEW_SIX, VIEW_SEVEN, VIEW_EIGHT, VIEW_NINE -> ViewHolder2(
                LayoutInflater.from(parent.context).inflate(R.layout.detail_item, parent, false)
            )
            VIEW_TEN -> ViewHolder3(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.detail_item_image, parent, false)
            )
            VIEW_ELEVEN -> ViewHolder4(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.detail_item_version, parent, false)
            )
            VIEW_TWELVE -> ViewHolder5(
                LayoutInflater.from(parent.context).inflate(R.layout.detail_item_end, parent, false)
            )
            else -> ViewHolder1(
                LayoutInflater.from(
                    parent.context
                ).inflate(R.layout.detail_item_standard, parent, false)
            )
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val detailContent: DetailContent = dataSource[position]

        //Collapsing box logic
        when (dataSource[position].type) {
            5, 6, 7, 8, 9 -> {
                holder.itemView.setOnClickListener {
                    if (detailContent.collapsed) {
                        detailContent.collapsed = false
                        notifyItemChanged(position)
                    } else {
                        detailContent.collapsed = true
                        notifyItemChanged(position)
                    }
                }
            }
        }

        //Override first box on instructions page to be expanded
        if (codePassed == "0-3" && dataSource[position].type == 5) {
            detailContent.collapsed = false
        }

        (holder as UpdateViewHolder).bindViews(detailContent, linkListener)
    }


    override fun getItemCount(): Int {
        return dataSource.size
    }

    //Define view type codes
    companion object {

        //Define view type codes

        const val VIEW_ONE = 1
        const val VIEW_TWO = 2
        const val VIEW_THREE = 3
        const val VIEW_FOUR = 4
        const val VIEW_FIVE = 5
        const val VIEW_SIX = 6
        const val VIEW_SEVEN = 7
        const val VIEW_EIGHT = 8
        const val VIEW_NINE = 9
        const val VIEW_TEN = 10
        const val VIEW_ELEVEN = 11
        const val VIEW_TWELVE = 12


        //Linkify Function

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
                TransformFilter { _, url ->
                    url.toString().replace("→ ", "").replace("→", "").replace(")", "").replace(
                        "(",
                        ""
                    )
                }

            Linkify.addLinks(textView, patternURL, "http://")
            Linkify.addLinks(textView, patternPhone, "tel:")
            Linkify.addLinks(textView, patternGuideline, linkGuideline, null, transformFilter)
        }

        class Bullet

        @Suppress("DEPRECATION")
        fun htmlProcess(text: String, view: View) : CharSequence {
            val parseHtml = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).trim()
            } else {
                Html.fromHtml(text, null,
                    { opening, tag, output, _ ->
                        if (tag == "li" && opening) {
                            output.setSpan(Bullet(), output.length, output.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                        }
                        if (tag == "li" && !opening) {
                            output.append("\n\n")
                            val lastMark = output.getSpans(0, output.length, Bullet::class.java).lastOrNull()
                            lastMark?.let {
                                val start = output.getSpanStart(it)
                                output.removeSpan(it)
                                if (start<output.length) {
                                    output.setSpan(BulletSpan(), start, output.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                                }
                            }
                        }
                    }).trim()
            }

            val parseSpans = SpannableStringBuilder(parseHtml)

            val bulletSpans = parseSpans.getSpans(0, parseSpans.length, BulletSpan::class.java)

            bulletSpans.forEach {
                val start = parseSpans.getSpanStart(it)
                val end = parseSpans.getSpanEnd(it)
                parseSpans.removeSpan(it)
                parseSpans.setSpan(
                    ImprovedBullet(bulletRadius = dip(3, view), gapWidth = dip(8, view)),
                    start,
                    end,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                )
            }

            return parseSpans
        }

        private fun dip(dp: Int, view: View): Int {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), view.resources.displayMetrics).toInt()
        }
    }
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


