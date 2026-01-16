
package com.example.taskmanagerandproductivitytracker.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanagerandproductivitytracker.network.ApiClient
import kotlinx.coroutines.launch
import org.json.JSONObject

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false,
    val userId: Int? = null
)

class LoginViewModel : ViewModel() {
    var uiState by mutableStateOf(LoginUiState())
        private set

    fun loginUser(email: String, password: String) {
        uiState = uiState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val result = ApiClient.login(email, password)

            result.onSuccess { responseBody ->
                val json = JSONObject(responseBody)
                val userId = json.getInt("user_id")
                uiState = uiState.copy(isLoading = false, loginSuccess = true, userId = userId)
            }.onFailure { exception ->
                val errorMessage = try {
                    JSONObject(exception.message ?: "{}").getString("detail")
                } catch (e: Exception) {
                    exception.message ?: "An unknown error occurred"
                }
                uiState = uiState.copy(isLoading = false, error = errorMessage)
            }
        }
    }
}