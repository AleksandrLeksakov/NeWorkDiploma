package ru.netology.nmedia.auth

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.nmedia.dto.AuthState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenHolder: TokenHolder
) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow<AuthState>(AuthState())
    val authStateFlow: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        val token = prefs.getString(TOKEN_KEY, null)
        val id = prefs.getLong(ID_KEY, 0L)
        val login = prefs.getString(LOGIN_KEY, null)
        val name = prefs.getString(NAME_KEY, null)
        val avatar = prefs.getString(AVATAR_KEY, null)

        if (token != null && id != 0L) {
            _authState.value = AuthState(id, token, login, name, avatar)
            tokenHolder.token = token
            Log.d("AppAuth", "Восстановлена авторизация: id=$id, login=$login")
        }
    }

    @Synchronized
    fun setAuth(id: Long, token: String, login: String? = null, name: String? = null, avatar: String? = null) {
        Log.d("AppAuth", "=== setAuth called ===")
        Log.d("AppAuth", "ID: $id")
        Log.d("AppAuth", "Token: ${token.take(10)}...")
        Log.d("AppAuth", "Login: $login")
        Log.d("AppAuth", "Name: $name")

        _authState.value = AuthState(id, token, login, name, avatar)

        prefs.edit()
            .putString(TOKEN_KEY, token)
            .putLong(ID_KEY, id)
            .putString(LOGIN_KEY, login)
            .putString(NAME_KEY, name)
            .putString(AVATAR_KEY, avatar)
            .apply()

        Log.d("AppAuth", "Auth saved to SharedPreferences")

        // Проверка что сохранилось
        val savedToken = prefs.getString(TOKEN_KEY, null)
        val savedId = prefs.getLong(ID_KEY, 0L)
        Log.d("AppAuth", "Verification - Saved ID: $savedId, Token: ${savedToken?.take(10)}...")
    }

    @Synchronized
    fun removeAuth() {
        _authState.value = AuthState()
        tokenHolder.token = null

        prefs.edit()
            .remove(TOKEN_KEY)
            .remove(ID_KEY)
            .remove(LOGIN_KEY)
            .remove(NAME_KEY)
            .remove(AVATAR_KEY)
            .apply()
    }

    fun isAuthenticated(): Boolean = !_authState.value.token.isNullOrBlank()

    // Геттеры для удобства
    val currentUserId: Long get() = _authState.value.id
    val currentUserLogin: String? get() = _authState.value.login
    val currentUserName: String? get() = _authState.value.name
    val currentUserAvatar: String? get() = _authState.value.avatar

    companion object {
        private const val TOKEN_KEY = "token"
        private const val ID_KEY = "id"
        private const val LOGIN_KEY = "login"
        private const val NAME_KEY = "name"
        private const val AVATAR_KEY = "avatar"
    }
}