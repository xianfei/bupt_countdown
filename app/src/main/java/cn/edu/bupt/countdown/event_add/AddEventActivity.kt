package cn.edu.bupt.countdown.event_add

import android.Manifest
import android.appwidget.AppWidgetManager
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import com.google.android.material.chip.Chip
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.github.florent37.glidepalette.GlidePalette
import cn.edu.bupt.countdown.R
import cn.edu.bupt.countdown.base_view.BaseTitleActivity
import cn.edu.bupt.countdown.bean.EventBean
import cn.edu.bupt.countdown.event_appwidget.EventAppWidget
import cn.edu.bupt.countdown.utils.GlideAppEngine
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_add_event.*
import kotlinx.android.synthetic.main.item_event.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.*
import org.jetbrains.anko.design.longSnackbar

class AddEventActivity : BaseTitleActivity() {

    override val layoutId: Int
        get() = R.layout.activity_add_event

    private lateinit var viewModel: AddEventViewModel
    private val REQUEST_CODE_CHOOSE_BG = 23
    private var makeSure = 0

    override fun onSetupSubButton(tvButton1: TextView, tvButton2: TextView) {
        val iconFont = ResourcesCompat.getFont(this, R.font.iconfont)
        tvButton1.textSize = 20f
        tvButton2.textSize = 20f
        tvButton1.typeface = iconFont
        tvButton2.typeface = iconFont

        tvButton1.text = "\uE6DB"
        tvButton2.text = "\uE6DE"

        tvButton1.setOnClickListener {
            pickImage()
        }

        tvButton2.setOnClickListener {
            if (viewModel.event.content.isBlank()) {
                tvButton2.longSnackbar("???????????????????????????<(?????????)>")
            } else {
                launch {
                    val msg = withContext(Dispatchers.IO) {
                        try {
                            viewModel.save()
                            if (!viewModel.isNew) {
                                val wList = viewModel.getEventWidgets()
                                withContext(Dispatchers.Main) {
                                    if (wList.isNotEmpty()) {
                                        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
                                        for (w in wList) {
                                            EventAppWidget.updateAppWidget(
                                                applicationContext,
                                                appWidgetManager,
                                                w.id,
                                                viewModel.event,
                                                w
                                            )
                                        }
                                    }
                                }
                            }
                            "ok"
                        } catch (e: Exception) {
                            "????????????>_<" + e.message
                        }
                    }
                    if (msg == "ok") {
                        toast("??????????????????????????")
                        finish()
                    } else {
                        longToast(msg)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AddEventViewModel::class.java)
        initEvent()

        if (intent.extras?.getParcelable<EventBean>("event") == null) {
            viewModel.event = EventBean()
            viewModel.isNew = true
        } else {
            viewModel.event = intent.extras!!.getParcelable("event")!!
            viewModel.isNew = false
            btn_delete.visibility = View.VISIBLE
        }
        initView()
    }

    private fun initEvent() {
        cv_event.setOnClickListener {
            pickImage()
        }

        btn_delete.setOnLongClickListener {
            launch {
                val msg = withContext(Dispatchers.IO) {
                    try {
                        viewModel.delete()
                        "ok"
                    } catch (e: Exception) {
                        "????????????>_<" + e.message
                    }
                }
                if (msg == "ok") {
                    longToast("????????????")
                    finish()
                } else {
                    longToast(msg)
                }
            }
            return@setOnLongClickListener true
        }

        var chipId = 0
        cg_type.setOnCheckedChangeListener { chipGroup, id ->
            when (id) {
                R.id.chip_commemoration -> {
                    tv_event_icon.text = "\uE6C2"
                    tv_event_icon.textColorResource = R.color.pink
                    et_event.hint = "????????????????????????"
                    et_date.hint = "????????????"
                    chipId = id
                    viewModel.event.type = 0
                }
                R.id.chip_birth -> {
                    tv_event_icon.text = "\uE6EB"
                    tv_event_icon.textColorResource = R.color.blue
                    et_event.hint = "??????"
                    et_date.hint = "????????????"
                    chipId = id
                    viewModel.event.type = 1
                }
                R.id.chip_lunar_birth -> {
                    tv_event_icon.text = "\uE6EB"
                    tv_event_icon.textColorResource = R.color.blue
                    et_event.hint = "??????"
                    et_date.hint = "????????????"
                    chipId = id
                    viewModel.event.type = 2
                }
                R.id.chip_rest -> {
                    tv_event_icon.text = "\uE6AC"
                    tv_event_icon.textColorResource = R.color.deepOrange
                    et_event.hint = "??????"
                    et_date.hint = "????????????"
                    chipId = id
                    viewModel.event.type = 3
                }
                else -> {
//                    chipGroup.find<Chip>(chipId).isChecked = true
                }
            }
            refreshPreview()
        }

        ll_fav.setOnClickListener {
            s_fav.isChecked = !s_fav.isChecked
        }

        s_fav.setOnCheckedChangeListener { _, isChecked ->
            viewModel.event.isFav = isChecked
        }

        ll_date.setOnClickListener {
            SelectDayDialog.newInstance().show(supportFragmentManager, null)
        }

        et_event.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.event.content = s.toString()
                refreshPreview()
            }
        })

        et_msg.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.event.msg = s.toString()
                refreshPreview()
            }
        })
    }

    fun initView() {
        (cg_type.getChildAt(viewModel.event.type) as Chip).isChecked = true
        refreshPreview()
        et_event.setText(viewModel.event.content)
        et_date.text = "${viewModel.event.year} - ${viewModel.event.month + 1} - ${viewModel.event.day}"
        et_msg.setText(viewModel.event.msg)
        s_fav.isChecked = viewModel.event.isFav
        showImage()
    }

    private fun refreshPreview() {
        val description = viewModel.event.getDescriptionWithDays(this)
        tv_content.text = description[0]
        tv_count.text = description[1]
        tv_date.text = description[2]

        if (viewModel.event.msg.isBlank()) {
            tv_msg.visibility = View.GONE
        } else {
            tv_msg.visibility = View.VISIBLE
            tv_msg.text = viewModel.event.msg
        }
    }

    private fun pickImage() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        } else {
            Matisse.from(this)
                .choose(setOf(MimeType.GIF, MimeType.PNG, MimeType.JPEG))
                .countable(true)
                .maxSelectable(1)
                .gridExpectedSize(dip(120))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(GlideAppEngine())
                .forResult(REQUEST_CODE_CHOOSE_BG)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Matisse.from(this)
                        .choose(setOf(MimeType.GIF, MimeType.PNG, MimeType.JPEG))
                        .countable(true)
                        .maxSelectable(1)
                        .gridExpectedSize(dip(120))
                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                        .thumbnailScale(0.85f)
                        .imageEngine(GlideAppEngine())
                        .forResult(REQUEST_CODE_CHOOSE_BG)
                } else {
                    longToast("???????????????????????????????????????")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CHOOSE_BG && resultCode == RESULT_OK) {
            viewModel.event.path = Matisse.obtainResult(data)[0].toString()
            showImage()
        }
    }

    private fun showImage() {
        if (viewModel.event.path.isBlank()) {
            Glide.with(this)
                .load(R.drawable.default_background)
                .override(300)
                .apply(bitmapTransform(BlurTransformation(25)))
                .into(iv_pic_bg)

        } else {
            Glide.with(this)
                .load(if (viewModel.event.path.isBlank()) R.drawable.default_background else viewModel.event.path)
                .override(300)
                .listener(
                    GlidePalette
                        .with(viewModel.event.path)
                        .intoCallBack {
                            val color = it!!.getVibrantColor(ContextCompat.getColor(this, R.color.blue_50))
                            tv_content.setTextColor(color)
                        }
                )
                .transform(MultiTransformation(CenterCrop(), RoundedCorners(50)))
                .into(iv_pic)
            Glide.with(this)
                .load(if (viewModel.event.path.isBlank()) R.drawable.default_background else viewModel.event.path)
                .override(300)
                .apply(bitmapTransform(BlurTransformation(50))).listener(
                    GlidePalette
                        .with(viewModel.event.path)
                        .intoCallBack {
                            val colorMatrix = ColorMatrix()
                            colorMatrix.setSaturation(1.8f)
                            val filter = ColorMatrixColorFilter(colorMatrix)
                           iv_pic_bg.colorFilter = filter
                        }
                )
                .into(iv_pic_bg)
        }
    }

    override fun onBackPressed() {
        if (makeSure == 0) {
            ll_date.longSnackbar("????????????????????????")
            makeSure++
            launch {
                delay(5000)
                makeSure = 0
            }
        } else {
            super.onBackPressed()
        }
    }
}
