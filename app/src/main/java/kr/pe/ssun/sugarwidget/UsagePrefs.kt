package kr.pe.ssun.sugarwidget

import android.content.Context

object UsagePrefs {
    private const val PREF_NAME = "usage_prefs"
    private const val KEY_VOICE_USED = "voice_used"
    private const val KEY_VOICE_TOTAL = "voice_total"
    private const val KEY_DATA_USED = "data_used"
    private const val KEY_DATA_TOTAL = "data_total"
    private const val KEY_SMS_USED = "sms_used"
    private const val KEY_SMS_TOTAL = "sms_total"
    private const val KEY_LAST_UPDATE = "last_update"

    fun saveUsage(
        context: Context,
        voiceUsed: String, voiceTotal: String,
        dataUsed: String, dataTotal: String,
        smsUsed: String, smsTotal: String
    ) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_VOICE_USED, voiceUsed)
            putString(KEY_VOICE_TOTAL, voiceTotal)
            putString(KEY_DATA_USED, dataUsed)
            putString(KEY_DATA_TOTAL, dataTotal)
            putString(KEY_SMS_USED, smsUsed)
            putString(KEY_SMS_TOTAL, smsTotal)
            putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
            apply()
        }
    }

    fun getUsage(context: Context): Map<String, String> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return mapOf(
            "voice_used" to (prefs.getString(KEY_VOICE_USED, "0") ?: "0"),
            "voice_total" to (prefs.getString(KEY_VOICE_TOTAL, "0") ?: "0"),
            "data_used" to (prefs.getString(KEY_DATA_USED, "0") ?: "0"),
            "data_total" to (prefs.getString(KEY_DATA_TOTAL, "0") ?: "0"),
            "sms_used" to (prefs.getString(KEY_SMS_USED, "0") ?: "0"),
            "sms_total" to (prefs.getString(KEY_SMS_TOTAL, "0") ?: "0")
        )
    }
}
