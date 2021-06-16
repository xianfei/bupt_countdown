package cn.edu.bupt.countdown

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import cn.edu.bupt.countdown.base_view.BaseTitleActivity
import cn.edu.bupt.countdown.main.BackupFragment
import cn.edu.bupt.countdown.utils.PreferenceUtils
import kotlinx.android.synthetic.main.activity_settings.*
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.startActivity

class SettingsActivity : BaseTitleActivity() {
    override val layoutId: Int
        get() = R.layout.activity_settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tv_about.setOnClickListener {
            startActivity<cn.edu.bupt.countdown.AboutActivity>()
        }

        tv_backup.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
            } else {
                BackupFragment.newInstance().show(supportFragmentManager, null)
            }
        }

        tv_nav_bar_color.setOnClickListener {
            ColorPickerDialogBuilder
                .with(this)
                .setTitle("选取颜色")
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setNegativeButton("取消") { _, _ -> }
                .initialColor(
                    PreferenceUtils.getIntFromSP(
                        applicationContext,
                        "nav_bar_color",
                        ContextCompat.getColor(applicationContext, R.color.white)
                    )
                )
                .setPositiveButton("确定") { _, colorInt, _ ->
                    PreferenceUtils.saveIntToSP(applicationContext, "nav_bar_color", colorInt)
                    it.longSnackbar("重启 App 后生效哦~")
                }
                .build()
                .show()
        }

        s_day_plus.isChecked = PreferenceUtils.getBooleanFromSP(this, "s_day_plus", false)

        ll_day_plus.setOnClickListener {
            s_day_plus.isChecked = !s_day_plus.isChecked
        }

        s_day_plus.setOnCheckedChangeListener { view, isChecked ->
            PreferenceUtils.saveBooleanToSP(this, "s_day_plus", isChecked)
            view.longSnackbar("重启 App 后生效哦~")
        }
    }
}
