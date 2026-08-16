package com.example.medplus.data.network

import com.google.firebase.Timestamp
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.JsonDeserializationContext
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Custom Gson adapter for Firebase Timestamp to handle conversion between ISO Date strings
 * (used in Mongoose backend) and Timestamp objects (used in client).
 */
class TimestampTypeAdapter : JsonDeserializer<Timestamp>, JsonSerializer<Timestamp> {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val backupFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Timestamp {
        if (json.isJsonPrimitive && json.asJsonPrimitive.isString) {
            val dateStr = json.asString
            try {
                val date = dateFormat.parse(dateStr)
                if (date != null) return Timestamp(date)
            } catch (e: Exception) {
                try {
                    val date = backupFormat.parse(dateStr)
                    if (date != null) return Timestamp(date)
                } catch (e2: Exception) {
                    // Fallback to current time
                }
            }
            return Timestamp.now()
        } else if (json.isJsonObject) {
            val obj = json.asJsonObject
            val seconds = obj.get("seconds")?.asLong ?: (obj.get("_seconds")?.asLong ?: 0L)
            val nanoseconds = obj.get("nanoseconds")?.asInt ?: (obj.get("_nanoseconds")?.asInt ?: 0)
            return Timestamp(seconds, nanoseconds)
        }
        return Timestamp.now()
    }

    override fun serialize(
        src: Timestamp,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return context.serialize(dateFormat.format(src.toDate()))
    }
}
