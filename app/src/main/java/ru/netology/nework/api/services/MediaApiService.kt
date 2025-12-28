package ru.netology.nework.api.services

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import ru.netology.nework.dto.Media

interface MediaApiService {

    @Multipart
    @POST("api/media")
    suspend fun save(
        @Part file: MultipartBody.Part
    ): Response<Media>
}