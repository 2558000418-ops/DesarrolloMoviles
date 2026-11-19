package com.example.todolist.util

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.todolist.MainActivity
import com.example.todolist.model.TaskPriority
import com.example.todolist.model.TodoTask
import java.time.LocalDate
import java.time.ZoneId

/**
 * BroadcastReceiver que dispara notificaciones nativas en segundo plano cuando se cumple
 * la fecha y hora programada en AlarmManager, incluso si la aplicación está cerrada.
 */
class TaskAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra("task_id", 1001)
        val title = intent.getStringExtra("task_title") ?: "Recordatorio de Tarea"
        val category = intent.getStringExtra("task_category") ?: "General"
        val time = intent.getStringExtra("task_time") ?: "Ahora"
        val priority = intent.getIntExtra("task_priority", 1)

        NotificationHelper.sendTaskNotification(
            context = context,
            notificationId = taskId,
            title = title,
            category = category,
            time = time,
            priorityLevel = priority
        )
    }
}

object NotificationHelper {
    private const val CHANNEL_ID = "todolist_reminders_channel"
    private const val CHANNEL_NAME = "Recordatorios de Tareas"
    private const val CHANNEL_DESC = "Alertas y recordatorios locales de actividades pendientes"

    fun ensureChannelCreated(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    fun sendTaskNotification(
        context: Context,
        notificationId: Int,
        title: String,
        category: String,
        time: String,
        priorityLevel: Int
    ): Boolean {
        return try {
            ensureChannelCreated(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }

            val priorityEnum = TaskPriority.fromLevel(priorityLevel)
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getActivity(context, notificationId, intent, pendingIntentFlags)

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle("${priorityEnum.badgeSymbol} [$category] $title")
                .setContentText("Programada: $time • Prioridad ${priorityEnum.labelEs}")
                .setPriority(
                    if (priorityLevel >= 2) NotificationCompat.PRIORITY_HIGH
                    else NotificationCompat.PRIORITY_DEFAULT
                )
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(notificationId, notification)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Programa una alarma en segundo plano con AlarmManager (`setAndAllowWhileIdle` compatible con Android 11 API 30+).
     */
    fun scheduleTaskReminder(context: Context, task: TodoTask) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val date = runCatching { LocalDate.parse(task.dueDate) }.getOrDefault(LocalDate.now())
            val time = parseTaskTime(task.time)
            val triggerMillis = date.atTime(time)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            val alarmIntent = Intent(context, TaskAlarmReceiver::class.java).apply {
                putExtra("task_id", task.id)
                putExtra("task_title", task.title)
                putExtra("task_category", task.category)
                putExtra("task_time", task.time)
                putExtra("task_priority", task.priority)
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, task.id, alarmIntent, flags)

            val effectiveTrigger = if (triggerMillis > System.currentTimeMillis()) {
                triggerMillis
            } else {
                System.currentTimeMillis() + 5_000L
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    effectiveTrigger,
                    pendingIntent
                )
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, effectiveTrigger, pendingIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Cancela una alarma programada cuando la tarea se completa o elimina.
     */
    fun cancelTaskReminder(context: Context, taskId: Int) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val alarmIntent = Intent(context, TaskAlarmReceiver::class.java)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, taskId, alarmIntent, flags)
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendTestNotification(context: Context, userName: String): Boolean {
        return sendTaskNotification(
            context = context,
            notificationId = 99901,
            title = "¡Hola $userName! Notificaciones y Alarmas activas en DataStore",
            category = "Sistema",
            time = "Ahora mismo",
            priorityLevel = TaskPriority.HIGH.level
        )
    }
}
