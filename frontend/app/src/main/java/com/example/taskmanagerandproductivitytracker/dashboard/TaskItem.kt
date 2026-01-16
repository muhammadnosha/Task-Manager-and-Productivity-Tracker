
package com.example.taskmanagerandproductivitytracker.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TaskItem(
    task: Task,
    isTimerActive: Boolean,
    elapsedTime: Long,
    onTimerStart: () -> Unit,
    onTimerStop: () -> Unit,
    onEdit: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onStatusChange: (Task) -> Unit
) {
    val priorityColor = when (task.priority) {
        "High" -> Color(0xFFE57373)
        "Medium" -> Color(0xFFFFB74D)
        else -> Color(0xFF64B5F6)
    }
    Card(elevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onStatusChange(task.copy(isCompleted = it)) }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Canvas(
                            modifier = Modifier.size(10.dp),
                            onDraw = { drawCircle(color = priorityColor) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = task.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                            color = if (task.isCompleted) Color.Gray else MaterialTheme.colors.onSurface
                        )
                    }
                    task.description?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            fontSize = 14.sp,
                            color = if (task.isCompleted) Color.Gray else MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    task.deadline?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.DateRange,
                                contentDescription = "Deadline",
                                modifier = Modifier.size(16.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Due: $it", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
                IconButton(onClick = { onEdit(task) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit Task")
                }
                IconButton(onClick = { onDelete(task) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete Task", tint = MaterialTheme.colors.error)
                }
            }
            Divider(modifier = Modifier.padding(top = 12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Time Spent", style = MaterialTheme.typography.caption, color = Color.Gray)
                    Text(
                        text = formatTime(task.timeSpentSeconds.toLong() + elapsedTime),
                        fontWeight = FontWeight.SemiBold,
                        color = if (isTimerActive) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
                    )
                }
                if (isTimerActive) {
                    Button(
                        onClick = onTimerStop,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE57373))
                    ) {
                        Icon(Icons.Filled.Stop, contentDescription = "Stop Timer")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Stop")
                    }
                } else {
                    OutlinedButton(onClick = onTimerStart, enabled = !task.isCompleted) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Start Timer")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start")
                    }
                }
            }
        }
    }
}

fun formatTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}