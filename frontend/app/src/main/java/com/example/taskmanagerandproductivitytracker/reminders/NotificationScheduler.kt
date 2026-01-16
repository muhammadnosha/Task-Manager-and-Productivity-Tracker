package com.example.taskmanagerandproductivitytracker.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.taskmanagerandproductivitytracker.dashboard.Task
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private val REMINDER_OFFSET_MINUTES = TimeUnit.HOURS.toMinutes(1)

    fun scheduleReminder(context: Context, task: Task) {
        if (task.isCompleted || task.deadline.isNullOrBlank()) {
            cancelReminder(context, task)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val reminderTime = getReminderTimeInMillis(task.deadline)
        if (reminderTime <= System.currentTimeMillis()) {
            Log.w("NotificationScheduler", "Cannot schedule reminder for a past deadline for task: ${task.id}")
            return
        }

        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra(TaskReminderReceiver.EXTRA_TASK_ID, task.id)
            putExtra(TaskReminderReceiver.EXTRA_TASK_TITLE, task.title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.e("NotificationScheduler", "Cannot schedule exact alarms. App needs SCHEDULE_EXACT_ALARM permission.")
            return
        }

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
            Log.d("NotificationScheduler", "✅ Reminder scheduled for task ${task.id} at ${Date(reminderTime)}")
        } catch (e: SecurityException) {
            Log.e("NotificationScheduler", "SecurityException: Missing USE_EXACT_ALARM or SCHEDULE_EXACT_ALARM permission.", e)
        }
    }

    fun cancelReminder(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskReminderReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("NotificationScheduler", "✅ Reminder cancelled for task ${task.id}")
        }
    }

    private fun getReminderTimeInMillis(deadline: String?): Long {
        if (deadline == null) return 0L
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = dateFormat.parse(deadline) ?: return 0L

            val calendar = Calendar.getInstance().apply {
                time = date
                add(Calendar.MINUTE, -REMINDER_OFFSET_MINUTES.toInt())
            }
            calendar.timeInMillis
        } catch (e: Exception) {
            Log.e("NotificationScheduler", "Error parsing deadline date-time string", e)
            0L
        }
    }
}

