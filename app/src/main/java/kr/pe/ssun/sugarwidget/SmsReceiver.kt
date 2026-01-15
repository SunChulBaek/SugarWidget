package kr.pe.ssun.sugarwidget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in messages) {
                val sender = sms.displayOriginatingAddress
                val body = sms.displayMessageBody
                
                if (sender == "1566-1246" || sender == "15661246") {
                    parseAndSave(context, body)
                    updateWidget(context)
                }
            }
        }
    }

    private fun parseAndSave(context: Context, body: String) {
        try {
            // 음성 (분) - 0.6 / 193.6
            // 데이터 (GB) - 0.98 / 9.68
            // 문자 (건) - 30 / 130
            
            val voiceRegex = """음성\s*\(분\)\s*-\s*([\d.]+)\s*/\s*([\d.]+)""".toRegex()
            val dataRegex = """데이터\s*\(GB\)\s*-\s*([\d.]+)\s*/\s*([\d.]+)""".toRegex()
            val smsRegex = """문자\s*\(건\)\s*-\s*([\d.]+)\s*/\s*([\d.]+)""".toRegex()

            val voiceMatch = voiceRegex.find(body)
            val dataMatch = dataRegex.find(body)
            val smsMatch = smsRegex.find(body)

            val voiceUsed = voiceMatch?.groupValues?.get(1) ?: "0"
            val voiceTotal = voiceMatch?.groupValues?.get(2) ?: "0"
            
            val dataUsed = dataMatch?.groupValues?.get(1) ?: "0"
            val dataTotal = dataMatch?.groupValues?.get(2) ?: "0"
            
            val smsUsed = smsMatch?.groupValues?.get(1) ?: "0"
            val smsTotal = smsMatch?.groupValues?.get(2) ?: "0"

            UsagePrefs.saveUsage(context, voiceUsed, voiceTotal, dataUsed, dataTotal, smsUsed, smsTotal)
        } catch (e: Exception) {
            Log.e("SmsReceiver", "Parsing error", e)
        }
    }

    private fun updateWidget(context: Context) {
        val intent = Intent(context, UsageWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
            ComponentName(context, UsageWidgetProvider::class.java)
        )
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(intent)
    }
}
