package dev.anaes.qrh

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import dev.anaes.qrh.databinding.FragmentDetailBinding

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

    private var _binding: FragmentDetailBinding? = null

    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        code = args.code
        title = args.title
        url = args.url
        version = "v. " + args.version
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        val bcEntry: Int = parentFragmentManager.backStackEntryCount
        vm.setBreadCrumbTitle(args.title, bcEntry)
        (activity as MainInt).updateBar(title.toString(), code.toString(), version.toString(),
            expanded = true,
            hideKeyboard = true,
            opaque = false
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        val bcEntry: Int = parentFragmentManager.backStackEntryCount
        vm.setBreadCrumbTitle(args.title, bcEntry)
        (activity as MainInt).updateBar(title.toString(), code.toString(), version.toString(),
            expanded = true,
            hideKeyboard = true,
            opaque = false
        )

        binding.detailCode2.text = code.toString()

        when(code) {
            "3-2" -> {
                binding.detailCode2.textSize = 48F
                binding.detailCode2.translationY = 0F
            }
            "3-3", "3-7", "3-11" -> {
                binding.detailCode2.textSize = 72F
                binding.detailCode2.translationY = -12F
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
                        (activity as MainInt).progressShow(true)
                        val fetchDetails: Array<String> = getDetailsFromCode(codeNew, safeContext)
                        navToDetail(codeNew, fetchDetails[0], fetchDetails[1], fetchDetails[2])
                    } else {
                        (activity as MainInt).openURL(url)
                    }
                }

            linearLayoutManager = LinearLayoutManager(context)
            binding.detailRecyclerView.layoutManager = linearLayoutManager
            binding.detailRecyclerView.isNestedScrollingEnabled = false
            binding.detailRecyclerView.adapter = adapter


        if(parentFragmentManager.backStackEntryCount > 1) {

            var bci = 0
            var bcd: Int = parentFragmentManager.backStackEntryCount - 1

            val color = ContextCompat.getColor(safeContext, R.color.colorAccent)

            val bcHomeButton = MaterialButton(safeContext, null, R.attr.borderlessButtonStyle)
            bcHomeButton.textSize = 12F
            bcHomeButton.isAllCaps = false
            bcHomeButton.letterSpacing = 0F
            bcHomeButton.setTextColor(color)
            bcHomeButton.text = getString(R.string.title_home)

            bcHomeButton.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(safeContext, R.drawable.ic_home),null,null,null)
            bcHomeButton.setOnClickListener {
                (activity as MainInt).popToDetail(parentFragmentManager.backStackEntryCount)
            }
            binding.breadCrumbStack.addView(bcHomeButton)


            while (bci < parentFragmentManager.backStackEntryCount) {

                val chevron = ImageView(context)
                chevron.setImageResource(R.drawable.ic_chevron_right)
                chevron.maxHeight = 12
                chevron.maxWidth = 12
                binding.breadCrumbStack.addView(chevron)

                val button = MaterialButton(safeContext, null, R.attr.borderlessButtonStyle)

                button.textSize = 12F
                button.isAllCaps = false
                button.letterSpacing = 0F
                button.setTextColor(color)
                button.text = vm.getBreadCrumbTitle(bci + 1)

                button.tag = bcd
                if (bcd > 0) {
                    button.setOnClickListener {
                        (activity as MainInt).progressShow(true)
                        (activity as MainInt).popToDetail(button.tag as Int)
                    }

                }
                binding.breadCrumbStack.addView(button)
                bci++
                bcd--
            }
        }
        }

        (view.parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
            (activity as MainInt).progressShow(false)
            binding.breadCrumbScroll.scrollTo(binding.breadCrumbScroll.right, 0)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_detail, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun navToDetail(code: String, title: String, url: String, version: String) {
        val action = DetailFragmentDirections.LoadNewDetail(code, title, url, version)
        (activity as MainInt).progressShow(true)
        navController.navigate(action)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.navigation_download -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                return true
            }
            R.id.navigation_swipe -> {
                val action = DetailFragmentDirections.LoadSwipe(args.code, args.title, args.version)
                navController.navigate(action)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        fun getDetailsFromCode(codePassed: String?, context: Context): Array<String> {
                val guideList = Guideline.getGuidelinesFromFile(
                    "guidelines.json",
                    context
                )
                val position: Int = guideList.indexOfFirst { it.code == codePassed }
                return arrayOf(
                    guideList[position].title,
                    guideList[position].url,
                    guideList[position].version.toString()
                )

        }
    }
}

