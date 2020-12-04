package dev.anaes.qrh

import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import dev.anaes.qrh.databinding.FragmentListBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ListFragment : Fragment() {

    private lateinit var linearLayoutManager: LinearLayoutManager

    private val navController by lazy { findNavController() }

    private val vm: MainViewModel by activityViewModels()

    private var _binding: FragmentListBinding? = null

    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        val snack = Snackbar.make(view,getString(R.string.snackLaunch),Snackbar.LENGTH_LONG)

        (activity as MainInt).updateBar("QRH", "", "", expanded = false, hideKeyboard = true, opaque = true)

        this.context?.let { safeContext ->

            val guidelineList =
                Guideline.getGuidelinesFromFile(
                    "guidelines.json",
                    safeContext
                )

            val adapter =
                ListRecyclerAdapter(guidelineList) { guideline: Guideline ->
                    binding.listSearchView.clearFocus()
                    snack.dismiss()
                    guidelineClicked(guideline)
                }

            linearLayoutManager = LinearLayoutManager(context)
            binding.listRecyclerView.layoutManager = linearLayoutManager
            binding.listRecyclerView.adapter = adapter
            binding.listRecyclerView.isNestedScrollingEnabled = false



            var alreadyAnimated = false

            adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onChanged() {
                    super.onChanged()
                    if (adapter.itemCount == 0) {
                        if (!alreadyAnimated) {
                            alreadyAnimated = true
                            binding.listEmpty.alpha = 0F
                            binding.listEmpty.visibility = View.VISIBLE
                            binding.listEmpty.animate().alpha(1F).duration = 300
                        }
                    } else {
                        binding.listEmpty.visibility = View.GONE
                        alreadyAnimated = false
                    }
                }
            })



            binding.listSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    adapter.filter.filter(newText)
                    return true
                }
            })

        }




            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
                (activity as MainInt).progressShow(false)
            }

            if (vm.isStartup) {
                vm.isStartup = false

                this.context?.let { safeContext ->

                    snack
                        .setDuration(8000)
                        .setBackgroundTint(getColor(safeContext, R.color.snackbarBackground))
                        .setTextColor(getColor(safeContext, R.color.snackbarText))

                    val snackView: View = snack.view
                    val snackTextView = snackView.findViewById(R.id.snackbar_text) as TextView
                    snackTextView.maxLines = 5
                    snackTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12F)

                    lifecycleScope.launch {
                        delay(100)
                        snack.show()
                    }

                    // Dismiss snackbar if list scrolled (otherwise will stay visible for 8 seconds)

                    var isDismissing = false

                    binding.listRecyclerView.addOnScrollListener(object :
                        RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)
                            if (dy > 2) {
                                binding.listSearchView.clearFocus()
                                if (!isDismissing) {
                                    isDismissing = true
                                    snackView.animate().alpha(0F).setDuration(600)
                                        .withEndAction {
                                            snack.view.visibility = View.GONE
                                            snack.dismiss()
                                        }
                                }
                            }
                        }
                    })
                }
            }


        }


    private fun guidelineClicked(guideline: Guideline) {
        val action = ListFragmentDirections.LoadDetail(
            guideline.code,
            guideline.title,
            guideline.url,
            guideline.version.toString()
        )
        (activity as MainInt).progressShow(true)
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
        (activity as MainInt).updateBar("QRH", "", "", expanded = false, hideKeyboard = true, opaque = true)
    }


}