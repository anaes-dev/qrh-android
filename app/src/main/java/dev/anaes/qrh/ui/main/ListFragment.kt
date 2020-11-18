package dev.anaes.qrh.ui.main

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.SearchView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import dev.anaes.qrh.Guideline
import dev.anaes.qrh.ListRecyclerAdapter
import dev.anaes.qrh.Main
import dev.anaes.qrh.R
import kotlinx.android.synthetic.main.activity_list.*


class ListFragment : Fragment() {

    private lateinit var linearLayoutManager: LinearLayoutManager

    private val navController by lazy { findNavController() }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        Main.breadcrumbCount = 0
        Main.breadcrumbList.clear()
        Main.breadcrumbIsActive = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity?.findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)?.title = "QRH"
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        activity?.findViewById<AppBarLayout>(R.id.app_bar)?.setExpanded(false)
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
    }

    private fun guidelineClicked(guideline : Guideline) {
        val code = guideline.code
        val action = ListFragmentDirections.LoadDetail(code)
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
        Main.breadcrumbCount = 0
        Main.breadcrumbList.clear()
        Main.breadcrumbIsActive = false
    }


}