package ru.netology.nework.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.dto.*

interface ApiService {
    // ========== AUTHENTICATION & REGISTRATION ==========
    @FormUrlEncoded
    @POST("api/users/registration")
    suspend fun usersRegistration(
        @Field("login") login: String,
        @Field("pass") pass: String,
        @Field("name") name: String
    ): Response<Token>

    @Multipart
    @POST("api/users/registration")
    suspend fun usersRegistrationWithPhoto(
        @Query("login") login: String,
        @Query("pass") pass: String,
        @Query("name") name: String,
        @Part file: MultipartBody.Part
    ): Response<Token>

    @FormUrlEncoded
    @POST("api/users/authentication")
    suspend fun usersAuthentication(
        @Field("login") login: String,
        @Field("pass") pass: String
    ): Response<Token>

    @GET("api/users")
    suspend fun usersGetAllUser(): Response<List<UserResponse>>

    @GET("api/users/{id}")
    suspend fun usersGetUser(
        @Path("id") id: Long,
    ): Response<UserResponse>



    // ========== POSTS ==========
    @GET("api/posts")
    suspend fun postsGetAllPost(): Response<List<Post>>

    @POST("api/posts")
    suspend fun postsSavePost(
        @Body post: Post,
    ): Response<Post>

    @POST("api/posts/{id}/likes")
    suspend fun postsLikePost(
        @Path("id") id: Long,
    ): Response<Post>

    @DELETE("api/posts/{id}/likes")
    suspend fun postsUnLikePost(
        @Path("id") id: Long,
    ): Response<Post>

    @GET("api/posts/{id}/newer")
    suspend fun postsGetNewerPost(@Path("id") id: Long): Response<List<Post>>

    @GET("api/posts/{id}/before")
    suspend fun postsGetBeforePost(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("api/posts/{id}/after")
    suspend fun postsGetAfterPost(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("api/post/{id}")
    suspend fun postsGetPost(
        @Path("id") id: Long,
    ): Response<Post>

    @DELETE("api/posts/{id}")
    suspend fun postsDeletePost(
        @Path("id") id: Long,
    ): Response<Unit>

    @GET("api/posts/latest")
    suspend fun postsGetLatestPage(@Query("count") count: Int): Response<List<Post>>

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
    suspend fun sendPushToken(@Body token: Token): Response<Unit>
}