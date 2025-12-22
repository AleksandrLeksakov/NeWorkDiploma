package ru.netology.nework.auth

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.nework.model.AuthModel
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context,
) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val idKey = "id"
    private val tokenKey = "token"
    private val _authState = MutableStateFlow(
        AuthModel(
            prefs.getLong(idKey, 0L),
            prefs.getString(tokenKey, null)
        )
    )

    val authState: StateFlow<AuthModel> = _authState.asStateFlow()

    @Synchronized
    fun setAuth(id: Long, token: String) {
        _authState.value = AuthModel(id, token)
        with(prefs.edit()) {
            putLong(idKey, id)
            putString(tokenKey, token)
            commit()
        }
    }

    @Synchronized
    fun removeAuth() {
        _authState.value = AuthModel()
        with(prefs.edit()) {
            clear()
            commit()
        }
    }

}