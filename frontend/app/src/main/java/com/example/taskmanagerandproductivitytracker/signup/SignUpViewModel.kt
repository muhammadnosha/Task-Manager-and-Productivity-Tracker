package com.example.taskmanagerandproductivitytracker.signup


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanagerandproductivitytracker.network.ApiClient
import kotlinx.coroutines.launch

data class SignUpUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val signUpSuccess: Boolean = false
)

class SignUpViewModel : ViewModel() {
    var uiState by mutableStateOf(SignUpUiState())
        private set

    fun signupUser(email: String, password: String, confirmPassword: String) {
        uiState = uiState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val result = ApiClient.signup(email, password, confirmPassword)

            result.onSuccess {
                uiState = uiState.copy(isLoading = false, signUpSuccess = true)
            }.onFailure { exception ->
                val errorMessage = exception.message ?: "An unknown error occurred"
                uiState = uiState.copy(isLoading = false, error = errorMessage)
            }
        }
    }
}