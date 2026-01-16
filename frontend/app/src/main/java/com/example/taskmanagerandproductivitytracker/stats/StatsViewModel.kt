package com.example.taskmanagerandproductivitytracker.stats

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanagerandproductivitytracker.network.ApiClient
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException

data class StatsUiState(
    val data: List<ProductivityData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class StatsViewModel : ViewModel() {
    var uiState by mutableStateOf(StatsUiState())
        private set

    fun loadStats(userId: Int) {
        uiState = uiState.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = ApiClient.getProductivityStats(userId)
            result.onSuccess { responseBody ->
                try {
                    val statsJson = JSONArray(responseBody)
                    val dataList = mutableListOf<ProductivityData>()
                    for (i in 0 until statsJson.length()) {
                        val statJson = statsJson.getJSONObject(i)
                        dataList.add(
                            ProductivityData(
                                date = statJson.getString("date"),
                                totalSeconds = statJson.getInt("total_seconds")
                            )
                        )
                    }
                    uiState = uiState.copy(isLoading = false, data = dataList)
                } catch (e: JSONException) {
                    Log.e("StatsViewModel", "JSON parsing failed: ${e.message}")
                    uiState = uiState.copy(isLoading = false, error = "Error parsing chart data.")
                }
            }.onFailure { throwable ->
                Log.e("StatsViewModel", "API error: ${throwable.message}")
                uiState = uiState.copy(isLoading = false, error = throwable.message ?: "Failed to load stats")
            }
        }
    }
}
