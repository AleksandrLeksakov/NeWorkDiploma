package ru.netology.nmedia.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.nmedia.dto.*

interface ApiService {


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

    @POST("api/posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Response<Post>

    @DELETE("api/posts/{id}/likes")
    suspend fun unlikeById(@Path("id") id: Long): Response<Post>

    @POST("api/posts")
    suspend fun save(@Body post: Post): Response<Post>

    @DELETE("api/posts/{id}")
    suspend fun removeById(@Path("id") id: Long): Response<Unit>

    @POST("api/users/authentication")
    suspend fun auth(@Body credentials: Credentials): Response<AuthResponse>

    @POST("api/users/registration")
    suspend fun register(@Body registrationData: RegistrationData): Response<AuthResponse>

    @Multipart
    @POST("api/media")
    suspend fun upload(@Part media: MultipartBody.Part): Response<Media>

    @POST("api/users/push-tokens")
    suspend fun sendPushToken(@Body token: PushToken): Response<Unit>
}