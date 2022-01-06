package dev.anaes.qrh

import android.net.Uri
import android.os.Build
import android.text.*
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CardRecyclerAdapter(
    private var dataSource: ArrayList<DetailContent>,
    private val codePassed: String?,
    private val expandingDisabled: Boolean,
    private val linkListener: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface UpdateViewHolder {
        fun bindViews(detailContent: DetailContent, linkListener: (String) -> Unit)
    }

    //ViewHolder1 = all three text fields
    class ViewHolder1(itemView: View) : RecyclerView.ViewHolder(itemView),
        UpdateViewHolder {
        override fun bindViews(detailContent: DetailContent, linkListener: (String) -> Unit) {

            val headerTxt = itemView.findViewById<TextView>(R.id.detail_head)
            val bodyTxt = itemView.findViewById<TextView>(R.id.detail_body)

            headerTxt.text = htmlProcess(detailContent.head, itemView)

            bodyTxt.text = htmlProcess(detailContent.body, itemView)

            linkifyFunction(headerTxt)

            linkifyFunction(bodyTxt)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                bodyTxt.lineHeight =
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP,
                        18F,
                        itemView.resources.displayMetrics
                    ).toInt()
            }

            bodyTxt.movementMethod = object : TextViewLinkHandler() {
                override fun onLinkClick(url: String?) {
                    linkListener(url.toString())
                }
            }

            itemView.findViewById<TextView>(R.id.detail_step).text = detailContent.step
        }
    }

    //ViewHolder2 = collapsible boxes
    class ViewHolder2(itemView: View) : RecyclerView.ViewHolder(itemView),
        UpdateViewHolder {
        override fun bindViews(detailContent: DetailContent, linkListener: (String) -> Unit) {

            val cardBody = itemView.findViewById<CardView>(R.id.detail_card)
            val headerTxt = itemView.findViewById<TextView>(R.id.detail_head)
            val bodyTxt = itemView.findViewById<TextView>(R.id.detail_body)
            val subArrow = itemView.findViewById<ImageView>(R.id.detail_arrow)


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                bodyTxt.lineHeight =
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP,
                        18F,
                        itemView.resources.displayMetrics
                    ).toInt()
            }


            when (detailContent.type) {
                5 -> {
                    cardBody.setCardBackgroundColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.detailItemOrangeBG
                        )
                    )
                    headerTxt.setTextColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.detailItemOrangeTXT
                        )
                    )
                    subArrow.setBackgroundColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.detailItemOrangeARR
                        )
                    )
                }
                6 -> {
                    cardBody.setCardBackgroundColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.detailItemBlueBG
                        )
                    )
                    headerTxt.setTextColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.detailItemBlueTXT
                        )
                    )
                    subArrow.setBackgroundColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.detailItemBlueARR
                        )
                    )
                }
                7 -> {
                    cardBody.setCardBackgroundColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.detailItemGreenBG
                        )
                    )
                    headerTxt.setTextColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.detailItemGreenTXT
                        )
                    )
                    subArrow.setBackgroundColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.detailItemGreenARR
                        )
                    )
                }
                8 -> {
                    cardBody.setCardBackgroundColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.detailItemBlackBG
                        )
                    )
                    headerTxt.setTextColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.detailItemBlackTXT
                        )
                    )
                    subArrow.setBackgroundColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.detailItemBlackARR
                        )
                    )
                }
                9 -> {
                    cardBody.setCardBackgroundColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.detailItemPurpleBG
                        )
                    )
                    headerTxt.setTextColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.detailItemPurpleTXT
                        )
                    )
                    subArrow.setBackgroundColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.detailItemPurpleARR
                        )
                    )
                }
            }

            headerTxt.text = htmlProcess(detailContent.head, itemView)
            bodyTxt.text = htmlProcess(detailContent.body, itemView)

            linkifyFunction(bodyTxt)

            bodyTxt.movementMethod = object : TextViewLinkHandler() {
                override fun onLinkClick(url: String?) {
                    linkListener(url.toString())
                }
            }



            if (detailContent.collapsed) {
                bodyTxt.visibility = View.GONE
                subArrow.setImageResource(R.drawable.ic_arrow_down)
            } else {
                bodyTxt.visibility = View.VISIBLE
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
            itemView.findViewById<TextView>(R.id.detail_caption).setText(detailContent.head)
                .toString()
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
            itemView.findViewById<TextView>(R.id.detailText).text =
                htmlProcess(detailContent.body, itemView)
        }
    }

    //ViewHolder5 = end disclaimer card
    class ViewHolder5(itemView: View) : RecyclerView.ViewHolder(itemView),
        UpdateViewHolder {
        override fun bindViews(detailContent: DetailContent, linkListener: (String) -> Unit) {
            itemView.findViewById<TextView>(R.id.detailText).text =
                htmlProcess(detailContent.head, itemView)
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
                if(expandingDisabled) {
                    detailContent.collapsed = false
                    holder.itemView.findViewById<ImageView>(R.id.detail_arrow)?.let {
                        it.visibility = View.GONE
                    }
                } else {
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
    }
}



