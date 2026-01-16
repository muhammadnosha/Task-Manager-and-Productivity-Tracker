package com.example.taskmanagerandproductivitytracker.dashboard

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taskmanagerandproductivitytracker.network.ApiClient
import com.example.taskmanagerandproductivitytracker.stats.StatsActivity
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.io.IOException

@Composable
fun DashboardScreen(
    viewModel: TaskViewModel,
    userId: Int,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit
) {
    val uiState = viewModel.uiState
    val activeTimerTaskId by viewModel.activeTimerTaskId.collectAsState()
    val elapsedTime by viewModel.elapsedTime.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    val context = LocalContext.current
    val activity = context as ComponentActivity
    val coroutineScope = rememberCoroutineScope()

    var showExportMenu by remember { mutableStateOf(false) }

    val fileSaverLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                val format = if (uri.toString().endsWith("pdf")) "pdf" else "csv"
                coroutineScope.launch {
                    Toast.makeText(context, "Exporting...", Toast.LENGTH_SHORT).show()
                    val result = ApiClient.exportTasks(userId, format)
                    result.onSuccess { responseBody ->
                        saveFile(context, it, responseBody)
                        Toast.makeText(context, "Export successful!", Toast.LENGTH_SHORT).show()
                    }.onFailure { e ->
                        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    )

    var showPermissionRationale by remember { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) showPermissionRationale = true
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) {
            val permission = "android.permission.POST_NOTIFICATIONS"
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    showPermissionRationale = true
                } else {
                    notificationPermissionLauncher.launch(permission)
                }
            }
        }
    }

    if (showPermissionRationale) {
        PermissionRationaleDialog(
            onDismiss = { showPermissionRationale = false },
            onConfirm = {
                showPermissionRationale = false
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary,
                actions = {
                    IconButton(onClick = onThemeToggle) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }

                    IconButton(onClick = {
                        val intent = Intent(context, StatsActivity::class.java).apply {
                            putExtra("USER_ID", userId)
                        }
                        context.startActivity(intent)
                    }) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "View Stats"
                        )
                    }

                    Box {
                        IconButton(onClick = { showExportMenu = true }) {
                            Icon(Icons.Default.Download, contentDescription = "Export Tasks")
                        }
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            DropdownMenuItem(onClick = {
                                fileSaverLauncher.launch("tasks.csv")
                                showExportMenu = false
                            }) {
                                Text("Export as CSV")
                            }
                            DropdownMenuItem(onClick = {
                                fileSaverLauncher.launch("tasks.pdf")
                                showExportMenu = false
                            }) {
                                Text("Export as PDF")
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                taskToEdit = null
                showDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.tasks.isEmpty() -> {
                    Text(
                        "No tasks yet. Tap the '+' button!",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.tasks, key = { it.id }) { task ->
                            TaskItem(
                                task = task,
                                isTimerActive = task.id == activeTimerTaskId,
                                elapsedTime = if (task.id == activeTimerTaskId) elapsedTime else 0L,
                                onTimerStart = { viewModel.startTimer(task) },
                                onTimerStop = { viewModel.stopTimer(task) },
                                onEdit = { taskToEdit = task; showDialog = true },
                                onDelete = { viewModel.deleteTask(context, task) },
                                onStatusChange = { updatedTask ->
                                    viewModel.updateTask(context, updatedTask)
                                }
                            )
                        }
                    }
                }
            }

            if (showDialog) {
                TaskDialog(
                    task = taskToEdit,
                    onDismiss = { showDialog = false },
                    onSave = { title, description, priority, deadline ->
                        if (taskToEdit == null) {
                            viewModel.createTask(context, title, description, priority, deadline)
                        } else {
                            taskToEdit?.let {
                                viewModel.updateTask(
                                    context,
                                    it.copy(
                                        title = title,
                                        description = description,
                                        priority = priority,
                                        deadline = deadline
                                    )
                                )
                            }
                        }
                        showDialog = false
                    }
                )
            }
        }
    }
}

private fun saveFile(context: Context, uri: Uri, body: ResponseBody) {
    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            body.byteStream().use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    } catch (e: IOException) {
        Toast.makeText(context, "Failed to save file.", Toast.LENGTH_LONG).show()
        e.printStackTrace()
    }
}
