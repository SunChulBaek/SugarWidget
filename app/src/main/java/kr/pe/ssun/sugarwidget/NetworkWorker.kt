package kr.pe.ssun.sugarwidget

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class NetworkWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val sharedPref = applicationContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val phoneNum = sharedPref.getString("phone_number", "") ?: ""
        val persNo = sharedPref.getString("birth_date", "") ?: ""

        if (phoneNum.isEmpty() || persNo.isEmpty()) {
            Log.d("NetworkWorker", "Phone number or birth date is missing. Skipping network call.")
            return Result.success()
        }

        return try {
            val url = "https://www.sugarmobile.co.kr/used_info.do?phoneNum=$phoneNum&persNo=$persNo"
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .post("".toRequestBody()) // Empty body for POST as requested parameters are in URL
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Log.d("NetworkWorker", "Network call successful: ${response.code}")
                    Result.success()
                } else {
                    Log.e("NetworkWorker", "Network call failed: ${response.code}")
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Log.e("NetworkWorker", "Error during network call", e)
            Result.retry()
        }
    }
}
