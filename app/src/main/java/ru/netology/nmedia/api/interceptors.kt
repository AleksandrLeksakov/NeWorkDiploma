package ru.netology.nmedia.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import ru.netology.nmedia.BuildConfig
import javax.inject.Inject

class ApiKeyInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var apiKey = BuildConfig.API_KEY.trim()

        // Убираем кавычки если они есть
        apiKey = apiKey.removeSurrounding("\"").removeSurrounding("'")

        if (apiKey.isBlank()) {
            Log.e("ApiKeyInterceptor", "❌ API_KEY is empty or invalid!")
            Log.e("ApiKeyInterceptor", "Raw value: '${BuildConfig.API_KEY}'")
        } else {
            Log.d("ApiKeyInterceptor", "✅ API_KEY loaded, length: ${apiKey.length}")
        }

        val request = chain.request()
        val newRequest = request.newBuilder()
            .addHeader("Api-Key", apiKey)
            .build()

        return chain.proceed(newRequest)
    }
}