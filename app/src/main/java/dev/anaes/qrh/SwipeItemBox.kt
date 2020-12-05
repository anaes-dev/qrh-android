package dev.anaes.qrh

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class SwipeItemBox(passedStep: String, passedHead: String, passedBody: String, passedType: Int) : Fragment() {

    private val step: String = passedStep
    private val head: String = passedHead
    private val body: String = passedBody
    private val type: Int = passedType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.swipe_item_standard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val detailStep = view.findViewById<TextView>(R.id.detail_step)
        val detailHead = view.findViewById<TextView>(R.id.detail_head)
        val detailBody = view.findViewById<TextView>(R.id.detail_body)

        when (type) {
            1 -> {
                detailStep.visibility = View.GONE
                detailHead.visibility = View.GONE
            }
            2 -> {
                detailStep.visibility = View.GONE
                detailBody.visibility = View.GONE
                detailHead.visibility = View.GONE
                val detailStart = view.findViewById<TextView>(R.id.detail_start)
                detailStart.visibility = View.VISIBLE
                detailStart.text = SwipeAdapter.htmlProcess(body, view)
            }
            3 -> {
                detailStep.text = step
                detailHead.text = head
            }
            4 -> {
                detailStep.text = step
                detailHead.visibility = View.GONE
            }
        }

        detailBody.text = SwipeAdapter.htmlProcess(body, view)
    }
}