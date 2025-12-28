package ru.netology.nework.api.services

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.netology.nework.dto.Job

interface JobApiService {

    @GET("api/my/jobs")
    suspend fun getMyJobs(): Response<List<Job>>

    @POST("api/my/jobs")
    suspend fun save(
        @Body job: Job
    ): Response<Job>

    @DELETE("api/my/jobs/{id}")
    suspend fun delete(
        @Path("id") id: Long
    ): Response<Unit>

    @GET("api/{userId}/jobs")
    suspend fun getByUserId(
        @Path("userId") userId: Long
    ): Response<List<Job>>
}