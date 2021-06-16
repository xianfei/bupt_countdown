package cn.edu.bupt.countdown


import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.CheckedTextView
import android.widget.Toast
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.BaseDialogFragment
import androidx.lifecycle.ViewModelProviders
import cn.edu.bupt.countdown.event_add.AddEventActivity
import cn.edu.bupt.countdown.main.MainViewModel
import kotlinx.android.synthetic.main.fragment_add.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.textColorResource
import okhttp3.*
import java.io.*
import androidx.appcompat.app.AppCompatActivity
import cn.edu.bupt.countdown.bean.EventBean
import kotlinx.coroutines.runBlocking


class AddFragment : BaseDialogFragment() {

    override val layoutId: Int
        get() = R.layout.fragment_add

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
    }

    private fun CheckedTextView.toCheckedStyle() {
        this.typeface = Typeface.DEFAULT_BOLD
        this.textColorResource = R.color.colorAccent
        this.isChecked = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_add_new.onClick {
            val intent = Intent(activity!!, AddEventActivity::class.java)
            startActivity(intent)
            dismiss()
        }
        btn_add_QR.onClick {
            val intent = Intent(activity!!, ScanQrActivity::class.java)
            startActivity(intent)
            dismiss()
        }
        btn_add_url.onClick {
            val et = EditText(activity!!);

            AlertDialog.Builder(activity!!).setTitle("请输入URL")
                .setView(et)
                .setPositiveButton(
                    "确定",
                    DialogInterface.OnClickListener() { dialog: DialogInterface, which: Int ->
                        run {
                            val input = et.getText().toString();
                            if (input.equals("")) {
                                Toast.makeText(
                                    activity!!,
                                    "搜索内容不能为空！" + input,
                                    Toast.LENGTH_LONG
                                ).show();
                            } else {
                                val client = OkHttpClient()
                                val request = Request.Builder()
                                    .url(input)
                                    .build()

                                client.newCall(request).enqueue(object : Callback {
                                    override fun onFailure(call: Call, e: IOException) {

                                    }

                                    override fun onResponse(call: Call, response: Response) {
                                        for (s in response.body!!.string().split("##")) {
                                            val array = s.split("::")
                                            val database =
                                                AppDatabase.getDatabase(activity!!.application)
                                            val eventDao = database.eventDao()
                                            val event = EventBean(
                                                content = array[0],
                                                type = array[1].toInt(),
                                                year = array[2].toInt(),
                                                month = array[3].toInt(),
                                                day = array[4].toInt(),
                                                msg = array[5],
                                                path = array[6]
                                            )
                                            runBlocking {
                                                eventDao.insert(event)
                                            }
                                        }
                                    }
                                })

                            }
                        }
                    })
                .setNegativeButton("取消", null).show()
        }
        btn_add_text.onClick {
            val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboard.primaryClip?.getItemAt(0)?.text?.split("::")
            if (clip == null) {
                Toast.makeText(context, "剪贴板不能为空哦", Toast.LENGTH_LONG).show()
                dismiss()
            } else if (clip.size != 7) {
                Toast.makeText(context, "导入数据格式不对哦", Toast.LENGTH_LONG).show()
                dismiss()
            }
            val database = AppDatabase.getDatabase(context!!)
            val eventDao = database.eventDao()
            val array = clip!!
            val event = EventBean(
                content = array[0],
                type = array[1].toInt(),
                year = array[2].toInt(),
                month = array[3].toInt(),
                day = array[4].toInt(),
                msg = array[5]
            )
            Toast.makeText(context, "${array}", Toast.LENGTH_LONG).show()
            withContext(Dispatchers.IO) {
                eventDao.insert(event)
            }
            dismiss()
        }
    }
}
