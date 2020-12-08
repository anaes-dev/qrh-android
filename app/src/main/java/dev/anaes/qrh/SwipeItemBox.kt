package dev.anaes.qrh
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView

class SwipeItemBox() : Fragment() {

    companion object {
        fun newInstance(passedHead: String, passedBody: String, passedType: Int): Fragment {
            val args = Bundle()
            args.putString("head", passedHead)
            args.putString("body", passedBody)
            args.putInt("type", passedType)
            val fragment = SwipeItemBox()
            fragment.arguments = args
            return fragment
        }
    }

    private var head: String = String()
    private var body: String = String()
    private var type: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            head = it.getString("head").toString()
            body = it.getString("body").toString()
            type = it.getInt("type")
        }
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
        val gradTop = view.findViewById<View>(R.id.grad_top)
        val gradBottom = view.findViewById<View>(R.id.grad_bottom)

        when (type) {
            5 -> {
                detailCard.setCardBackgroundColor(ContextCompat.getColor(view.context, R.color.detailItemOrangeBG))
                detailHead.setTextColor(ContextCompat.getColor(view.context, R.color.detailItemOrangeTXT))
                gradTop.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(view.context, R.color.detailItemOrangeBG))
                gradBottom.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(view.context, R.color.detailItemOrangeBG))

            }
            6 -> {
                detailCard.setCardBackgroundColor(ContextCompat.getColor(view.context, R.color.detailItemBlueBG))
                detailHead.setTextColor(ContextCompat.getColor(view.context, R.color.detailItemBlueTXT))
                gradTop.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(view.context, R.color.detailItemBlueBG))
                gradBottom.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(view.context, R.color.detailItemBlueBG))

            }
            7 -> {
                detailCard.setCardBackgroundColor(ContextCompat.getColor(view.context, R.color.detailItemGreenBG))
                detailHead.setTextColor(ContextCompat.getColor(view.context, R.color.detailItemGreenTXT))
                gradTop.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(view.context, R.color.detailItemGreenBG))
                gradBottom.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(view.context, R.color.detailItemGreenBG))

            }
            8 -> {
                detailCard.setCardBackgroundColor(ContextCompat.getColor(view.context, R.color.detailItemBlackBG))
                detailHead.setTextColor(ContextCompat.getColor(view.context, R.color.detailItemBlackTXT))
                gradTop.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(view.context, R.color.detailItemBlackBG))
                gradBottom.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(view.context, R.color.detailItemBlackBG))

            }
            9 -> {
                detailCard.setCardBackgroundColor(ContextCompat.getColor(view.context, R.color.detailItemPurpleBG))
                detailHead.setTextColor(ContextCompat.getColor(view.context, R.color.detailItemPurpleTXT))
                gradTop.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(view.context, R.color.detailItemPurpleBG))
                gradBottom.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(view.context, R.color.detailItemPurpleBG))

            }
        }

        detailHead.text = head
        detailBody.text = htmlProcess(body, view)

        linkifyFunction(detailBody)

        detailBody.movementMethod = object : TextViewLinkHandler() {
            override fun onLinkClick(url: String?) {
                activity?.let { SwipeFragment.swipeToLink(url as String, it, context!!) }
            }
        }
    }
}