package kr.pe.ssun.sugarwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

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

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            // 즉시 갱신 요청 (OneTimeWork)
            val workRequest = OneTimeWorkRequestBuilder<DailyNetworkWorker>().build()
            WorkManager.getInstance(context).enqueue(workRequest)
            
            Toast.makeText(context, "사용량 조회를 요청했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val ACTION_REFRESH = "kr.pe.ssun.sugarwidget.ACTION_REFRESH"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val usage = UsagePrefs.getUsage(context)
            val views = RemoteViews(context.packageName, R.layout.usage_widget)

            views.setTextViewText(R.id.tv_data, "${usage["data_used"]}/${usage["data_total"]}G")
            views.setTextViewText(R.id.tv_voice, "${usage["voice_used"]}/${usage["voice_total"]}분")
            views.setTextViewText(R.id.tv_sms, "${usage["sms_used"]}/${usage["sms_total"]}건")

            // 새로고침 버튼에 PendingIntent 설정
            val intent = Intent(context, UsageWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_refresh, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
