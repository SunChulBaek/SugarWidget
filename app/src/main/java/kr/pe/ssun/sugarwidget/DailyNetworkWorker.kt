package kr.pe.ssun.sugarwidget

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class DailyNetworkWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val sharedPref = applicationContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val phoneNum = sharedPref.getString("phone_number", "") ?: ""
        val persNo = sharedPref.getString("birth_date", "") ?: ""

        if (phoneNum.isEmpty() || persNo.isEmpty()) {
            Log.d("DailyNetworkWorker", "저장된 정보가 없어 작업을 수행하지 않습니다.")
            return Result.success()
        }

        val client = OkHttpClient()
        val url = "https://www.sugarmobile.co.kr/used_info.do?phoneNum=$phoneNum&persNo=$persNo"
        
        val request = Request.Builder()
            .url(url)
            .post("".toRequestBody()) // 빈 본문으로 POST 요청
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Log.d("DailyNetworkWorker", "네트워크 요청 성공: ${response.code}")
                    Result.success()
                } else {
                    Log.e("DailyNetworkWorker", "네트워크 요청 실패: ${response.code}")
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Log.e("DailyNetworkWorker", "네트워크 요청 중 오류 발생", e)
            Result.retry()
        }
    }
}
