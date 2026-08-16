package com.example.medplus.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.medplus.model.MedicineReminder
import com.example.medplus.receiver.MedicineReminderReceiver
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

class MedicineReminderRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("medicine_reminders_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getReminders(): List<MedicineReminder> {
        val json = prefs.getString("reminders_key", null) ?: return emptyList()
        val type = object : TypeToken<List<MedicineReminder>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveReminders(reminders: List<MedicineReminder>) {
        val json = gson.toJson(reminders)
        prefs.edit().putString("reminders_key", json).apply()
    }

    fun addReminder(reminder: MedicineReminder) {
        val current = getReminders().toMutableList()
        current.add(reminder)
        saveReminders(current)
        if (reminder.isActive) {
            scheduleAlarm(reminder)
        }
    }

    fun deleteReminder(id: String) {
        val reminder = getReminders().find { it.id == id }
        if (reminder != null) {
            cancelAlarm(reminder)
        }
        val current = getReminders().filter { it.id != id }
        saveReminders(current)
    }

    fun toggleReminder(id: String) {
        val current = getReminders().map {
            if (it.id == id) {
                val updated = it.copy(isActive = !it.isActive)
                if (updated.isActive) {
                    scheduleAlarm(updated)
                } else {
                    cancelAlarm(updated)
                }
                updated
            } else it
        }
        saveReminders(current)
    }

    private fun scheduleAlarm(reminder: MedicineReminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MedicineReminderReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
            putExtra("medicine_name", reminder.medicineName)
            putExtra("dosage", reminder.dosage)
            putExtra("time_str", reminder.time)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance()
        try {
            val cleanTime = reminder.time.trim()
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

            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

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

            val sdf = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
            val formattedDate = sdf.format(calendar.time)
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                android.widget.Toast.makeText(
                    context,
                    "Alert scheduled for: $formattedDate",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cancelAlarm(reminder: MedicineReminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MedicineReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
