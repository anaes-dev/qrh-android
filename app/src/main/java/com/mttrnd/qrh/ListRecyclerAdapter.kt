package com.mttrnd.qrh

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item_guideline.view.*
import java.util.*
import kotlin.collections.ArrayList

class ListRecyclerAdapter(var dataSource: ArrayList<Guideline>, val clickListener: (Guideline) -> Unit) :
    RecyclerView.Adapter<ListRecyclerAdapter.GuidelineHolder>(), Filterable {

    var guidelineFilterList = ArrayList<Guideline>()
    init {
        guidelineFilterList = dataSource
    }


    class GuidelineHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item_guideline, parent, false)) {
        val mTitleView: TextView = itemView.findViewById(R.id.guideline_title)
        val mCodeView: TextView = itemView.findViewById(R.id.guideline_code)

        fun bind(guideline: Guideline, clickListener: (Guideline) -> Unit) {
            itemView.setOnClickListener { clickListener(guideline) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListRecyclerAdapter.GuidelineHolder {
        val inflater = LayoutInflater.from(parent.context)
        return GuidelineHolder(inflater,parent)
    }

    override fun getItemCount(): Int {
        return guidelineFilterList.size
    }

    override fun onBindViewHolder(holder: ListRecyclerAdapter.GuidelineHolder, position: Int) {
        val guideline: Guideline = guidelineFilterList[position]
        holder.mTitleView.text = guideline.title
        holder.mCodeView.text= guideline.codedisplay

        holder.bind(guidelineFilterList[position], clickListener)
        }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    guidelineFilterList = dataSource
                } else {
                    val resultList = ArrayList<Guideline>()
                    val charSearch = charSearch.toLowerCase();

                    for (item in dataSource) {
                        if (item.title.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT))) {
                            resultList.add(item)
                        }
                    }

                    guidelineFilterList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = guidelineFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                guidelineFilterList = results?.values as ArrayList<Guideline>
                notifyDataSetChanged()
            }

        }
    }

}




