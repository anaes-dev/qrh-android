package dev.anaes.qrh

import androidx.recyclerview.widget.DiffUtil
import dev.anaes.qrh.Guideline

class ListDiffUtil(
    private var oldList: ArrayList<Guideline>,
    private var newList: ArrayList<Guideline>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].code === newList[newItemPosition].code
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldGuideline: Guideline = oldList[oldItemPosition]
        val newGuideline: Guideline = newList[newItemPosition]
        return oldGuideline.hashCode() == newGuideline.hashCode()
    }
}