package androidx.fragment.app

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.LayoutRes
import com.google.android.material.card.MaterialCardView
import cn.edu.bupt.countdown.R
import fr.tvbarthel.lib.blurdialogfragment.SupportBlurDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.jetbrains.anko.find
import kotlin.coroutines.CoroutineContext

abstract class BaseDialogFragment : SupportBlurDialogFragment(), CoroutineScope {

    private lateinit var job: Job
    public var bgColor = Color.argb(50,0,0,0)
    var layoutWidth = 280

    @get:LayoutRes
    protected abstract val layoutId: Int

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        activity!!.window.navigationBarColor = Color.parseColor("#cdcdcd")
        // dialog?.window?.setLayout(dip(layoutWidth), ViewGroup.LayoutParams.WRAP_CONTENT)
        val root = inflater.inflate(R.layout.fragment_base_dialog, container, false)
        val cardView = root.find<MaterialCardView>(R.id.base_card_view)
        LayoutInflater.from(context).inflate(layoutId, cardView, true)
        return root
    }

    override fun show(manager: androidx.fragment.app.FragmentManager, tag: String?) {
//        mDismissed = false
//        mShownByMe = true
        val ft = manager.beginTransaction()
        ft.add(this, tag)
        ft.commitAllowingStateLoss()
    }

    override fun onDestroy() {
        super.onDestroy()
        activity!!.window.navigationBarColor = Color.WHITE
        job.cancel()
    }

    override fun getDownScaleFactor(): Float {
        // Allow to customize the down scale factor.
        return 6f
    }

    override fun getBlurRadius(): Int {
        // Allow to customize the blur radius factor.
        return 20
    }

    override fun isActionBarBlurred(): Boolean {
        // Enable or disable the blur effect on the action bar.
        // Disabled by default.
        return true
    }

    override fun isRenderScriptEnable(): Boolean {
        // Enable or disable the use of RenderScript for blurring effect
        // Disabled by default.
        return true
    }

    override fun getColor(): Int {
        // Allow to customize the blur background color.
        return bgColor
//        return Color.TRANSPARENT
    }
}