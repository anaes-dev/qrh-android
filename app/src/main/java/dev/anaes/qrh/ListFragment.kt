package dev.anaes.qrh

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.SearchView
import android.widget.TextView
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import dev.anaes.qrh.ListFragmentDirections
import kotlinx.android.synthetic.main.activity_list.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_detail.*


class ListFragment : Fragment() {

    private lateinit var linearLayoutManager: LinearLayoutManager

    private val navController by lazy { findNavController() }

    private val vm: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        (activity as MainInt).updateBar("QRH", "", "", false)

        val rvScrollListener = R.id.list_recyclerview

        val guidelineList =
            Guideline.getGuidelinesFromFile(
                "guidelines.json",
                context
            )

        val adapter =
            ListRecyclerAdapter(guidelineList) { guideline: Guideline ->
                guidelineClicked(guideline)
            }

        linearLayoutManager = LinearLayoutManager(context)
        list_recyclerview.layoutManager = linearLayoutManager
        list_recyclerview.adapter = adapter
        list_recyclerview.isNestedScrollingEnabled = false

        list_searchview.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                if(adapter.itemCount == 0) {
                    view.findViewById<TextView>(R.id.list_empty)?.visibility = View.VISIBLE
                } else {
                    view.findViewById<TextView>(R.id.list_empty)?.visibility = View.GONE
                }
                return false
            }
        })

        (view.parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
            activity?.progress_circular?.visibility = View.GONE
        }
    }

    private fun guidelineClicked(guideline : Guideline) {
        val action = ListFragmentDirections.LoadDetail(
            guideline.code,
            guideline.title,
            guideline.url,
            guideline.version.toString()
        )
        activity?.progress_circular?.visibility = View.VISIBLE
        navController.navigate(action)
    }

   override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.navigation_about -> {
                findNavController().navigate(R.id.LoadAbout)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainInt).updateBar("QRH", "", "", false)
    }


}