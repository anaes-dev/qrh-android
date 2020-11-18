package dev.anaes.qrh.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import dev.anaes.qrh.*
import kotlinx.android.synthetic.main.activity_detail_scrollingcontent.*


interface PushDetail {
    fun navToDetail(code: String)
}

class DetailFragment : Fragment(), PushDetail {

    private val navController by lazy { findNavController() }

    private val args: DetailFragmentArgs by navArgs()

    private lateinit var linearLayoutManager: LinearLayoutManager

    var code: String? = String()

    private var getDetails: Array<String> = arrayOf()
    var title: String? = String()
    var url: String? = String()
    var version: String? = String()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        code = args.code

        getDetails = getDetailsFromCode(code)
        title = getDetails[0]
        url = getDetails[1]
        version = "v. "+ getDetails[2]

        activity?.findViewById<AppBarLayout>(R.id.app_bar)?.setExpanded(true)
        activity?.findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)?.title = title

        Main.breadcrumbList.add(title.toString())
        Main.breadcrumbCount++
        Main.breadcrumbIsActive = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()


        activity.let { it ->
            if (it != null) {
                it.findViewById<TextView>(R.id.detail_code).text = code
                it.findViewById<TextView>(R.id.detail_code_2).text = code
                it.findViewById<TextView>(R.id.detail_version).text = version
            }
        }


        val filenameSuffix = ".json"
        val filename = code + filenameSuffix

        this.context?.let { safeContext ->
            val content = DetailContent.getContentFromFile(
                filename,
                safeContext
            )
            val adapter =
                CardRecyclerAdapter(content, code) { code: String ->
                    navToDetail(code)
                }
            linearLayoutManager = LinearLayoutManager(context)
            detail_recyclerview.layoutManager = linearLayoutManager
            detail_recyclerview.isNestedScrollingEnabled = false
            detail_recyclerview.adapter = adapter
        } ?: run {
            Error("Internal Error")
        }

        val bcStack = detail_stack

        if(Main.breadcrumbCount > 1) {
            detail_scroll.isVisible = true

            var bci: Int = Main.breadcrumbCount
            bci--


            for (bc in Main.breadcrumbList) {
                Log.d("loop", bci.toString())
                val chevron = ImageView(context)
                chevron.setImageResource(R.drawable.ic_chevron_right)
                chevron.maxHeight = 12
                chevron.maxWidth = 12
                bcStack.addView(chevron)

                val button = Button(context, null, android.R.attr.buttonBarButtonStyle)
                button.textSize = 12F
                button.isAllCaps = false
                button.text = bc
                button.tag = bci
                button.setOnClickListener {
                    (activity as PopDetail).popToDetail(button.tag as Int)
                }
                bcStack.addView(button)
                bci--
            }
            detail_home.setOnClickListener {
                Main.breadcrumbCount == 0
                Main.breadcrumbList.clear()
                val action = DetailFragmentDirections.PopHome()
                findNavController().navigate(action)
            }

        }

        (view.parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
        }

    }

    override fun onResume() {
        super.onResume()
        Main.breadcrumbIsActive = true
        activity?.findViewById<AppBarLayout>(R.id.app_bar)?.setExpanded(true)
        activity?.findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)?.title = title
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_detail, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun navToDetail(code: String) {
        val action = DetailFragmentDirections.LoadNewDetail(code)
        navController.navigate(action)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.navigation_download -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun getDetailsFromCode(codePassed: String?): Array<String> {
        this.context?.let { safeContext ->
            val guideList = Guideline.getGuidelinesFromFile(
                "guidelines.json",
                safeContext
            )
            val position: Int = guideList.indexOfFirst { it.code == codePassed }
            return arrayOf(
                guideList[position].title,
                guideList[position].url,
                guideList[position].version.toString()
            )
        } ?: run {
            return arrayOf(
                "Default", "", ""
            )
        }
    }

}

