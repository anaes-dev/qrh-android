package dev.anaes.qrh

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_detail.*

interface PushDetail {
    fun navToDetail(code: String, title: String, url: String, version: String)
}

class DetailFragment : Fragment(), PushDetail {

    private val navController by lazy { findNavController() }

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

        val fragmentHash: Int = parentFragmentManager.backStackEntryCount
        vm.breadcrumbTitles[fragmentHash] = args.title
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        (activity as MainInt).updateBar(title.toString(), code.toString(), version.toString(), true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        (activity as MainInt).updateBar(title.toString(), code.toString(), version.toString(), true)
        detail_code_2.text = code.toString()
        activity?.findViewById<AppBarLayout>(R.id.app_bar)?.setExpanded(true)


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
                        activity?.findViewById<ProgressBar>(R.id.progress_circular)?.visibility = View.VISIBLE
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



        if(parentFragmentManager.backStackEntryCount > 1) {
            detail_scroll.isVisible = true


            detail_home.setOnClickListener {
                (activity as MainInt).popToDetail(parentFragmentManager.backStackEntryCount)
            }


            var bci: Int = parentFragmentManager.backStackEntryCount
            bci--

            var bi: Int = 0

            while (bi < parentFragmentManager.backStackEntryCount) {

                val chevron = ImageView(context)
                chevron.setImageResource(R.drawable.ic_chevron_right)
                chevron.maxHeight = 12
                chevron.maxWidth = 12
                bcStack.addView(chevron)

                val button = Button(context, null, android.R.attr.buttonBarButtonStyle)
                button.textSize = 12F
                button.isAllCaps = false


                val hash = parentFragmentManager.getBackStackEntryAt(bi).id.hashCode()
                Log.d("Hash:", hash.toString())

                button.text = vm.breadcrumbTitles[bi + 1]

                button.tag = bci
                if (bci > 0) {
                    button.setOnClickListener {
                        activity?.findViewById<ProgressBar>(R.id.progress_circular)?.visibility =
                            View.VISIBLE
                        (activity as MainInt).popToDetail(button.tag as Int)
                    }

                }
                bcStack.addView(button)
                bci--
                bi++
            }

        }

        (view.parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
            activity?.findViewById<ProgressBar>(R.id.progress_circular)?.visibility = View.GONE
            detail_scroll.scrollTo(detail_scroll.right, 0)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_detail, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun navToDetail(code: String, title: String, url: String, version: String) {
        val action = DetailFragmentDirections.LoadNewDetail(code, title, url, version)
        activity?.findViewById<ProgressBar>(R.id.progress_circular)?.visibility = View.VISIBLE
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

