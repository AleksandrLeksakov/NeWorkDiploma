package ru.netology.nmedia.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Response
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.auth.TokenHolder

fun loggingInterceptor() = HttpLoggingInterceptor()
    .apply {
        if (BuildConfig.DEBUG) {
            level = HttpLoggingInterceptor.Level.HEADERS
        }
    }

fun authInterceptor(tokenHolder: TokenHolder) = fun(chain: Interceptor.Chain): Response {

    tokenHolder.token?.let { token ->
        val newRequest = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        return chain.proceed(newRequest)
    }

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