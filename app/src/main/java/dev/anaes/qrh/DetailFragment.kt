package dev.anaes.qrh

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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import kotlinx.android.synthetic.main.activity_detail_scrollingcontent.*
import kotlinx.android.synthetic.main.activity_main.*


interface PushDetail {
    fun navToDetail(code: String, title: String, url: String, version: String)
}

class DetailFragment : Fragment(), PushDetail {

    private val navController by lazy { findNavController() }

    private val fm by lazy { supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.childFragmentManager }

    private val args: DetailFragmentArgs by navArgs()

    private lateinit var linearLayoutManager: LinearLayoutManager

    private val vm: MainViewModel by activityViewModels()


    var code: String? = String()
    var title: String? = String()
    var url: String? = String()
    var version: String? = String()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        code = args.code
        title = args.title
        url = args.url
        version = "v. " + args.version

        activity?.findViewById<AppBarLayout>(R.id.app_bar)?.setExpanded(true)
        activity?.findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)?.title = title

        vm.breadcrumbList.add(title.toString())
        vm.breadcrumbCount++
        vm.breadcrumbIsActive = true
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
                (activity as MainInt).appBarCode(code.toString(), version.toString())
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
                CardRecyclerAdapter(content, code) { url: String ->
                    if(url.startsWith("qrh://")) {
                        val codeNew = url.removePrefix("qrh://")
                        activity?.progress_circular?.visibility = View.VISIBLE
                        val fetchDetails: Array<String> = getDetailsFromCode(codeNew)
                        navToDetail(codeNew, fetchDetails[0], fetchDetails[1], fetchDetails[2])
                    } else {
                        (activity as MainInt).openURL(url)
                    }
                }

            linearLayoutManager = LinearLayoutManager(context)
            detail_recyclerview.layoutManager = linearLayoutManager
            detail_recyclerview.isNestedScrollingEnabled = false
            detail_recyclerview.adapter = adapter
        } ?: run {
            Error("Internal Error")
        }

        val bcStack = detail_stack

        if(vm.breadcrumbCount > 1) {
            detail_scroll.isVisible = true

            var bci: Int = vm.breadcrumbCount
            bci--


            for (bc in vm.breadcrumbList) {
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
                    activity?.progress_circular?.visibility = View.VISIBLE
                    (activity as MainInt).popToDetail(button.tag as Int)
                }
                bcStack.addView(button)
                bci--
            }

            detail_home.setOnClickListener {
                vm.breadcrumbCount == 0
                vm.breadcrumbList.clear()
                val action = DetailFragmentDirections.PopHome()
                findNavController().navigate(action)
            }

        }

        (view.parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
            activity?.progress_circular?.visibility = View.GONE
        }

    }

    override fun onResume() {
        super.onResume()
        vm.breadcrumbIsActive = true
        activity?.findViewById<AppBarLayout>(R.id.app_bar)?.setExpanded(true)
        activity?.findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)?.title = title
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_detail, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun navToDetail(code: String, title: String, url: String, version: String) {
        val action = DetailFragmentDirections.LoadNewDetail(code, title, url, version)
        activity?.progress_circular?.visibility = View.VISIBLE
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

