package ru.netology.nework.api.services

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import ru.netology.nework.dto.Event

interface EventApiService {

    @POST("api/events")
    suspend fun save(
        @Body event: Event
    ): Response<Event>

    @POST("api/events/{id}/likes")
    suspend fun like(
        @Path("id") id: Long
    ): Response<Event>

    @DELETE("api/events/{id}/likes")
    suspend fun unlike(
        @Path("id") id: Long
    ): Response<Event>

    @GET("api/events/{id}/before")
    suspend fun getBefore(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Event>>

    @GET("api/events/{id}/after")
    suspend fun getAfter(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Event>>

    @DELETE("api/events/{id}")
    suspend fun delete(
        @Path("id") id: Long
    ): Response<Unit>

    @GET("api/events/latest")
    suspend fun getLatest(
        @Query("count") count: Int
    ): Response<List<Event>>
}