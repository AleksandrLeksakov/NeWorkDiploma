package ru.netology.nmedia.dto

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Job(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("position")
    val position: String,

    @SerializedName("start")
    val start: String,

    @SerializedName("finish")
    val finish: String? = null,

    @SerializedName("link")
    val link: String? = null
) {
    fun formatPeriod(): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

            val startDate = inputFormat.parse(start) ?: return start

            val startFormatted = outputFormat.format(startDate)

            if (finish != null) {
                val finishDate = inputFormat.parse(finish)
                if (finishDate != null) {
                    val finishFormatted = outputFormat.format(finishDate)
                    "$startFormatted - $finishFormatted"
                } else {
                    "$startFormatted - $finish"
                }
            } else {
                "$startFormatted - настоящее время"
            }
        } catch (e: Exception) {
            start
        }
    }
}