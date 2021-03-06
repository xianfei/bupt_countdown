package cn.edu.bupt.countdown.event_appwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.TypedValue.COMPLEX_UNIT_SP
import android.view.View
import android.widget.RemoteViews
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.AppWidgetTarget
import cn.edu.bupt.countdown.AppDatabase
import cn.edu.bupt.countdown.R
import cn.edu.bupt.countdown.bean.EventBean
import cn.edu.bupt.countdown.bean.SingleAppWidgetBean
import kotlinx.coroutines.*

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [EventAppWidgetConfigureActivity]
 */
class EventAppWidget : AppWidgetProvider() {

    private var job: Job? = null

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val dataBase = AppDatabase.getDatabase(context)
        val widgetDao = dataBase.singleWidgetDao()
        val eventDao = dataBase.eventDao()

        job = GlobalScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                for (appWidget in widgetDao.getAll()) {
                    val e = eventDao.getByIdInThread(appWidget.eventId)
                    withContext(Dispatchers.Main) {
                        updateAppWidget(context, appWidgetManager, appWidget.id, e, appWidget)
                    }
                }
            }
            job?.cancel()
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val dataBase = AppDatabase.getDatabase(context)
        val widgetDao = dataBase.singleWidgetDao()
        job = GlobalScope.launch(Dispatchers.IO) {
            for (id in appWidgetIds) {
                widgetDao.delete(id)
            }
            job?.cancel()
        }
    }

    companion object {

        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int, event: EventBean, widget: SingleAppWidgetBean
        ) {
            val mRemoteViews = RemoteViews(context.packageName, R.layout.event_app_widget_0 + widget.weight)
            mRemoteViews.setTextViewTextSize(R.id.tv_event_widget, COMPLEX_UNIT_SP, widget.contentSize.toFloat())
            val description = event.getDescriptionWithDays(context)
            if (widget.textHorizontal) {
                mRemoteViews.setViewVisibility(R.id.tv_days_widget, View.GONE)
                mRemoteViews.setTextViewText(R.id.tv_event_widget, description[0] + description[1])
            } else {
                mRemoteViews.setTextViewTextSize(R.id.tv_days_widget, COMPLEX_UNIT_SP, widget.daySize.toFloat())
                mRemoteViews.setViewVisibility(R.id.tv_days_widget, View.VISIBLE)
                mRemoteViews.setTextViewText(R.id.tv_event_widget, description[0])
                mRemoteViews.setTextViewText(R.id.tv_days_widget, description[1])
            }
            if (event.msg.isBlank()) {
                mRemoteViews.setViewVisibility(R.id.tv_event_msg, View.GONE)
            } else {
                mRemoteViews.setTextViewTextSize(R.id.tv_event_msg, COMPLEX_UNIT_SP, widget.msgSize.toFloat())
                mRemoteViews.setViewVisibility(R.id.tv_event_msg, View.VISIBLE)
                mRemoteViews.setTextViewText(R.id.tv_event_msg, event.msg)
            }
            if (widget.withPic) {
                mRemoteViews.setViewVisibility(R.id.iv_widget, View.VISIBLE)
                val appWidgetTarget = AppWidgetTarget(context, R.id.iv_widget, mRemoteViews, appWidgetId)
                Glide.with(context)
                    .asBitmap()
                    .load(if (event.path.isBlank()) R.drawable.default_background else event.path)
                    .override(300)
                    .into(appWidgetTarget)
            } else {
                mRemoteViews.setViewVisibility(R.id.iv_widget, View.GONE)
            }
            mRemoteViews.setInt(R.id.ll_background, "setBackgroundColor", widget.bgColor)
            if (widget.weight == 0) {
                mRemoteViews.setInt(R.id.rl_background, "setBackgroundColor", widget.bgColor)
            }
            mRemoteViews.setTextColor(R.id.tv_event_widget, widget.textColor)
            mRemoteViews.setTextColor(R.id.tv_days_widget, widget.textColor)
            mRemoteViews.setTextColor(R.id.tv_event_msg, widget.textColor)
            val intent = Intent(context, EventAppWidgetConfigureActivity::class.java)
            intent.putExtra("modify", true)
            intent.putExtra("widgetData", widget)
            val pIntent = PendingIntent.getActivity(context, widget.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            mRemoteViews.setOnClickPendingIntent(R.id.ll_background, pIntent)
            appWidgetManager.updateAppWidget(appWidgetId, mRemoteViews)
        }
    }
}

