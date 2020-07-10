package com.mttrnd.qrh

import android.net.Uri
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

@Suppress("DEPRECATION")
class CardRecyclerAdapter(var dataSource: ArrayList<DetailContent>, val codePassed: String?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    //Define view type codes
    companion object {
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

    interface UpdateViewHolder {
        fun bindViews(detailContent: DetailContent)
    }

    //ViewHolder1 = all three text fields
    class ViewHolder1(itemView: View) : RecyclerView.ViewHolder(itemView), UpdateViewHolder {
        override fun bindViews(detailContent: DetailContent) {

            itemView.findViewById<TextView>(R.id.detail_main).text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(detailContent.main).trim()
            } else {
                Html.fromHtml(detailContent.main, null, BulletHandler()).trim()
            }

            itemView.findViewById<TextView>(R.id.detail_sub).text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(detailContent.sub).trim()
            } else {
                Html.fromHtml(detailContent.sub, null, BulletHandler()).trim()
            }

            itemView.findViewById<TextView>(R.id.detail_step).setText(detailContent.step).toString()

        }
    }

    //ViewHolder2 = collapsible boxes
    class ViewHolder2(itemView: View) : RecyclerView.ViewHolder(itemView), UpdateViewHolder {
        override fun bindViews(detailContent: DetailContent) {
            itemView.findViewById<TextView>(R.id.detail_main).text =
                Html.fromHtml((detailContent.main)).trim()
            itemView.findViewById<TextView>(R.id.detail_sub).text =
                Html.fromHtml((detailContent.sub)).trim()

            val subCard = itemView.findViewById<TextView>(R.id.detail_sub)
            val subArrow = itemView.findViewById<ImageView>(R.id.detail_arrow)

            if(detailContent.collapsed) {
                subCard.visibility = View.GONE
                subArrow.setImageResource(R.drawable.ic_arrow_left)
            } else {
                subCard.visibility = View.VISIBLE
                subArrow.setImageResource(R.drawable.ic_arrow_down)
            }

        }
    }

    //ViewHolder3 = images with glide (put path in 'main' in JSON)
    class ViewHolder3(itemView: View) : RecyclerView.ViewHolder(itemView), UpdateViewHolder {
        override fun bindViews(detailContent: DetailContent) {
            val detailImage = itemView.findViewById<ImageView>(R.id.detail_image)
            itemView.findViewById<TextView>(R.id.detail_caption).setText(detailContent.sub).toString()
            Glide
                .with(itemView)
                .load(Uri.parse(detailContent.main))
                .into(detailImage)
        }
    }

    //ViewHolder4 = single text item
    class ViewHolder4(itemView: View) : RecyclerView.ViewHolder(itemView), UpdateViewHolder {
        override fun bindViews(detailContent: DetailContent) {
            itemView.findViewById<TextView>(R.id.detail_text).setText((Html.fromHtml(detailContent.main)).trim())
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
            VIEW_ONE -> ViewHolder1(LayoutInflater.from(parent.context).inflate(R.layout.detail_item_preamble, parent, false))
            VIEW_TWO -> ViewHolder4(LayoutInflater.from(parent.context).inflate(R.layout.detail_item_start, parent, false))
            VIEW_THREE -> ViewHolder1(LayoutInflater.from(parent.context).inflate(R.layout.detail_item_standard, parent, false))
            VIEW_FOUR -> ViewHolder1(LayoutInflater.from(parent.context).inflate(R.layout.detail_item_standard_oneline, parent, false))
            VIEW_FIVE -> ViewHolder2(LayoutInflater.from(parent.context).inflate(R.layout.detail_item_orange, parent, false))
            VIEW_SIX -> ViewHolder2(LayoutInflater.from(parent.context).inflate(R.layout.detail_item_blue, parent, false))
            VIEW_SEVEN -> ViewHolder2(LayoutInflater.from(parent.context).inflate(R.layout.detail_item_green, parent, false))
            VIEW_EIGHT -> ViewHolder2(LayoutInflater.from(parent.context).inflate(R.layout.detail_item_black, parent, false))
            VIEW_NINE -> ViewHolder2(LayoutInflater.from(parent.context).inflate(R.layout.detail_item_purple, parent, false))
            VIEW_TEN -> ViewHolder3(LayoutInflater.from(parent.context).inflate(R.layout.detail_item_image, parent, false))
            VIEW_ELEVEN -> ViewHolder4(LayoutInflater.from(parent.context).inflate(R.layout.detail_item_version, parent, false))
            VIEW_TWELVE -> ViewHolder4(LayoutInflater.from(parent.context).inflate(R.layout.detail_item_end, parent, false))
            else ->  ViewHolder1(LayoutInflater.from(parent.context).inflate(R.layout.detail_item_standard, parent, false))
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val detailContent: DetailContent = dataSource[position]

        //Collapsing box logic
        when (dataSource[position].type) {
            5,6,7,8,9 -> {
                holder.itemView.setOnClickListener {
                    if(detailContent.collapsed) {
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
        if(codePassed == "0-3" && dataSource[position].type == 5) {
            detailContent.collapsed = false
        }

        //Override autotext for sepsis page (parses ml.kg as URL)
        if(codePassed == "3-14") {
            when (dataSource[position].type) {
                5,6,7,8,9 -> holder.itemView.findViewById<TextView>(R.id.detail_sub).autoLinkMask = 0
                1 -> holder.itemView.findViewById<TextView>(R.id.detail_main).autoLinkMask = 0
            }
        }

        (holder as UpdateViewHolder).bindViews(detailContent)
    }


    override fun getItemCount(): Int {
        return dataSource.size
    }

}





