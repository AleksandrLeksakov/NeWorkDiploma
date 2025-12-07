package ru.netology.nmedia.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nmedia.enumeration.AttachmentType

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromLongList(value: List<Long>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toLongList(value: String): List<Long> {
        return try {
            val listType = object : TypeToken<List<Long>>() {}.type
            gson.fromJson(value, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromAttachmentType(value: AttachmentType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toAttachmentType(value: String?): AttachmentType? {
        return value?.let { AttachmentType.valueOf(it) }
    }
}