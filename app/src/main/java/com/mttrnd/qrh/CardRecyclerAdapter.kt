package com.mttrnd.qrh

import android.net.Uri
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


@Suppress("DEPRECATION")
class CardRecyclerAdapter(var dataSource: ArrayList<DetailContent>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
    }

   interface UpdateViewHolder {
        fun bindViews(detailContent: DetailContent)
    }

/*    fun setFadeAnimation(view: View) {
        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.duration = 1000
        view.startAnimation(anim)
    }*/

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

    class ViewHolder3(itemView: View) : RecyclerView.ViewHolder(itemView), UpdateViewHolder {
        override fun bindViews(detailContent: DetailContent) {
            val detailImage = itemView.findViewById<ImageView>(R.id.detail_image)
            Glide
                .with(itemView)
                .load(Uri.parse(detailContent.main))
                .into(detailImage)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val type = when (dataSource[position].type) {
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
            else -> VIEW_THREE
            }
        return type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder: RecyclerView.ViewHolder = when (viewType) {
            VIEW_ONE -> ViewHolder1(LayoutInflater.from(parent.context).inflate(R.layout.detail_item_preamble, parent, false))
            VIEW_TWO -> ViewHolder1(LayoutInflater.from(parent.context).inflate(R.layout.detail_item_start, parent, false))
            VIEW_THREE -> ViewHolder1(LayoutInflater.from(parent.context).inflate(R.layout.detail_item_standard, parent, false))
            VIEW_FOUR -> ViewHolder1(LayoutInflater.from(parent.context).inflate(R.layout.detail_item_standard_oneline, parent, false))
            VIEW_FIVE -> ViewHolder2(LayoutInflater.from(parent.context).inflate(R.layout.detail_item_orange, parent, false))
            VIEW_SIX -> ViewHolder2(LayoutInflater.from(parent.context).inflate(R.layout.detail_item_blue, parent, false))
            VIEW_SEVEN -> ViewHolder2(LayoutInflater.from(parent.context).inflate(R.layout.detail_item_green, parent, false))
            VIEW_EIGHT -> ViewHolder2(LayoutInflater.from(parent.context).inflate(R.layout.detail_item_black, parent, false))
            VIEW_NINE -> ViewHolder2(LayoutInflater.from(parent.context).inflate(R.layout.detail_item_purple, parent, false))
            VIEW_TEN -> ViewHolder3(LayoutInflater.from(parent.context).inflate(R.layout.detail_item_image, parent, false))
            else ->  ViewHolder1(LayoutInflater.from(parent.context).inflate(R.layout.detail_item_standard, parent, false))
            }
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val detailContent: DetailContent = dataSource[position]
//        setFadeAnimation(holder.itemView);
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
        (holder as UpdateViewHolder).bindViews(detailContent)
    }


    override fun getItemCount(): Int {
        return dataSource.size
    }

}





