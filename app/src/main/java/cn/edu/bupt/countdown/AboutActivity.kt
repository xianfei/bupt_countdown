package cn.edu.bupt.countdown

import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import android.widget.TextView
import cn.edu.bupt.countdown.base_view.BaseTitleActivity
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.textColorResource

class AboutActivity : BaseTitleActivity() {

    override val layoutId: Int
        get() = R.layout.activity_about

    override fun onSetupSubButton(tvButton1: TextView, tvButton2: TextView) {
        val iconFont = ResourcesCompat.getFont(this, R.font.iconfont)
        tvButton2.textSize = 20f
        tvButton2.typeface = iconFont
        tvButton2.text = "\uE6C2"
        tvButton2.textColorResource = R.color.pink
        tvButton2.setOnClickListener {
            it.longSnackbar("我爱你\no(*////▽////*)q")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
