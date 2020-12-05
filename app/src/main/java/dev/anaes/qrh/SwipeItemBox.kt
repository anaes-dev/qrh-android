package dev.anaes.qrh

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView

class SwipeItemBox(passedHead: String, passedBody: String, passedType: Int) : Fragment() {

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
        return inflater.inflate(R.layout.swipe_item_box, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val detailHead = view.findViewById<TextView>(R.id.detail_head)
        val detailBody = view.findViewById<TextView>(R.id.detail_body)
        val detailCard = view.findViewById<MaterialCardView>(R.id.detail_card)

        when (type) {
            5 -> {
                detailCard.setCardBackgroundColor(ContextCompat.getColor(view.context, R.color.detailItemOrangeBG))
                detailHead.setTextColor(ContextCompat.getColor(view.context, R.color.detailItemOrangeTXT))
            }
            6 -> {
                detailCard.setCardBackgroundColor(ContextCompat.getColor(view.context, R.color.detailItemBlueBG))
                detailHead.setTextColor(ContextCompat.getColor(view.context, R.color.detailItemBlueTXT))
            }
            7 -> {
                detailCard.setCardBackgroundColor(ContextCompat.getColor(view.context, R.color.detailItemGreenBG))
                detailHead.setTextColor(ContextCompat.getColor(view.context, R.color.detailItemGreenTXT))
            }
            8 -> {
                detailCard.setCardBackgroundColor(ContextCompat.getColor(view.context, R.color.detailItemBlackBG))
                detailHead.setTextColor(ContextCompat.getColor(view.context, R.color.detailItemBlackTXT))
            }
            9 -> {
                detailCard.setCardBackgroundColor(ContextCompat.getColor(view.context, R.color.detailItemPurpleBG))
                detailHead.setTextColor(ContextCompat.getColor(view.context, R.color.detailItemPurpleTXT))
            }
        }

        detailHead.text = head
        detailBody.text = htmlProcess(body, view)


    }
}