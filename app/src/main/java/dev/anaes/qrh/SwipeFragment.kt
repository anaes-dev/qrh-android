package dev.anaes.qrh

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import dev.anaes.qrh.databinding.FragmentSwipeBinding

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
            val content = DetailContent.getContentFromFile(
                "$code.json",
                safeContext
            )
            val adapter = SwipeAdapter(this, content)
            viewPager.adapter = adapter

            binding.swipeIndicator.text = adapter.itemCount.toString()
        }




        (view.parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
            (activity as MainInt).progressShow(false)
        }

    }



}