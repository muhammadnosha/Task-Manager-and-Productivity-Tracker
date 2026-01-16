package com.example.taskmanagerandproductivitytracker.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.*
import com.example.taskmanagerandproductivitytracker.MainActivity
import com.example.taskmanagerandproductivitytracker.R
import com.example.taskmanagerandproductivitytracker.reminders.DailySummaryWorker
import com.example.taskmanagerandproductivitytracker.signup.SignUpActivity
import com.example.taskmanagerandproductivitytracker.ui.theme.GradientEnd
import com.example.taskmanagerandproductivitytracker.ui.theme.GradientStart
import com.example.taskmanagerandproductivitytracker.ui.theme.TaskManagerAndProductivityTrackerTheme
import java.util.*
import java.util.concurrent.TimeUnit

class LoginActivity : ComponentActivity() {
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskManagerAndProductivityTrackerTheme {
                LoginScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    val gradient = Brush.verticalGradient(
        colors = listOf(GradientStart, GradientEnd)
    )

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val uiState = viewModel.uiState

    uiState.error?.let {
        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
    }

    if (uiState.loginSuccess && uiState.userId != null) {
        val userId = uiState.userId
        LaunchedEffect(key1 = userId) {
            scheduleDailySummaryWorker(context, userId)

            Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra("USER_ID", userId)
            }
            context.startActivity(intent)
            (context as? Activity)?.finish()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            elevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Productivity Pro",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.primary
                    )
                }

                Text(
                    text = "Welcome Back",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Sign in to continue your productivity journey",
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.loginUser(email, password) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary,
                        contentColor = Color.White
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Login", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { /* Forgot password */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Forgot Password?",
                        color = MaterialTheme.colors.primary.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                TextButton(
                    onClick = {
                        context.startActivity(Intent(context, SignUpActivity::class.java))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Don't have an account? Sign Up",
                        color = MaterialTheme.colors.primary
                    )
                }
            }
        }
    }
}

private fun scheduleDailySummaryWorker(context: android.content.Context, userId: Int) {
    val inputData = workDataOf(DailySummaryWorker.USER_ID_KEY to userId)

    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val calendar = Calendar.getInstance()
    val nowMillis = calendar.timeInMillis
    calendar.set(Calendar.HOUR_OF_DAY, 21)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)

    if (calendar.timeInMillis <= nowMillis) {
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }

    val initialDelay = calendar.timeInMillis - nowMillis

    val dailyWorkRequest = PeriodicWorkRequestBuilder<DailySummaryWorker>(1, TimeUnit.DAYS)
        .setInputData(inputData)
        .setConstraints(constraints)
        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        DailySummaryWorker.WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        dailyWorkRequest
    )
}