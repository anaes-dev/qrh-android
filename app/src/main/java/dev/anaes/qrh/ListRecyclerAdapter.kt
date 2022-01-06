package dev.anaes.qrh

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.collections.ArrayList

class ListRecyclerAdapter(
    var dataSource: ArrayList<Guideline>,
    private val clickListener: (Guideline) -> Unit
) : RecyclerView.Adapter<ListRecyclerAdapter.GuidelineHolder>(),
    Filterable {

    var filterList = ArrayList<Guideline>()
    var filterListPublished = dataSource

    class GuidelineHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item_guideline, parent, false)) {
        val mTitleView: TextView = itemView.findViewById(R.id.guideline_title)
        val mCodeView: TextView = itemView.findViewById(R.id.guideline_code)
        val mVersionView: TextView = itemView.findViewById(R.id.guideline_version)

        fun bind(guideline: Guideline, clickListener: (Guideline) -> Unit) {
            itemView.setOnClickListener { clickListener(guideline) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuidelineHolder {
        val inflater = LayoutInflater.from(parent.context)
        return GuidelineHolder(
            inflater,
            parent
        )
    }

    override fun getItemCount(): Int {
        return filterListPublished.size
    }

    override fun onBindViewHolder(holder: GuidelineHolder, position: Int) {
        val guideline: Guideline = filterListPublished[position]
        holder.mTitleView.text = guideline.title
        holder.mCodeView.text= guideline.code

        val guidelineVersion = "v." + guideline.version.toString()
        holder.mVersionView.text = guidelineVersion

        holder.bind(filterListPublished[position], clickListener)
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(searchSeq: CharSequence): FilterResults {
                var charSearch = searchSeq.toString()
                filterList = if (charSearch.isEmpty()) {
                    dataSource
                } else {
                    val resultList = ArrayList<Guideline>()
                    charSearch = charSearch.lowercase(Locale.ROOT)
                    for (item in dataSource) {
                        if ((item.code.lowercase(Locale.ROOT) +(item.title.lowercase(Locale.ROOT))).contains(
                                charSearch.lowercase(
                                    Locale.ROOT
                                )
                            )) {
                            resultList.add(item)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filterList
                filterResults.count = filterList.size
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                val newList = results?.values as ArrayList<Guideline>
                updateList(newList)
            }
        }
    }

    fun updateList(guidelines: ArrayList<Guideline>) {
        val diffCallback = ListDiffUtil(filterListPublished, guidelines)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        filterListPublished = filterList
        diffResult.dispatchUpdatesTo(this)
    }

}




