package cn.edu.bupt.countdown.main

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback
import com.chad.library.adapter.base.listener.OnItemDragListener
import com.github.florent37.glidepalette.BitmapPalette
import com.github.florent37.glidepalette.GlidePalette
import com.google.android.material.appbar.AppBarLayout
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import cn.edu.bupt.countdown.*
import cn.edu.bupt.countdown.base_view.BaseActivity
import cn.edu.bupt.countdown.bean.EventBean
import cn.edu.bupt.countdown.bean.EventOldBean
import cn.edu.bupt.countdown.bean.SolarBean
import cn.edu.bupt.countdown.bean.UpdateInfoBean
import cn.edu.bupt.countdown.utils.*
import cn.edu.bupt.countdown.utils.MyRetrofitUtils
import cn.edu.bupt.countdown.utils.PreferenceUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.tv_msg
import kotlinx.android.synthetic.main.fragment_detail.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonException
import kotlinx.serialization.list
import okhttp3.ResponseBody
import org.jetbrains.anko.*
import org.jetbrains.anko.design.longSnackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.math.abs


class MainActivity : BaseActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: EventListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        lightStatusIcon = false
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        updateFromOldVersion()
        setContentView(R.layout.activity_main)
        resizeMarginTop()
        initView()
        initEvent()
        drawer_layout.backgroundColor = PreferenceUtils.getIntFromSP(applicationContext, "nav_bar_color", Color.WHITE)
        MyRetrofitUtils.instance.getService().getUpdateInfo().enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {}

            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                if (response!!.body() != null) {
                    try {
                        val updateInfo = viewModel.json.parse(
                            UpdateInfoBean.serializer(),
                            response.body()!!.string()
                        )
                    } catch (e: JsonException) {

                    }
                }
            }
        })

        viewModel.sortTypeLiveData.value = PreferenceUtils.getIntFromSP(this@MainActivity, "sortType", 0)
    }

    override fun onStart() {
        super.onStart()
        launch {
            viewModel.favEvent = withContext(Dispatchers.IO) {
                viewModel.getFavEventInThread()
            }
            if (viewModel.favEvent == null) {
               cv_event.visibility = View.GONE
//                tv_start_time.visibility = View.GONE
            } else {
                cv_event.visibility = View.VISIBLE
//                tv_start_time.visibility = View.VISIBLE
                val description = viewModel.favEvent!!.getDescriptionWithDays(this@MainActivity)
                tv_content.text = description[0]
                val strs = description[1].split(" ")
                val str = strs[0] + "<myfont size='180px'>"+strs[1]+"</myfont>"+strs[2]
                tv_count.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(str, HtmlCompat.FROM_HTML_MODE_COMPACT,null, HtmlTagHandler("myfont"))
                } else {
                    Html.fromHtml(str,null, HtmlTagHandler("myfont"))
                }
//                tv_count.text = description[1]
                tv_date.text = description[2]
                tv_msg.text = viewModel.favEvent!!.msg
                cv_event.setOnClickListener {
                    DetailFragment.newInstance(viewModel.favEvent!!).show(supportFragmentManager, "detail")
                }
//                if (viewModel.favEvent!!.type == 0 && PreferenceUtils.getBooleanFromSP(
//                        this@MainActivity,
//                        "s_day_plus",
//                        false
//                    )
//                ) {
//                    tv_plus.visibility = View.VISIBLE
//                    tv_days.setContent("${viewModel.favEvent!!.count + 1}")
//                } else {
//                    tv_plus.visibility = View.GONE
//                    tv_days.setContent("${viewModel.favEvent!!.count}")
//                }
//                tv_start_time.text = description[2]
//                tv_event_main.text = description[0] + " " + description[1]
                Glide.with(this@MainActivity)
                    .load(if (viewModel.favEvent!!.path.isBlank()) cn.edu.bupt.countdown.R.drawable.default_background else viewModel.favEvent!!.path)
                    .override(600)
                    .listener(
                        GlidePalette
                            .with(viewModel.favEvent!!.path)
                            .use(BitmapPalette.Profile.MUTED_DARK)
                            .intoBackground(v_bg)
                    )
                    .into(iv_pic)
            }
        }
    }

    private fun updateFromOldVersion() {
        val eventJson = PreferenceUtils.getStringFromSP(applicationContext, "events", "")
        if (eventJson != "") {
            val oldList = viewModel.json.parse(EventOldBean.serializer().list, eventJson!!)
            val newList = arrayListOf<EventBean>()
            oldList.forEach {
                val d = it.date.split('-')
                val mYear = d[0].toInt()
                val mMonth = d[1].toInt()
                val mDay = d[2].toInt()
                val newEvent = EventBean().apply {
                    this.content = it.context
                    this.path = it.picture_path
                    this.isFav = it.isFavourite
                }
                when (it.type) {
                    // 农历生日，对应新的2
                    3 -> {
                        val solar = SolarBean(mYear, mMonth, mDay)
                        val lunar = LunarUtils.solarToLunar(solar)
                        newEvent.year = lunar.lunarYear
                        newEvent.month = lunar.lunarMonth - 1
                        newEvent.day = lunar.lunarDay
                        newEvent.type = 2
                        newList.add(newEvent)
                    }
                    2 -> {
                        newEvent.year = mYear
                        newEvent.month = mMonth - 1
                        newEvent.day = mDay
                        newEvent.type = 3
                        newList.add(newEvent)
                    }
                    else -> {
                        newEvent.year = mYear
                        newEvent.month = mMonth - 1
                        newEvent.day = mDay
                        newEvent.type = it.type
                        newList.add(newEvent)
                    }
                }
            }
            launch {
                val msg = withContext(Dispatchers.IO) {
                    try {
                        viewModel.insertEvents(newList)
                        "ok"
                    } catch (e: Exception) {
                        "发生异常>_<" + e.message
                    }
                }
                if (msg == "ok") {
                    PreferenceUtils.remove(applicationContext, "events")
                    TipsFragment.newInstance(
                        "发现你是由旧版本升级上来的哦，注意你从旧版本放置在桌面的<b><font color='#1976D2'>小部件全部失效</font></b>请重新放置。<br>另外为了小部件正常工作，请<b><font color='#1976D2'>允许App保持后台，加入节电白名单</font></b>。",
                        "v1.998"
                    )
                        .show(supportFragmentManager, "tips")
                } else {
                    longToast(msg)
                }
            }

        }
    }


    private fun initView() {
        adapter = EventListAdapter(R.layout.item_event, viewModel.showList)
        adapter.setOnItemClickListener { _, _, position ->
            // TODO: 添加显示页面显示详情
//            startActivity<AddEventActivity>("event" to viewModel.showList[position])
            DetailFragment.newInstance(viewModel.showList[position]).show(supportFragmentManager, "detail")
        }
//        adapter.addHeaderView(UI {
//            view {
//                minimumHeight = dip(24)
//            }
//        }.view)
        adapter.addFooterView(UI {
            view {
                minimumHeight = dip(56)
            }
        }.view)

        // 当事件列表为空
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
        rv_events.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        rv_events.adapter = adapter
        val itemDragAndSwipeCallback = ItemDragAndSwipeCallback(adapter)
        val itemTouchHelper = ItemTouchHelper(itemDragAndSwipeCallback)
        itemTouchHelper.attachToRecyclerView(rv_events)
        val onItemDragListener = object : OnItemDragListener {

            override fun onItemDragMoving(
                source: androidx.recyclerview.widget.RecyclerView.ViewHolder,
                from: Int,
                target: androidx.recyclerview.widget.RecyclerView.ViewHolder,
                to: Int
            ) {
            }

            override fun onItemDragStart(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, pos: Int) {
                if (viewModel.sortTypeLiveData.value != 1) {
                    contentView!!.longSnackbar("在当前的排序方式下更改不会保存哦，请在右上角切换")
                }
            }

            override fun onItemDragEnd(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, pos: Int) {
                if (viewModel.sortTypeLiveData.value == 1) {
                    launch {
                        progress_bar.visibility = View.VISIBLE
                        window.setFlags(
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        )
                        val task = withContext(Dispatchers.IO) {
                            try {
                                viewModel.showList.forEachIndexed { index, eventBean ->
                                    eventBean.sortNum = index
                                }
                                viewModel.updateEvents(viewModel.showList)
                                "ok"
                            } catch (e: Exception) {
                                "发生异常>_<" + e.message
                            }
                        }
                        if (task != "ok") {
                            longToast(task)
                        }
                        progress_bar.visibility = View.GONE
                        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    }
                }
            }
        }
        adapter.enableDragItem(itemTouchHelper)
        adapter.setOnItemDragListener(onItemDragListener)
        viewModel.getAllEvents().observe(this, Observer { list ->
            if (list == null) return@Observer
            viewModel.showList.clear()
            viewModel.showList.addAll(list)
            when (viewModel.sortTypeLiveData.value) {
                0 -> viewModel.showList.sortBy { it.id }
                1 -> viewModel.showList.sortBy { it.sortNum }
                2 -> viewModel.showList.sortBy { abs(it.count) }
                3 -> viewModel.showList.sortByDescending { abs(it.count) }
                4 -> viewModel.showList.sortByDescending { it.id }
            }
            adapter.notifyDataSetChanged()
        })
        viewModel.sortTypeLiveData.observe(this, Observer { i ->
            if (i == null) return@Observer
            when (i) {
                0 -> viewModel.showList.sortBy { it.id }
                1 -> viewModel.showList.sortBy { it.sortNum }
                2 -> viewModel.showList.sortBy { abs(it.count) }
                3 -> viewModel.showList.sortByDescending { abs(it.count) }
                4 -> viewModel.showList.sortByDescending { it.id }
            }
            adapter.notifyDataSetChanged()
            PreferenceUtils.saveIntToSP(this, "sortType", i)
        })
    }

    private fun resizeMarginTop() {
        val statusBarMargin = getStatusBarHeight()
        (toolbar.layoutParams as FrameLayout.LayoutParams).topMargin = statusBarMargin
    }

    private fun initEvent() {

        tv_add.setOnClickListener {
//            startActivity<AddEventActivity>()
             cn.edu.bupt.countdown.AddFragment().show(supportFragmentManager, null)
        }

        tv_share.setOnClickListener {
            startActivity<SettingsActivity>()
        }

        tv_sort.setOnClickListener {
            SortFragment().show(supportFragmentManager, null)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    BackupFragment().show(supportFragmentManager, null)
                } else {
                    this.longToast("你取消了授权>_<无法备份还原")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val filePath = data!!.getStringExtra(FilePickerActivity.RESULT_FILE_PATH)
            launch {
                val import = withContext(Dispatchers.IO) {
                    try {
                        viewModel.importFromFile(filePath)
                    } catch (e: Exception) {
                        e.message
                    }
                }
                when (import) {
                    "ok" -> {
                        toast("导入成功(ﾟ▽ﾟ)/")
                    }
                    else -> longToast("发生异常>_<\n$import")
                }
            }
        }
    }

    override fun onBackPressed() {
        when {
            drawer_layout.isDrawerOpen(GravityCompat.START) -> drawer_layout.closeDrawer(GravityCompat.START)
            else -> super.onBackPressed()
        }
    }
}
