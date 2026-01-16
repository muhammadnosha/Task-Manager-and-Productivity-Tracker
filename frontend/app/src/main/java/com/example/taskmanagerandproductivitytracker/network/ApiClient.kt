package com.example.taskmanagerandproductivitytracker.network

import android.os.Build
import com.example.taskmanagerandproductivitytracker.dashboard.Task

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import okhttp3.ResponseBody


object ApiClient {
    private val client = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val BASE_URL = if (isEmulator()) {
        "http://10.0.2.2:8000/api"
    } else {
        "http://127.0.0.1:8000/api"
    }

    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }

    private suspend fun makeApiCall(request: Request): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string() ?: ""
                    if (response.isSuccessful) {
                        Result.success(responseBody)
                    } else {
                        Result.failure(Exception("Error: ${response.code} ${response.message}\n$responseBody"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun signup(email: String, password: String, confirmPassword: String): Result<String> {
        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
            put("confirmPassword", confirmPassword)
        }
        val body = json.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("$BASE_URL/auth/signup")
            .post(body)
            .build()
        return makeApiCall(request)
    }

    suspend fun login(email: String, password: String): Result<String> {
        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
        }
        val body = json.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("$BASE_URL/auth/login")
            .post(body)
            .build()
        return makeApiCall(request)
    }

    suspend fun getTasks(userId: Int): Result<String> {
        val request = Request.Builder()
            .url("$BASE_URL/tasks/$userId")
            .get()
            .build()
        return makeApiCall(request)
    }

    suspend fun createTask(title: String, description: String, userId: Int, priority: String, deadline: String?): Result<String> {
        val json = JSONObject().apply {
            put("title", title)
            put("description", description)
            put("user_id", userId)
            put("priority", priority)
            deadline?.let { put("deadline", it) }
        }
        val body = json.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("$BASE_URL/tasks/")
            .post(body)
            .build()
        return makeApiCall(request)
    }

    suspend fun updateTask(task: Task, userId: Int): Result<String> {
        val json = JSONObject().apply {
            put("title", task.title)
            put("description", task.description)
            put("is_completed", task.isCompleted)
            put("user_id", userId)
            put("priority", task.priority)
            task.deadline?.let { put("deadline", it) }
        }
        val body = json.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("$BASE_URL/tasks/${task.id}")
            .put(body)
            .build()
        return makeApiCall(request)
    }

    suspend fun deleteTask(taskId: Int, userId: Int): Result<String> {
        val json = JSONObject().apply {
            put("user_id", userId)
        }
        val body = json.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("$BASE_URL/tasks/$taskId")
            .delete(body)
            .build()
        return makeApiCall(request)
    }

    suspend fun updateTimeSpent(taskId: Int, userId: Int, seconds: Int): Result<String> {
        val json = JSONObject().apply {
            put("user_id", userId)
            put("time_spent_seconds", seconds)
        }
        val body = json.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("$BASE_URL/tasks/$taskId/time")
            .patch(body)
            .build()
        return makeApiCall(request)
    }

    suspend fun getProductivityStats(userId: Int): Result<String> {
        val request = Request.Builder()
            .url("$BASE_URL/stats/productivity/$userId")
            .get()
            .build()
        return makeApiCall(request)
    }

    suspend fun getDailySummary(userId: Int): Result<String> {
        val request = Request.Builder()
            .url("$BASE_URL/stats/summary/daily/$userId")
            .get()
            .build()
        return makeApiCall(request)
    }
    suspend fun exportTasks(userId: Int, format: String): Result<ResponseBody> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/export/$userId?format=$format")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful && response.body != null) {
                    Result.success(response.body!!)
                } else {
                    Result.failure(Exception("Error: ${response.code} ${response.message}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}