package com.example.taskmanagerandproductivitytracker.reminders


import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.taskmanagerandproductivitytracker.network.ApiClient
import org.json.JSONObject

class DailySummaryWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    companion object {
        const val USER_ID_KEY = "USER_ID"
        const val WORK_NAME = "DailySummaryNotification"
    }

    override suspend fun doWork(): Result {
        val userId = inputData.getInt(USER_ID_KEY, -1)
        if (userId == -1) {
            Log.e(WORK_NAME, "Work failed: Missing User ID")
            return Result.failure()
        }

        Log.d(WORK_NAME, "Worker started for user ID: $userId")

        val result = ApiClient.getDailySummary(userId)

        result.onSuccess { responseBody ->
            return try {
                val json = JSONObject(responseBody)
                val totalSeconds = json.getLong("total_seconds")
                SummaryNotification.show(applicationContext, totalSeconds)
                Log.d(WORK_NAME, "Work success: Notified user with total seconds: $totalSeconds")
                Result.success()
            } catch (e: Exception) {
                Log.e(WORK_NAME, "Work failed: JSON parsing error", e)
                Result.failure()
            }
        }.onFailure { error ->
            Log.e(WORK_NAME, "Work failed: API call error", error)
            return Result.retry()
        }

        return Result.failure()
    }
}