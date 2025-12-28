@file:Suppress("unused")

package ru.netology.nework.api

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.netology.nework.BuildConfig
import ru.netology.nework.api.services.AuthApiService
import ru.netology.nework.api.services.EventApiService
import ru.netology.nework.api.services.JobApiService
import ru.netology.nework.api.services.MediaApiService
import ru.netology.nework.api.services.PostApiService
import ru.netology.nework.api.services.UserApiService
import ru.netology.nework.api.services.WallApiService
import ru.netology.nework.auth.AppAuth
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiServiceModule {

    companion object {
        private const val BASE_URL = "http://94.228.125.136:8080/"
    }

    @Singleton
    @Provides
    fun provideOkHttp(appAuth: AppAuth): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Api-Key", BuildConfig.API_KEY)
                .apply {
                    appAuth.authState.value.token?.let { token ->
                        addHeader("Authorization", token)
                    }
                }
                .build()
            chain.proceed(request)
        }
        .build()

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder().registerTypeAdapter(
                    OffsetDateTime::class.java,
                    object : TypeAdapter<OffsetDateTime>() {
                        override fun write(out: JsonWriter?, value: OffsetDateTime?) {
                            out?.value(value?.toEpochSecond())
                        }

                        override fun read(jsonReader: JsonReader): OffsetDateTime {
                            return OffsetDateTime.ofInstant(
                                Instant.parse(jsonReader.nextString()),
                                ZoneId.systemDefault()
                            )
                        }
                    }
                ).create()
            )
        )
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .build()

    // Отдельные API сервисы

    @Singleton
    @Provides
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService = retrofit.create()

    @Singleton
    @Provides
    fun provideUserApiService(retrofit: Retrofit): UserApiService = retrofit.create()

    @Singleton
    @Provides
    fun providePostApiService(retrofit: Retrofit): PostApiService = retrofit.create()

    @Singleton
    @Provides
    fun provideEventApiService(retrofit: Retrofit): EventApiService = retrofit.create()

    @Singleton
    @Provides
    fun provideJobApiService(retrofit: Retrofit): JobApiService = retrofit.create()

    @Singleton
    @Provides
    fun provideMediaApiService(retrofit: Retrofit): MediaApiService = retrofit.create()

    @Singleton
    @Provides
    fun provideWallApiService(retrofit: Retrofit): WallApiService = retrofit.create()
}