package cn.edu.bupt.countdown.main

import androidx.lifecycle.ViewModelProviders
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.CheckedTextView
import androidx.fragment.app.BaseDialogFragment
import cn.edu.bupt.countdown.R
import kotlinx.android.synthetic.main.fragment_sort.*
import org.jetbrains.anko.textColorResource

class SortFragment : BaseDialogFragment(), View.OnClickListener {

    override val layoutId: Int
        get() = R.layout.fragment_sort

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
        when (viewModel.sortTypeLiveData.value) {
            0 -> btn_add_time.toCheckedStyle()
            1 -> btn_self.toCheckedStyle()
            2 -> btn_asc.toCheckedStyle()
            3 -> btn_desc.toCheckedStyle()
            4 -> btn_add_time_desc.toCheckedStyle()
        }
        btn_add_time.setOnClickListener(this)
        btn_self.setOnClickListener(this)
        btn_asc.setOnClickListener(this)
        btn_desc.setOnClickListener(this)
        btn_add_time_desc.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        viewModel.sortTypeLiveData.value = when (v.id) {
            R.id.btn_add_time -> 0
            R.id.btn_self -> 1
            R.id.btn_asc -> 2
            R.id.btn_desc -> 3
            R.id.btn_add_time_desc -> 4
            else -> 0
        }
        dismiss()
    }
}
