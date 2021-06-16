package cn.edu.bupt.countdown

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.BaseDialogFragment
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.github.florent37.glidepalette.GlidePalette
import cn.edu.bupt.countdown.bean.EventBean
import cn.edu.bupt.countdown.event_add.AddEventActivity
import cn.edu.bupt.countdown.event_add.AddEventViewModel
import cn.edu.bupt.countdown.share.ShareEventActivity
import kotlinx.android.synthetic.main.activity_share_event.*
import kotlinx.android.synthetic.main.fragment_detail.*
import kotlinx.android.synthetic.main.fragment_detail.iv_share
import kotlinx.android.synthetic.main.fragment_detail.tv_days
import kotlinx.android.synthetic.main.fragment_detail.tv_msg
import kotlinx.android.synthetic.main.fragment_detail.tv_share_event
import kotlinx.android.synthetic.main.fragment_detail.tv_start_time
import org.jetbrains.anko.support.v4.startActivity
import cn.edu.bupt.countdown.utils.HtmlTagHandler

import androidx.core.content.ContextCompat
import cn.edu.bupt.countdown.utils.ColorPalette
import kotlinx.android.synthetic.main.activity_add_event.*
import kotlinx.android.synthetic.main.item_event.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.longToast
import org.jetbrains.anko.textColor


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Use the [DetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailFragment : BaseDialogFragment() {
    // TODO: Rename and change types of parameters

    private lateinit var viewModel: AddEventViewModel

    override val layoutId: Int
        get() = R.layout.fragment_detail

    override fun onCreate(savedInstanceState: Bundle?) {
//        bgColor = Color.argb(192,0,0,0)
        super.onCreate(savedInstanceState)
        activity!!.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        viewModel = ViewModelProviders.of(this).get(AddEventViewModel::class.java)
        arguments?.let {
            viewModel.event = it.getParcelable("event")!!
            viewModel.isNew = false
        }
    }

    override fun onDestroy() {
        activity!!.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        cardvvv.onClick {  }
        bb.onClick { dismiss() }
        super.onViewCreated(view, savedInstanceState)
        val iconFont = ResourcesCompat.getFont(context!!, R.font.iconfont)
        val description = viewModel.event!!.getDescriptionWithDays(context!!)
        tv_share_event.text = description[0]
        val strs = description[1].split(" ")
        val str = strs[0] + "<myfont size='150px'>"+strs[1]+"</myfont>"+strs[2]
        tv_days.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(str, HtmlCompat.FROM_HTML_MODE_COMPACT,null, HtmlTagHandler("myfont"))
        } else {
            Html.fromHtml(str,null, HtmlTagHandler("myfont"))
        }
        tv_start_time.text = description[2]
        if (viewModel.event!!.msg.isBlank()) {
            linearLayout3.removeView(tv_msg)
        } else {
            tv_msg.visibility = View.VISIBLE
            tv_msg.text = viewModel.event!!.msg
        }
        Glide.with(this)
            .load(if (viewModel.event!!.path.isBlank()) R.drawable.default_background else viewModel.event!!.path)
//            .transform(MultiTransformation(CenterCrop(), RoundedCorners(50)))
            .listener(
                GlidePalette
                    .with(viewModel.event.path)
                    .intoCallBack {
                        val colorP = ColorPalette(it!!)

                        tv_days.textColor = colorP.getTitleTextColor()
                        tv_share_event.textColor = colorP.getTitleTextColor()
                        button.textColor = colorP.getTitleTextColor()
                        button2.textColor = colorP.getTitleTextColor()
                        tv_msg.textColor = colorP.getBodyTextColor()
                        tv_start_time.textColor = colorP.getBodyTextColor()
                        tv_card.backgroundColor = colorP.getColor1()
                        view1.backgroundColor = it!!.getLightVibrantColor(ContextCompat.getColor(context!!, R.color.blue_50))
                        view2.backgroundColor = it!!.getVibrantColor(ContextCompat.getColor(context!!, R.color.blue_50))
                        view3.backgroundColor = it!!.getDarkVibrantColor(ContextCompat.getColor(context!!, R.color.blue_50))
                        view4.backgroundColor = it!!.getLightMutedColor(ContextCompat.getColor(context!!, R.color.blue_50))
                        view5.backgroundColor = it!!.getMutedColor(ContextCompat.getColor(context!!, R.color.blue_50))
                        view6.backgroundColor = it!!.getDarkMutedColor(ContextCompat.getColor(context!!, R.color.blue_50))
                    }
            )
            .override(600)
            .into(iv_share)
        // edit
        button.setOnClickListener {
            startActivity<AddEventActivity>("event" to viewModel.event)
        }
        button.typeface = iconFont
        button2.setOnClickListener {
            startActivity<ShareEventActivity>("event" to viewModel.event)
        }
        button2.typeface = iconFont
        button3.typeface = iconFont
        button3.setOnClickListener {
            launch {
                val msg = withContext(Dispatchers.IO) {
                    try {
                        viewModel.delete()
                        "ok"
                    } catch (e: Exception) {
                        "发生异常>_<" + e.message
                    }
                }
                if (msg == "ok") {
                    longToast("删除成功")
                    dismiss()
                } else {
                    longToast(msg)
                }
            }
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(arg:EventBean) =
            DetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("event", arg)
                }
            }
    }
}