package cn.edu.bupt.countdown.main

import android.os.Build
import android.text.Html
import android.view.View
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.chad.library.adapter.base.BaseItemDraggableAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.github.florent37.glidepalette.GlidePalette
import cn.edu.bupt.countdown.R
import cn.edu.bupt.countdown.bean.EventBean
import cn.edu.bupt.countdown.utils.ColorPalette
import cn.edu.bupt.countdown.utils.HtmlTagHandler
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.fragment_detail.*
import org.jetbrains.anko.backgroundColor


class EventListAdapter(layoutResId: Int, data: MutableList<EventBean>) :
    BaseItemDraggableAdapter<EventBean, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: EventBean) {
        if(item.isFav){
            helper.setGone(R.id.cv_event,false)
        }else{
            helper.setGone(R.id.cv_event,true)
        }
        val description = item.getDescriptionWithDays(mContext)
        helper.setText(R.id.tv_content, description[0])
        val strs = description[1].split(" ")
        val str = strs[0] + "<myfont size='80px'>"+strs[1]+"</myfont>"+strs[2]
        helper.setText(R.id.tv_count,if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(str, HtmlCompat.FROM_HTML_MODE_COMPACT,null, HtmlTagHandler("myfont"))
        } else {
            Html.fromHtml(str,null, HtmlTagHandler("myfont"))
        })
        helper.setText(R.id.tv_date, description[2])

        if (item.msg.isBlank()) {
            helper.getView<View>(R.id.tv_msg).visibility = View.GONE
        } else {
            helper.getView<View>(R.id.tv_msg).visibility = View.VISIBLE
            helper.setText(R.id.tv_msg, item.msg)
        }

        Glide.with(mContext)
            .load(if (item.path.isBlank()) R.drawable.default_background else item.path)
            .override(300)
            .transform(MultiTransformation(CenterCrop(), RoundedCorners(50)))
            .into(helper.getView(R.id.iv_pic))
        Glide.with(mContext)
            .load(if (item.path.isBlank()) R.drawable.default_background else item.path)
            .override(300)
            .apply(bitmapTransform(BlurTransformation(50))).listener(
                GlidePalette
                    .with(item.path)
                    .intoCallBack {
                        val colorP = ColorPalette(it!!)
                        helper.getView<TextView>(R.id.tv_content).setTextColor(colorP.getColor1())
                        helper.getView<View>(R.id.v_bg).backgroundColor = colorP.getColor1BG()
                    }
            )
            .into(helper.getView(R.id.iv_pic_bg))
    }
}