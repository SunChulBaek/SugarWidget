package kr.pe.ssun.sugarwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews

class UsageWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val usage = UsagePrefs.getUsage(context)
            val views = RemoteViews(context.packageName, R.layout.usage_widget)

            // 1x1 사이즈에 맞춰 텍스트 포맷 간소화
            views.setTextViewText(R.id.tv_data, "${usage["data_used"]}/${usage["data_total"]}G")
            views.setTextViewText(R.id.tv_voice, "${usage["voice_used"]}/${usage["voice_total"]}분")
            views.setTextViewText(R.id.tv_sms, "${usage["sms_used"]}/${usage["sms_total"]}건")

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
