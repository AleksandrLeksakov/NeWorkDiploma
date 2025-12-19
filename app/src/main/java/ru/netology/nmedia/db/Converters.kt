package ru.netology.nmedia.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nmedia.enumeration.AttachmentType
import ru.netology.nmedia.enumeration.EventType
import java.util.Date

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromLongList(value: List<Long>): String = gson.toJson(value)

    @TypeConverter
    fun toLongList(value: String): List<Long> =
        gson.fromJson(value, object : TypeToken<List<Long>>() {}.type)

    @TypeConverter
    fun fromAttachmentType(value: AttachmentType): String = value.name

    @TypeConverter
    fun toAttachmentType(value: String): AttachmentType = AttachmentType.valueOf(value)

    @TypeConverter
    fun fromEventType(value: EventType): String = value.name

    @TypeConverter
    fun toEventType(value: String): EventType = EventType.valueOf(value)

    @TypeConverter
    fun fromDate(date: Date): Long = date.time

    @TypeConverter
    fun toDate(timestamp: Long): Date = Date(timestamp)
}