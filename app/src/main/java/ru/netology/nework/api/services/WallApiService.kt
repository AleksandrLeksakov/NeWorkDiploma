package ru.netology.nework.api.services

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import ru.netology.nework.dto.Post

interface WallApiService {

    @GET("api/{authorId}/wall")
    suspend fun getByAuthorId(
        @Path("authorId") authorId: Long
    ): Response<List<Post>>
}