package dev.anaes.qrh

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.ORIENTATION_HORIZONTAL
import com.google.android.material.tabs.TabLayoutMediator
import dev.anaes.qrh.databinding.FragmentSwipeBinding
import java.lang.Math.abs

class SwipeFragment : Fragment() {

    private val navController by lazy { findNavController() }

    private val args: SwipeFragmentArgs by navArgs()

    private lateinit var viewPager: ViewPager2

    private lateinit var linearLayoutManager: LinearLayoutManager

    private val vm: MainViewModel by activityViewModels()

    var code: String = String()
    var title: String = String()
    var version: String = String()

    private var _binding: FragmentSwipeBinding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
        code = args.code
        title = args.title
        version = args.version
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSwipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        (activity as MainInt).updateBar(title, code, version, expanded = false, hideKeyboard = false, opaque = false)

        this.context?.let { safeContext ->
            viewPager = binding.swipeViewpager
            val content = vm.fetchGuideline(code, safeContext)
            content.removeAll {
                it.type == 12 || it.type == 11
            }
            val adapter = SwipeAdapter(this, content)
            viewPager.adapter = adapter

            val swipeCallback = object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    val currentPage = (position + 1).toString()
                    val totalPages = adapter.itemCount.toString()
                    val pagesString = "$currentPage of $totalPages"
                    binding.swipeIndicator.text = pagesString
                }
            }

            val offsetPx = 18.dpToPx(resources.displayMetrics)
            val pageMarginPx = 16.dpToPx(resources.displayMetrics)

            viewPager.apply {
                registerOnPageChangeCallback(swipeCallback)
                clipChildren = false
                clipToPadding = false
                offscreenPageLimit = 2
                setPageTransformer { page, position ->
                    val offset = position * -(2 * offsetPx + pageMarginPx)
                    if(ViewCompat.getLayoutDirection(binding.swipeViewpager) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                        page.translationX = -offset
                    } else {
                        page.translationX = offset
                    }
                    page.alpha = 0.9F + (1 - kotlin.math.abs(position))
                }
            }

            TabLayoutMediator(binding.swipeTabLayout, viewPager) { _, _ -> }.attach()
        }




        (view.parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
            (activity as MainInt).progressShow(false)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun Int.dpToPx(displayMetrics: DisplayMetrics): Int = (this * displayMetrics.density).toInt()

    companion object {
        fun swipeToLink(url: String, activity: Activity, context: Context) {
            if (url.startsWith("qrh://")) {
                val codeNew = url.removePrefix("qrh://")
                (activity as MainInt).progressShow(true)
                val fetchDetails: Array<String> =
                    DetailFragment.getDetailsFromCode(codeNew, context)
                (activity as MainInt).swipeDetail(
                    codeNew, fetchDetails[0], fetchDetails[1], fetchDetails[2])
            } else {
                (activity as MainInt).openURL(url)
            }
        }
    }

}