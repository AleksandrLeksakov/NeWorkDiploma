package ru.netology.nework.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.dto.*

interface ApiService {
    // ========== AUTHENTICATION & REGISTRATION ==========
    @POST("api/users/authentication")
    suspend fun auth(
        @Query("login") login: String,
        @Query("pass") pass: String
    ): Response<AuthResponse>


    @Multipart
    @POST("api/users/registration")
    suspend fun register(
        @Query("login") login: String,
        @Query("pass") pass: String,
        @Query("name") name: String,
        @Part avatar: MultipartBody.Part
    ): Response<AuthResponse>



    // ========== POSTS ==========
    @GET("api/posts")
    suspend fun getAllPosts(
        @Query("latest") latest: Long? = null,
        @Query("count") count: Int? = null
    ): Response<List<Post>>

    @GET("api/posts/{id}")
    suspend fun getPostById(@Path("id") id: Long): Response<Post>

    @POST("api/posts")
    suspend fun save(@Body post: Post): Response<Post>

    @POST("api/posts/{id}")
    suspend fun updatePost(@Path("id") id: Long, @Body post: Post): Response<Post>

    @DELETE("api/posts/{id}")
    suspend fun removeById(@Path("id") id: Long): Response<Unit>

    @POST("api/posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Response<Post>

    @DELETE("api/posts/{id}/likes")
    suspend fun unlikeById(@Path("id") id: Long): Response<Post>

    @POST("api/posts/{id}/shares")
    suspend fun shareById(@Path("id") id: Long): Response<Post>

    // Для пагинации (старый вариант - можно оставить для совместимости)
    @GET("api/posts/latest")
    suspend fun getLatest(@Query("count") count: Int): Response<List<Post>>

    @GET("api/posts/{id}/after")
    suspend fun getAfter(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("api/posts/{id}/before")
    suspend fun getBefore(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    // ========== EVENTS ==========
    @GET("api/events")
    suspend fun getAllEvents(
        @Query("latest") latest: Long? = null,
        @Query("count") count: Int? = null
    ): Response<List<Event>>

    @GET("api/events/{id}")
    suspend fun getEventById(@Path("id") id: Long): Response<Event>

    @POST("api/events")
    suspend fun saveEvent(@Body event: Event): Response<Event>

    @POST("api/events/{id}")
    suspend fun updateEvent(@Path("id") id: Long, @Body event: Event): Response<Event>

    @DELETE("api/events/{id}")
    suspend fun removeEvent(@Path("id") id: Long): Response<Unit>

    @POST("api/events/{id}/likes")
    suspend fun likeEvent(@Path("id") id: Long): Response<Event>

    @DELETE("api/events/{id}/likes")
    suspend fun unlikeEvent(@Path("id") id: Long): Response<Event>

    @POST("api/events/{id}/participants")
    suspend fun participateInEvent(@Path("id") id: Long): Response<Event>

    @DELETE("api/events/{id}/participants")
    suspend fun cancelParticipation(@Path("id") id: Long): Response<Event>

    // ========== USERS ==========
    @GET("api/users")
    suspend fun getAllUsers(): Response<List<UserResponse>>

    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") id: Long): Response<UserResponse>

    // Стена пользователя
    @GET("api/users/{id}/wall")
    suspend fun getUserWall(
        @Path("id") id: Long,
        @Query("latest") latest: Long? = null,
        @Query("count") count: Int? = null
    ): Response<List<Post>>

    // Работы пользователя
    @GET("api/users/{id}/jobs")
    suspend fun getUserJobs(@Path("id") id: Long): Response<List<Job>>

    @POST("api/users/{id}/jobs")
    suspend fun saveJob(@Path("id") id: Long, @Body job: Job): Response<Job>

    @DELETE("api/users/{id}/jobs/{jobId}")
    suspend fun removeJob(
        @Path("id") id: Long,
        @Path("jobId") jobId: Long
    ): Response<Unit>

    // ========== COMMENTS ==========
    @GET("api/posts/{id}/comments")
    suspend fun getComments(@Path("id") id: Long): Response<List<Comment>>

    @POST("api/posts/{id}/comments")
    suspend fun saveComment(@Path("id") id: Long, @Body comment: Comment): Response<Comment>

    @DELETE("api/posts/{id}/comments/{commentId}")
    suspend fun removeComment(
        @Path("id") id: Long,
        @Path("commentId") commentId: Long
    ): Response<Unit>

    @POST("api/posts/{id}/comments/{commentId}/likes")
    suspend fun likeComment(
        @Path("id") id: Long,
        @Path("commentId") commentId: Long
    ): Response<Comment>

    @DELETE("api/posts/{id}/comments/{commentId}/likes")
    suspend fun unlikeComment(
        @Path("id") id: Long,
        @Path("commentId") commentId: Long
    ): Response<Comment>

    // ========== MEDIA ==========
    @Multipart
    @POST("api/media")
    suspend fun upload(@Part media: MultipartBody.Part): Response<Media>

    // ========== PUSH TOKENS ==========
    @POST("api/users/push-tokens")
    suspend fun sendPushToken(@Body token: PushToken): Response<Unit>
}