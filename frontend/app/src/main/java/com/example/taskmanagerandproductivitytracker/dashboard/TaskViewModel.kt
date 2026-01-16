package com.example.taskmanagerandproductivitytracker.dashboard

import android.content.Context
import com.example.taskmanagerandproductivitytracker.reminders.NotificationScheduler

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanagerandproductivitytracker.network.ApiClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import android.util.Log


data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class TaskViewModel : ViewModel() {
    // THIS 'by' KEYWORD IS THE MOST IMPORTANT FIX
    var uiState by mutableStateOf(TaskUiState())
        private set

    private val _activeTimerTaskId = MutableStateFlow<Int?>(null)
    val activeTimerTaskId: StateFlow<Int?> = _activeTimerTaskId
    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime
    private var timerJob: Job? = null
    private var userId: Int = -1

    fun initialize(userId: Int) {
        if (this.userId == -1) { this.userId = userId; loadTasks() }
    }

    fun loadTasks() {
        uiState = uiState.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = ApiClient.getTasks(userId)
            result.onSuccess { responseBody ->
                Log.d("TaskViewModel", "✅ Success responseBody = $responseBody")

                try {
                    val tasksJson = JSONArray(responseBody)
                    val taskList = mutableListOf<Task>()
                    for (i in 0 until tasksJson.length()) {
                        val taskJson = tasksJson.getJSONObject(i)
                        taskList.add(
                            Task(
                                id = taskJson.getInt("id"),
                                title = taskJson.getString("title"),
                                description = taskJson.optString("description", null),
                                isCompleted = taskJson.getInt("is_completed") == 1,
                                priority = taskJson.optString("priority", "Medium"),
                                deadline = taskJson.optString("deadline", null)?.ifEmpty { null },
                                timeSpentSeconds = taskJson.optInt("time_spent_seconds", 0)
                            )
                        )
                    }
                    uiState = uiState.copy(isLoading = false, tasks = taskList.sortedBy { it.isCompleted })
                } catch (e: JSONException) {
                    Log.e("TaskViewModel", "❌ JSON parsing failed: ${e.message}")
                    uiState = uiState.copy(isLoading = false, error = "Error parsing server data.")
                }
            }.onFailure { throwable ->
                Log.e("TaskViewModel", "❌ Network/API error: ${throwable.message}")
                uiState = uiState.copy(isLoading = false, error = throwable.message ?: "Failed to load tasks")
            }
        }
    }
    fun startTimer(task: Task) {
        if (timerJob?.isActive == true) return
        _activeTimerTaskId.value = task.id
        _elapsedTime.value = 0L
        timerJob = viewModelScope.launch {
            while (isActive) { delay(1000); _elapsedTime.value++ }
        }
    }

    fun stopTimer(task: Task) {
        timerJob?.cancel()
        timerJob = null
        val totalSeconds = task.timeSpentSeconds + _elapsedTime.value.toInt()
        _activeTimerTaskId.value = null
        _elapsedTime.value = 0L
        viewModelScope.launch {
            ApiClient.updateTimeSpent(task.id, userId, totalSeconds).onSuccess {
                loadTasks()
            }.onFailure {
                uiState = uiState.copy(error = "Failed to save time.")
            }
        }
    }

    fun createTask(context: Context, title: String, description: String, priority: String, deadline: String?) {
        viewModelScope.launch {
            ApiClient.createTask(title, description, userId, priority, deadline).onSuccess {
                loadTasks()
            }.onFailure {
                uiState = uiState.copy(error = it.message ?: "Failed to create task")
            }
        }
    }

    fun updateTask(context: Context, task: Task) {
        viewModelScope.launch {
            ApiClient.updateTask(task, userId).onSuccess {
                NotificationScheduler.scheduleReminder(context, task)
                loadTasks()
            }.onFailure {
                uiState = uiState.copy(error = it.message ?: "Failed to update task")
            }
        }
    }

    fun deleteTask(context: Context, task: Task) {
        viewModelScope.launch {
            ApiClient.deleteTask(task.id, userId).onSuccess {
                NotificationScheduler.cancelReminder(context, task)
                loadTasks()
            }.onFailure {
                uiState = uiState.copy(error = it.message ?: "Failed to delete task")
            }
        }
    }
}