package ru.netology.nework.entity.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Coordinates
import ru.netology.nework.dto.UserPreview

class CoordsConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromCoords(coords: Coordinates?): String? {
        return coords?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toCoords(coords: String?): Coordinates? {
        return coords?.let { gson.fromJson(it, Coordinates::class.java) }
    }
}

class MentionIdsConverter {
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Long>>() {}.type

    @TypeConverter
    fun fromMentionIds(mentionIds: List<Long>?): String? {
        return mentionIds?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toMentionIds(mentionIds: String?): List<Long>? {
        return mentionIds?.let { gson.fromJson(it, typeToken) }
    }
}

class AttachmentConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromAttachment(attachment: Attachment?): String? {
        return attachment?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toAttachment(attachment: String?): Attachment? {
        return attachment?.let { gson.fromJson(it, Attachment::class.java) }
    }
}

class UsersConverter {
    private val gson = Gson()

    // TypeToken для Map<Long, UserPreview>
    private val typeToken = object : TypeToken<Map<Long, UserPreview>>() {}.type

    @TypeConverter
    fun fromUsers(users: Map<Long, UserPreview>?): String? {
        return users?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toUsers(users: String?): Map<Long, UserPreview>? {
        if (users == null) return null

        // Gson парсит JSON-объект с числовыми ключами как Map<String, ...>
        // Поэтому сначала парсим как Map<String, UserPreview>, затем конвертируем ключи
        val stringKeyType = object : TypeToken<Map<String, UserPreview>>() {}.type
        val stringKeyMap: Map<String, UserPreview> = gson.fromJson(users, stringKeyType)

        // Конвертируем String ключи в Long
        return stringKeyMap.mapKeys { (key, _) -> key.toLong() }
    }
}