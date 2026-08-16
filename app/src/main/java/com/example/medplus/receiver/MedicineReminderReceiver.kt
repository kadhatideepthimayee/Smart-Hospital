package com.example.medplus.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.medplus.MainActivity
import com.example.medplus.repository.MedicineReminderRepository
import java.util.Calendar

class MedicineReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra("reminder_id") ?: return
        val medicineName = intent.getStringExtra("medicine_name") ?: "Medicine"
        val dosage = intent.getStringExtra("dosage") ?: ""
        val timeStr = intent.getStringExtra("time_str") ?: ""

        val repository = MedicineReminderRepository(context)
        val activeReminders = repository.getReminders()
        val reminder = activeReminders.find { it.id == reminderId }

        // Only alert and reschedule if reminder is still registered and active
        if (reminder == null || !reminder.isActive) {
            return
        }

        // 1. Post notification alert
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "medicine_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Medicine Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channels for medicine intake notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val mainIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode(),
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.example.medplus.R.mipmap.ic_launcher)
            .setContentTitle("Medicine Reminder")
            .setContentText("Time to take: $medicineName ($dosage)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(reminderId.hashCode(), notification)

        // 2. Reschedule for tomorrow at the same exact time
        rescheduleForTomorrow(context, reminderId, medicineName, dosage, timeStr)
    }

    private fun rescheduleForTomorrow(
        context: Context,
        reminderId: String,
        medicineName: String,
        dosage: String,
        timeStr: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MedicineReminderReceiver::class.java).apply {
            putExtra("reminder_id", reminderId)
            putExtra("medicine_name", medicineName)
            putExtra("dosage", dosage)
            putExtra("time_str", timeStr)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance()
        try {
            val cleanTime = timeStr.trim()
            val timeParts = cleanTime.split(" ")
            val hm = timeParts[0].split(":")
            var hour = hm[0].toInt()
            val minute = hm[1].toInt()

            if (timeParts.size > 1) {
                val ampm = timeParts[1].uppercase()
                if (ampm == "PM" && hour < 12) hour += 12
                if (ampm == "AM" && hour == 12) hour = 0
            }

            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            // Reschedule for tomorrow
            calendar.add(Calendar.DAY_OF_YEAR, 1)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
