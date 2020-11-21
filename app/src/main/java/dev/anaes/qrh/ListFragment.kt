package dev.anaes.qrh


import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.SearchView
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_list.*


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

        var alreadyAnimated: Boolean = false

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                if (adapter.itemCount == 0) {
                    if(!alreadyAnimated) {
                        alreadyAnimated = true
                        list_empty.alpha = 0F
                        list_empty.visibility = View.VISIBLE
                        list_empty.animate().alpha(1F).duration = 300
                    }
                } else {
                    list_empty.visibility = View.GONE
                    alreadyAnimated = false
                }
            }
        })

        list_searchview.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })

        (view.parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
            (activity as MainInt).progressShow(false)
        }

        if(vm.isStartup) {
            vm.isStartup = false

            this.context?.let { safeContext ->
            val snack =
                Snackbar
                    .make(
                        container,
                                "Unofficial adaptation of Quick Reference Handbook\n" +
                                "Not endorsed by the Association of Anaesthetists\n" +
                                "Untested and unregulated; not recommended for clinical use\n" +
                                "No guarantees of completeness, accuracy or performance\n" +
                                "Should not override your own knowledge and judgement",
                        Snackbar.LENGTH_LONG
                    )
                    .setDuration(8000)
                    .setBackgroundTint(getColor(safeContext, R.color.snackbarBackground))
                    .setAction(R.string.title_about) {
                        findNavController().navigate(R.id.LoadAbout)
                    }

            val snackView: View = snack.view
            val snackTextView = snackView.findViewById(R.id.snackbar_text) as TextView
            snackTextView.maxLines = 5
            snackTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12F)
            snack.show()

            // Dismiss snackbar if list scrolled (otherwise will stay visible for 8 seconds)

            var isDismissing = false

            list_recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 2) {
                        if (!isDismissing) {
                            isDismissing = true
                            snackView.animate().alpha(0F).setDuration(600)
                                .withEndAction { snack.dismiss() }
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
        (activity as MainInt).updateBar("QRH", "", "", false)
    }


}