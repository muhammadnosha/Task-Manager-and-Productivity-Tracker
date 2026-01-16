package com.example.taskmanagerandproductivitytracker.dashboard

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TaskDialog(
    task: Task?,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, priority: String, deadline: String?) -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var priority by remember { mutableStateOf(task?.priority ?: "Medium") }

    var deadlineDate by remember { mutableStateOf<String?>(null) }
    var deadlineTime by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(task) {
        task?.deadline?.let { deadlineString ->
            val parts = deadlineString.split(" ")
            deadlineDate = parts.getOrNull(0)
            deadlineTime = parts.getOrNull(1)
        }
    }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val selectedCalendar = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            deadlineDate = sdf.format(selectedCalendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour: Int, minute: Int ->
            deadlineTime = String.format(Locale.US, "%02d:%02d", hour, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (task == null) "Add New Task" else "Edit Task",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description (Optional)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(24.dp))
                Text("Priority", fontWeight = FontWeight.Medium)
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceAround) {
                    listOf("Low", "Medium", "High").forEach { p ->
                        PriorityChip(text = p, isSelected = priority == p, onClick = { priority = p })
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Deadline", fontWeight = FontWeight.Medium)

                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val deadlineText = when {
                        deadlineDate != null && deadlineTime != null -> "$deadlineDate $deadlineTime"
                        deadlineDate != null -> "$deadlineDate (time not set)"
                        else -> "Not Set"
                    }
                    Text(
                        text = deadlineText,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.body1
                    )
                    if (deadlineDate != null || deadlineTime != null) {
                        IconButton(onClick = {
                            deadlineDate = null
                            deadlineTime = null
                        }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear Deadline")
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { datePickerDialog.show() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Set Date")
                    }
                    Button(
                        onClick = { timePickerDialog.show() },
                        enabled = deadlineDate != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Set Time")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val finalDeadline = if (deadlineDate != null && deadlineTime != null) {
                            "$deadlineDate $deadlineTime"
                        } else {
                            null
                        }
                        if (title.isNotBlank()) onSave(title, description, priority, finalDeadline)
                    }) { Text("Save") }
                }
            }
        }
    }
}

@Composable
fun PriorityChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) MaterialTheme.colors.primary else Color.LightGray.copy(alpha = 0.5f)
    val contentColor = if (isSelected) Color.White else MaterialTheme.colors.onSurface
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(text = text, color = contentColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}