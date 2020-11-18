package dev.anaes.qrh.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import dev.anaes.qrh.CardRecyclerAdapter
import dev.anaes.qrh.DetailContent
import dev.anaes.qrh.R
import kotlinx.android.synthetic.main.activity_detail_scrollingcontent.*


interface PushDetail {
    fun navToDetail(code: String)
}

class DetailFragment : Fragment(), PushDetail {

    private val navController by lazy { findNavController() }

    private val args: DetailFragmentArgs by navArgs()

    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        setHasOptionsMenu(true)
//        activity?.findViewById<AppBarLayout>(R.id.app_bar)?.setExpanded(true)
//        activity?.findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)?.title = "test"
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val filenameSuffix = ".json"
        val filename = args.code + filenameSuffix

        val content = DetailContent.getContentFromFile(
            filename,
            context
        )

        val adapter =
            CardRecyclerAdapter(content, args.code) { code: String ->
                navToDetail(code)
            }

        linearLayoutManager = LinearLayoutManager(context)
        detail_recyclerview.layoutManager = linearLayoutManager
        detail_recyclerview.isNestedScrollingEnabled = false
        detail_recyclerview.adapter = adapter
    }

//
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.menu_detail, menu)
//        super.onCreateOptionsMenu(menu, inflater)
//    }

    override fun navToDetail(code: String) {
        val action = DetailFragmentDirections.LoadNewDetail(code)
        findNavController().navigate(action)
    }
}

