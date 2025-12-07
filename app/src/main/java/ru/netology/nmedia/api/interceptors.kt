package ru.netology.nmedia.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Response
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.auth.AppAuth

fun loggingInterceptor() = HttpLoggingInterceptor()
    .apply {
        if (BuildConfig.DEBUG) {
            level = HttpLoggingInterceptor.Level.HEADERS  // Показываем заголовки
        }
    }

fun authInterceptor(auth: AppAuth) = fun(chain: Interceptor.Chain): Response {
    val token = auth.authStateFlow.value.token
    Log.d("INTERCEPTOR", "Auth token: ${token ?: "null"}")

    token?.let {
        val newRequest = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $it")
            .build()
        return chain.proceed(newRequest)
    }

    return chain.proceed(chain.request())
}

fun apiKeyInterceptor() = fun(chain: Interceptor.Chain): Response {
    // Проверяем API_KEY
    val apiKey = BuildConfig.API_KEY.trim()
    Log.d("INTERCEPTOR", "🔑 API_KEY raw: '$apiKey'")
    Log.d("INTERCEPTOR", "🔑 API_KEY length: ${apiKey.length}")

    // Проверяем что ключ не пустой
    if (apiKey.isBlank() || apiKey == "\"\"" || apiKey == "''") {
        Log.e("INTERCEPTOR", "❌ ERROR: API_KEY is empty!")
        Log.e("INTERCEPTOR", "Check secret.properties file in project root")
        Log.e("INTERCEPTOR", "It should contain: API_KEY=your_key_here")
    } else if (apiKey.length < 10) {
        Log.w("INTERCEPTOR", "⚠️ WARNING: API_KEY seems too short: $apiKey")
    }

    val request = chain.request()
    Log.d("INTERCEPTOR", "🌐 Request URL: ${request.url}")

    val newRequest = request.newBuilder()
        .addHeader("Api-Key", apiKey)
        .build()

    // Логируем все заголовки - ИСПРАВЛЕННЫЙ СИНТАКСИС
    Log.d("INTERCEPTOR", "📋 Request headers:")
    newRequest.headers.forEach { (name, value) ->  // Используем деструктуризацию
        if (name.equals("Api-Key", ignoreCase = true)) {
            Log.d("INTERCEPTOR", "  $name: ****${value.takeLast(4)}") // Маскируем ключ
        } else {
            Log.d("INTERCEPTOR", "  $name: $value")
        }
    }

    return chain.proceed(newRequest)
}