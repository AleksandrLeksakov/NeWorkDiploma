package ru.netology.nework.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import ru.netology.nework.auth.AppAuth
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val appAuth: AppAuth
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url.toString()
        val path = original.url.encodedPath

        Log.d("AuthInterceptor", "=== Processing: $path ===")

        // Для аутентификационных запросов не добавляем Authorization
        if (path == "/api/users/authentication" || path == "/api/users/registration") {
            Log.d("AuthInterceptor", "⚠️ Auth endpoint - skipping Authorization")
            return chain.proceed(original)
        }

        val authState = appAuth.authStateFlow.value
        Log.d("AuthInterceptor", "Auth State:")
        Log.d("AuthInterceptor", "  - ID: ${authState.id}")
        Log.d("AuthInterceptor", "  - Token exists: ${authState.token != null}")
        Log.d("AuthInterceptor", "  - Token: ${authState.token?.let { "${it.take(10)}..." } ?: "null"}")

        val token = authState.token

        val newRequest = if (!token.isNullOrBlank()) {
            Log.d("AuthInterceptor", "✅ Adding Authorization header for $path")
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            Log.w("AuthInterceptor", "❌ No token available for $path")
            original
        }

        // Логируем итоговые заголовки
        Log.d("AuthInterceptor", "Final headers for $path:")
        newRequest.headers.forEach { (name, value) ->
            if (name == "Authorization") {
                Log.d("AuthInterceptor", "  $name: Bearer ***")
            } else if (name == "Api-Key") {
                Log.d("AuthInterceptor", "  $name: *** (length: ${value.length})")
            } else {
                Log.d("AuthInterceptor", "  $name: $value")
            }
        }
        Log.d("AuthInterceptor", "==========================")

        return chain.proceed(newRequest)
    }
}