package com.mttrnd.qrh

import android.text.Editable
import android.text.Html
import android.text.Html.TagHandler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


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
    }

   interface UpdateViewHolder {
        fun bindViews(detailContent: DetailContent)
    }

    class ViewHolder1(itemView: View) : RecyclerView.ViewHolder(itemView), UpdateViewHolder {
        override fun bindViews(detailContent: DetailContent) {
        }
    }
    class ViewHolder2(itemView: View) : RecyclerView.ViewHolder(itemView), UpdateViewHolder {
        override fun bindViews(detailContent: DetailContent) {
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
            VIEW_FIVE -> ViewHolder1(LayoutInflater.from(parent.context).inflate(R.layout.detail_item_style1, parent, false))
            VIEW_SIX -> ViewHolder1(LayoutInflater.from(parent.context).inflate(R.layout.detail_item_style2, parent, false))
            VIEW_SEVEN -> ViewHolder1(LayoutInflater.from(parent.context).inflate(R.layout.detail_item_style3, parent, false))
            else ->  ViewHolder1(LayoutInflater.from(parent.context).inflate(R.layout.detail_item_standard, parent, false))
            }
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
       val detailContent: DetailContent = dataSource[position]


       holder.apply {
           itemView.findViewById<TextView>(R.id.detail_main).setText(Html.fromHtml((detailContent.main)).trim())
           itemView.findViewById<TextView>(R.id.detail_sub).setText(Html.fromHtml((detailContent.sub)).trim())
           itemView.findViewById<TextView>(R.id.detail_step).setText(detailContent.step).toString()
       }
        (holder as UpdateViewHolder).bindViews(dataSource[position])
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }


}





