package ru.netology.nmedia.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.nmedia.dto.*

interface ApiService {
    // ============ POSTS ============
    @GET("posts")
    suspend fun getAllPosts(): Response<List<Post>>

    @GET("posts/{id}")
    suspend fun getPostById(@Path("id") id: Long): Response<Post>

    @POST("posts")
    suspend fun savePost(@Body post: Post): Response<Post>

    @DELETE("posts/{id}")
    suspend fun removeById(@Path("id") id: Long): Response<Unit>

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun dislikeById(@Path("id") id: Long): Response<Post>

    @GET("posts/{id}/newer")
    suspend fun getNewerPosts(@Path("id") id: Long): Response<List<Post>>

    @GET("posts/{id}/before")
    suspend fun getPostsBefore(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("posts/{id}/after")
    suspend fun getPostsAfter(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("posts/latest")
    suspend fun getLatestPosts(@Query("count") count: Int): Response<List<Post>>

    // ============ EVENTS ============
    @GET("events")
    suspend fun getAllEvents(): Response<List<Event>>

    @GET("events/{id}")
    suspend fun getEventById(@Path("id") id: Long): Response<Event>

    @POST("events")
    suspend fun saveEvent(@Body event: Event): Response<Event>

    @DELETE("events/{id}")
    suspend fun removeEventById(@Path("id") id: Long): Response<Unit>

    @POST("events/{id}/likes")
    suspend fun likeEventById(@Path("id") id: Long): Response<Event>

    @DELETE("events/{id}/likes")
    suspend fun dislikeEventById(@Path("id") id: Long): Response<Event>

    @POST("events/{id}/participants")
    suspend fun participateInEvent(@Path("id") id: Long): Response<Event>

    @DELETE("events/{id}/participants")
    suspend fun cancelParticipation(@Path("id") id: Long): Response<Event>

    @GET("events/{id}/newer")
    suspend fun getNewerEvents(@Path("id") id: Long): Response<List<Event>>

    @GET("events/{id}/before")
    suspend fun getEventsBefore(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Event>>

    @GET("events/{id}/after")
    suspend fun getEventsAfter(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Event>>

    @GET("events/latest")
    suspend fun getLatestEvents(@Query("count") count: Int): Response<List<Event>>

    // ============ USERS ============
    @GET("users")
    suspend fun getAllUsers(): Response<List<UserResponse>>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Long): Response<UserResponse>

    @POST("users/authentication")
    @FormUrlEncoded
    suspend fun authenticate(
        @Field("login") login: String,
        @Field("pass") password: String
    ): Response<Token>

    @Multipart
    @POST("users/registration")
    suspend fun register(
        @Part("login") login: RequestBody,
        @Part("pass") password: RequestBody,
        @Part("name") name: RequestBody,
        @Part file: MultipartBody.Part?
    ): Response<Token>

    // ============ WALL ============
    @GET("users/{authorId}/wall")
    suspend fun getUserWall(@Path("authorId") authorId: Long): Response<List<Post>>

    @GET("my/wall")
    suspend fun getMyWall(): Response<List<Post>>

    // ============ JOBS ============
    @GET("users/{userId}/jobs")
    suspend fun getUserJobs(@Path("userId") userId: Long): Response<List<Job>>

    @GET("my/jobs")
    suspend fun getMyJobs(): Response<List<Job>>

    @POST("my/jobs")
    suspend fun createJob(@Body job: Job): Response<Job>

    @DELETE("my/jobs/{id}")
    suspend fun removeJob(@Path("id") id: Long): Response<Unit>

    // ============ COMMENTS ============
    @GET("posts/{postId}/comments")
    suspend fun getComments(@Path("postId") postId: Long): Response<List<Comment>>

    @POST("posts/{postId}/comments")
    suspend fun saveComment(
        @Path("postId") postId: Long,
        @Body comment: Comment
    ): Response<Comment>

    @POST("posts/{postId}/comments/{id}/likes")
    suspend fun likeComment(
        @Path("postId") postId: Long,
        @Path("id") id: Long
    ): Response<Comment>

    @DELETE("posts/{postId}/comments/{id}/likes")
    suspend fun dislikeComment(
        @Path("postId") postId: Long,
        @Path("id") id: Long
    ): Response<Comment>

    @DELETE("posts/{postId}/comments/{id}")
    suspend fun removeComment(
        @Path("postId") postId: Long,
        @Path("id") id: Long
    ): Response<Unit>

    // ============ MEDIA ============
    @Multipart
    @POST("media")
    suspend fun uploadMedia(@Part file: MultipartBody.Part): Response<Media>

    // ============ PUSH TOKENS ============
    @POST("users/push-tokens")
    suspend fun sendPushToken(@Body pushToken: PushToken): Response<Unit>
}

// Media response from /api/media
data class Media(
    val url: String
)