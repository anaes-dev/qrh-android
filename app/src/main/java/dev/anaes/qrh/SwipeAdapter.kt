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
        return dataSource.size
    }

    override fun createFragment(position: Int): Fragment {
        when(dataSource[position].type) {
            5,6,7,8,9 -> {
                return SwipeItemBox(
                    dataSource[position].head,
                    dataSource[position].body,
                    dataSource[position].type
                )}
            else ->
                return SwipeItemStandard(
                    dataSource[position].step,
                    dataSource[position].head,
                    dataSource[position].body,
                    dataSource[position].type
                )
            }
        }

}