package ru.netology.nework.api.services

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import ru.netology.nework.dto.Post

interface PostApiService {

    @POST("api/posts")
    suspend fun save(
        @Body post: Post
    ): Response<Post>

    @POST("api/posts/{id}/likes")
    suspend fun like(
        @Path("id") id: Long
    ): Response<Post>

    @DELETE("api/posts/{id}/likes")
    suspend fun unlike(
        @Path("id") id: Long
    ): Response<Post>

    @GET("api/posts/{id}/before")
    suspend fun getBefore(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("api/posts/{id}/after")
    suspend fun getAfter(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @DELETE("api/posts/{id}")
    suspend fun delete(
        @Path("id") id: Long
    ): Response<Unit>

    @GET("api/posts/latest")
    suspend fun getLatest(
        @Query("count") count: Int
    ): Response<List<Post>>
}