package cn.edu.bupt.countdown.share

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import cn.edu.bupt.countdown.R
import cn.edu.bupt.countdown.base_view.BaseTitleActivity
import cn.edu.bupt.countdown.bean.EventBean
import cn.edu.bupt.countdown.event_appwidget.AppWidgetConfigureViewModel
import cn.edu.bupt.countdown.main.EventListAdapter
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import gdut.bsx.share2.FileUtil
import gdut.bsx.share2.Share2
import gdut.bsx.share2.ShareContentType
import kotlinx.android.synthetic.main.activity_add_event.*
import kotlinx.android.synthetic.main.activity_share_event.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.*
import org.jetbrains.anko.design.longSnackbar
import java.io.File
import com.github.florent37.glidepalette.GlidePalette
import cn.edu.bupt.countdown.utils.ColorPalette


class ShareEventActivity : BaseTitleActivity() {

    override val layoutId: Int
        get() = R.layout.activity_share_event

    private lateinit var viewModel: AppWidgetConfigureViewModel
    private var maskColor: Int = 0

    override fun onSetupSubButton(tvButton1: TextView, tvButton2: TextView) {
        val iconFont = ResourcesCompat.getFont(this, R.font.iconfont)
        tvButton2.textSize = 20f
        tvButton2.typeface = iconFont
        tvButton2.text = "\uE6DE"
        tvButton2.setOnClickListener {
            if (viewModel.selectedEvent == null) {
                it.longSnackbar("你还没有选择事件哦，往下划动看看。")
            } else {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                } else {
                    exportPicture()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AppWidgetConfigureViewModel::class.java)
        if (intent.extras?.getParcelable<EventBean>("event") == null) {

        } else {
            viewModel.selectedEvent = intent.extras!!.getParcelable("event")!!
            viewModel.widgetBean.eventId = viewModel.selectedEvent!!.id
            setPreviewContent()
        }
        maskColor = ContextCompat.getColor(this, R.color.maskBlack)
        initEvent()
        //initList()
    }

    private fun exportPicture() {
        launch {
            val screenshot = Bitmap.createBitmap(ll_share.width, ll_share.height, Bitmap.Config.ARGB_8888)
            val c = Canvas(screenshot)
            ll_share.draw(c)
            val task = withContext(Dispatchers.IO) {
                try {
                    val mPicDir =
                        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "倒数日")
//                    val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath
//                    val myDir = if (dir.endsWith(File.separator)) {
//                        "${dir}倒数日/"
//                    } else {
//                        "$dir/倒数日/"
//                    }
//                    val dirFile = File(myDir)
//                    if (!dirFile.exists()) {
//                        dirFile.mkdir()
//                    }
                    mPicDir.mkdirs()
                    val mImageTime = System.currentTimeMillis()
                    val dateSeconds = mImageTime / 1000
                    val mImageFileName = "pic_$mImageTime.png"
                    val mImageFilePath = File(mPicDir, mImageFileName).absolutePath
                    val values = ContentValues()
                    values.put(MediaStore.Images.ImageColumns.DATA, mImageFilePath)
                    values.put(MediaStore.Images.ImageColumns.TITLE, mImageFileName)
                    values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, mImageFileName)
                    values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, mImageTime)
                    values.put(MediaStore.Images.ImageColumns.DATE_ADDED, dateSeconds)
                    values.put(MediaStore.Images.ImageColumns.DATE_MODIFIED, dateSeconds)
                    values.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/png")
                    values.put(MediaStore.Images.ImageColumns.WIDTH, screenshot.width)
                    values.put(MediaStore.Images.ImageColumns.HEIGHT, screenshot.height)
                    val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

                    val out = contentResolver.openOutputStream(uri!!)
                    screenshot.compress(Bitmap.CompressFormat.PNG, 100, out)// bitmap转换成输出流，写入文件
                    out?.flush()
                    out?.close()

                    values.clear()
                    values.put(
                        MediaStore.Images.ImageColumns.SIZE,
                        File(mImageFilePath).length()
                    )
                    contentResolver.update(uri, values, null, null)
                    mImageFilePath
                } catch (e: Exception) {
                    "error: ${e.message}"
                }
            }
            if (task.startsWith("error:")) {
                longToast("出现异常>_<\n$task")
            } else {
                longToast("已保存在相册「倒数日」中")
                Share2.Builder(this@ShareEventActivity)
                    .setContentType(ShareContentType.FILE)
                    .setShareFileUri(FileUtil.getFileUri(this@ShareEventActivity, ShareContentType.IMAGE, File(task)))
                    .setTitle("导出并分享")
                    .build()
                    .shareBySystem()
            }
        }

    }

    private fun initEvent() {

        ll_with_logo.setOnClickListener {
            s_logo.isChecked = !s_logo.isChecked
        }

        s_logo.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                ll_logo.visibility = View.VISIBLE
            } else {
                ll_logo.visibility = View.INVISIBLE
            }
        }

        ll_mask_color.setOnClickListener {
            buildColorPickerDialogBuilder()
                .initialColor(maskColor)
                .setPositiveButton("确定") { _, colorInt, _ ->
                    maskColor = colorInt
                    v_share.backgroundColor = colorInt
                }
                .build()
                .show()
        }

        ll_text_color.setOnClickListener {
            buildColorPickerDialogBuilder()
                .initialColor(tv_days.currentTextColor)
                .setPositiveButton("确定") { _, colorInt, _ ->
                    for (i in 0 until ll_info.childCount) {
                        val v = ll_info.getChildAt(i)
                        if (v is TextView) {
                            v.textColor = colorInt
                        }
                    }
                }
                .build()
                .show()
        }
    }

    private fun initList() {
        launch {
            viewModel.showList.addAll(withContext(Dispatchers.IO) {
                viewModel.getAllEvents()
            })
            val adapter = EventListAdapter(R.layout.item_event, viewModel.showList)
            adapter.setOnItemClickListener { _, _, position ->
                viewModel.selectedEvent = viewModel.showList[position]
                viewModel.widgetBean.eventId = viewModel.selectedEvent!!.id
                setPreviewContent()
                scrollToTop()
            }
            adapter.emptyView = UI {
                frameLayout {

                    verticalLayout {

                        imageView(R.drawable.ic_smileface) {
                            scaleType = ImageView.ScaleType.CENTER_CROP
                        }.lparams(dip(240), dip(240))

                        textView("这里空空的呢\n要不试试加点东西？") {
                            gravity = Gravity.CENTER
                        }.lparams(matchParent, wrapContent) {
                            topMargin = -dip(64)
                        }

                    }.lparams(wrapContent, wrapContent) {
                        gravity = Gravity.CENTER
                    }
                }
            }.view
        }
    }

    private fun setPreviewContent() {
        val description = viewModel.selectedEvent!!.getDescriptionWithDays(this)
        tv_share_event.text = description[0]
        tv_days.text = description[1]
        tv_start_time.text = description[2]
        if (viewModel.selectedEvent!!.msg.isBlank()) {
            tv_msg.visibility = View.GONE
        } else {
            tv_msg.visibility = View.VISIBLE
            tv_msg.text = viewModel.selectedEvent!!.msg
        }
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(
                ""+viewModel.selectedEvent!!.content+"::"
                        +viewModel.selectedEvent!!.type+"::"
                        +viewModel.selectedEvent!!.year+"::"
                        +viewModel.selectedEvent!!.month+"::"
                        +viewModel.selectedEvent!!.day+"::"
                        +viewModel.selectedEvent!!.msg+"::"
                , BarcodeFormat.QR_CODE, 400, 400)
//            val imageViewQrCode = findViewById<View>(R.id.qrCode) as ImageView
            qrCode.setImageBitmap(bitmap)
        } catch (e: java.lang.Exception) {
        }
        Glide.with(this)
            .load(if (viewModel.selectedEvent!!.path.isBlank()) R.drawable.default_background else viewModel.selectedEvent!!.path)
            .override(600)
            .listener(
                GlidePalette
                    .with(viewModel.selectedEvent!!.path)
                    .intoCallBack {
                        val colorP = ColorPalette(it!!)
                        ll_share.backgroundColor = colorP.getColor1BG()
                    }
            )
            .into(iv_share)
    }

    private fun buildColorPickerDialogBuilder(): ColorPickerDialogBuilder {
        return ColorPickerDialogBuilder
            .with(this)
            .setTitle("选取颜色")
            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
            .density(12)
            .setNegativeButton("取消") { _, _ -> }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    exportPicture()
                } else {
                    longToast("你取消了授权，无法分享图片")
                }
            }
        }
    }

}
