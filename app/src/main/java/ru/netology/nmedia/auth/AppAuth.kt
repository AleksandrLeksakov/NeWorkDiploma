package ru.netology.nmedia.auth

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.dto.AuthState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext private val context: Context
    // УБИРАЕМ ВСЕ сетевые зависимости!
) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _authState = MutableStateFlow<AuthState>(AuthState())
    val authStateFlow: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        val token = prefs.getString(TOKEN_KEY, null)
        val id = prefs.getLong(ID_KEY, 0L)

        if (token != null) {
            _authState.value = AuthState(id, token)
        }
    }

    @Synchronized
    fun setAuth(id: Long, token: String) {
        _authState.value = AuthState(id, token)
        with(prefs.edit()) {
            putString(TOKEN_KEY, token)
            putLong(ID_KEY, id)
            apply()
        }
        // Временно отключаем отправку push токена
        // sendPushToken()
    }

    @Synchronized
    fun removeAuth() {
        _authState.value = AuthState()
        with(prefs.edit()) {
            remove(TOKEN_KEY)
            remove(ID_KEY)
            apply()
        }
    }

    // Временно отключаем - добавим позже
    // private fun sendPushToken() {
    //     scope.launch {
    //         try {
    //             val pushToken = FirebaseMessaging.getInstance().token.await()
    //             // Будем отправлять позже
    //         } catch (e: Exception) {
    //             e.printStackTrace()
    //         }
    //     }
    // }

    companion object {
        private const val TOKEN_KEY = "token"
        private const val ID_KEY = "id"
    }
}