package ru.netology.nework.api.services

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import ru.netology.nework.dto.UserResponse

interface UserApiService {

    @GET("api/users")
    suspend fun getAllUsers(): Response<List<UserResponse>>

    @GET("api/users/{id}")
    suspend fun getUserById(
        @Path("id") id: Long
    ): Response<UserResponse>
}