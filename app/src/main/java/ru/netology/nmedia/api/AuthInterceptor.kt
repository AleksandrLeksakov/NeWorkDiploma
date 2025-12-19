package ru.netology.nmedia.api

import okhttp3.Interceptor
import okhttp3.Response
import ru.netology.nmedia.auth.AppAuth
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val appAuth: AppAuth
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Получаем токен из AppAuth
        val token = appAuth.authStateFlow.value.token

        val newRequest = if (!token.isNullOrBlank()) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }

        return chain.proceed(newRequest)
    }
}