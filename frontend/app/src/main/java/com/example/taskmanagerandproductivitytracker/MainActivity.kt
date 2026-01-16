package com.example.taskmanagerandproductivitytracker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.taskmanagerandproductivitytracker.dashboard.DashboardScreen
import com.example.taskmanagerandproductivitytracker.dashboard.TaskViewModel
import com.example.taskmanagerandproductivitytracker.ui.theme.TaskManagerAndProductivityTrackerTheme

class MainActivity : ComponentActivity() {
    private val viewModel: TaskViewModel by viewModels()
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "Error: User not found.", Toast.LENGTH_LONG).show()
            finish(); return
        }
        viewModel.initialize(userId)
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }

            TaskManagerAndProductivityTrackerTheme(darkTheme = isDarkMode) {
                DashboardScreen(
                    viewModel = viewModel,
                    userId = userId,
                    // --- NEW: Pass state and event handler ---
                    isDarkMode = isDarkMode,
                    onThemeToggle = { isDarkMode = !isDarkMode }
                )
            }
        }
    }
}