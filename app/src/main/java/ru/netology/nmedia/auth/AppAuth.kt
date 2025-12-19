package ru.netology.nmedia.auth

import android.content.Context
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

        if (token != null && id != 0L) {
            _authState.value = AuthState(id, token)
            tokenHolder.token = token
        }
    }

    @Synchronized
    fun setAuth(id: Long, token: String) {
        _authState.value = AuthState(id, token)
        tokenHolder.token = token

        prefs.edit()
            .putString(TOKEN_KEY, token)
            .putLong(ID_KEY, id)
            .apply()
    }

    @Synchronized
    fun removeAuth() {
        _authState.value = AuthState()
        tokenHolder.token = null

        prefs.edit()
            .remove(TOKEN_KEY)
            .remove(ID_KEY)
            .apply()
    }

    fun isAuthenticated(): Boolean = !_authState.value.token.isNullOrBlank()

    companion object {
        private const val TOKEN_KEY = "token"
        private const val ID_KEY = "id"
    }
}