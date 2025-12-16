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
            level = HttpLoggingInterceptor.Level.HEADERS
        }
    }

// ВРЕМЕННО УПРОЩАЕМ - без зависимости от AppAuth
fun authInterceptor() = fun(chain: Interceptor.Chain): Response {
    // Временно пустой - добавим токен позже
    return chain.proceed(chain.request())
}

fun apiKeyInterceptor() = fun(chain: Interceptor.Chain): Response {
    val apiKey = BuildConfig.API_KEY.trim()
    Log.d("INTERCEPTOR", "🔑 API_KEY raw: '$apiKey'")
    Log.d("INTERCEPTOR", "🔑 API_KEY length: ${apiKey.length}")

    if (apiKey.isBlank() || apiKey == "\"\"" || apiKey == "''") {
        Log.e("INTERCEPTOR", "❌ ERROR: API_KEY is empty!")
    }

    val request = chain.request()
    Log.d("INTERCEPTOR", "🌐 Request URL: ${request.url}")

    val newRequest = request.newBuilder()
        .addHeader("Api-Key", apiKey)
        .build()

    Log.d("INTERCEPTOR", "📋 Request headers:")
    newRequest.headers.forEach { (name, value) ->
        if (name.equals("Api-Key", ignoreCase = true)) {
            Log.d("INTERCEPTOR", "  $name: ****${value.takeLast(4)}")
        } else {
            Log.d("INTERCEPTOR", "  $name: $value")
        }
    }

    return chain.proceed(newRequest)
}