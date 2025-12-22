package ru.netology.nework.api

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.netology.nework.BuildConfig
import ru.netology.nework.auth.AppAuth
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiServiceModule {

    @Singleton
    @Provides
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        apiKeyInterceptor: ApiKeyInterceptor,
        authInterceptor: AuthInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(apiKeyInterceptor)
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()


    @Singleton
    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                // Фильтруем и форматируем логи
                when {
                    message.startsWith("-->") -> Log.d("OkHttp", "🚀 REQUEST: $message")
                    message.startsWith("<--") -> Log.d("OkHttp", "📥 RESPONSE: $message")
                    message.startsWith("{") || message.startsWith("[") -> {
                        // Красиво форматируем JSON
                        Log.d("OkHttp", "📄 BODY: $message")
                    }
                    else -> Log.d("OkHttp", message)
                }
            }
        }).apply {
            if (BuildConfig.DEBUG) {
                level = HttpLoggingInterceptor.Level.BODY
            } else {
                level = HttpLoggingInterceptor.Level.NONE
            }
        }
    @Singleton
    @Provides
    fun provideApiKeyInterceptor(): ApiKeyInterceptor = ApiKeyInterceptor()

    @Singleton
    @Provides
    fun provideAuthInterceptor(appAuth: AppAuth): AuthInterceptor =
        AuthInterceptor(appAuth)

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .build()

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}