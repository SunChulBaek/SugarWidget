package kr.pe.ssun.sugarwidget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Telephony
import android.util.Log

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("SmsReceiver", "onReceive action: ${intent.action}")

        when (intent.action) {
            Telephony.Sms.Intents.SMS_RECEIVED_ACTION -> {
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                for (sms in messages) {
                    val sender = sms.displayOriginatingAddress
                    val body = sms.displayMessageBody
                    processMessage(context, sender, body)
                }
            }
            Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION -> {
                // MMS는 내용이 바로 오지 않으므로 ContentProvider를 통해 조회해야 할 수도 있으나,
                // 시스템이 MMS 수신 후 DB에 저장할 때까지 시간이 걸릴 수 있습니다.
                // 여기서는 우선 최근 메시지를 조회하는 방식을 시도합니다.
                Thread {
                    try {
                        Thread.sleep(2000) // DB 저장 대기
                        queryLatestMms(context)
                    } catch (e: Exception) {
                        Log.e("SmsReceiver", "MMS query error", e)
                    }
                }.start()
            }
        }
    }

    private fun processMessage(context: Context, sender: String?, body: String?) {
        Log.d("SmsReceiver", "Process - Sender: $sender, Body: $body")
        if (sender != null && (sender.contains("15661246") || sender.contains("1566-1246"))) {
            if (body != null && body.contains("사용량 조회")) {
                parseAndSave(context, body)
                updateWidget(context)
            }
        }
    }

    private fun queryLatestMms(context: Context) {
        val uri = Uri.parse("content://mms/inbox")
        val projection = arrayOf("_id", "date")
        val cursor = context.contentResolver.query(uri, projection, null, null, "date DESC LIMIT 1")

        cursor?.use {
            if (it.moveToFirst()) {
                val mmsId = it.getString(it.getColumnIndexOrThrow("_id"))
                val body = getMmsText(context, mmsId)
                val sender = getMmsSender(context, mmsId)
                processMessage(context, sender, body)
            }
        }
    }

    private fun getMmsText(context: Context, id: String): String {
        val partUri = Uri.parse("content://mms/part")
        val cursor = context.contentResolver.query(partUri, null, "mid = ?", arrayOf(id), null)
        val sb = StringBuilder()
        cursor?.use {
            while (it.moveToNext()) {
                val type = it.getString(it.getColumnIndexOrThrow("ct"))
                if (type == "text/plain") {
                    val data = it.getString(it.getColumnIndexOrThrow("_data"))
                    val text = if (data != null) {
                        // 파일로 저장된 경우 읽기 (생략 가능성이 높음)
                        ""
                    } else {
                        it.getString(it.getColumnIndexOrThrow("text"))
                    }
                    sb.append(text)
                }
            }
        }
        return sb.toString()
    }

    private fun getMmsSender(context: Context, id: String): String {
        val addrUri = Uri.parse("content://mms/$id/addr")
        val cursor = context.contentResolver.query(addrUri, null, "msg_id = ?", arrayOf(id), null)
        var sender = ""
        cursor?.use {
            while (it.moveToNext()) {
                val address = it.getString(it.getColumnIndexOrThrow("address"))
                if (address != null && address != "insert-address-token") {
                    sender = address
                    break
                }
            }
        }
        return sender
    }

    private fun parseAndSave(context: Context, body: String) {
        try {
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
        if (ids.isNotEmpty()) {
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }
}
