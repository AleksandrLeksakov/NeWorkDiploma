package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.AuthState
import ru.netology.nmedia.dto.Credentials
import ru.netology.nmedia.dto.RegistrationData
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val api: ApiService,
    private val auth: AppAuth
) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        _authState.value = auth.authStateFlow.value
    }

    fun auth(login: String, password: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = api.auth(Credentials(login, password))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        auth.setAuth(body.id, body.token)
                        _authState.value = auth.authStateFlow.value
                    }
                } else {
                    _error.value = "Неправильный логин или пароль"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка сети: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(login: String, name: String, password: String, avatarFile: File? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                var avatarMediaId: String? = null

                // Загружаем аватар если есть
                avatarFile?.let { file ->
                    val requestFile = file.asRequestBody("image/*".toMediaType())
                    val body = MultipartBody.Part.createFormData(
                        "file",
                        file.name,
                        requestFile
                    )
                    val response = api.upload(body)
                    if (response.isSuccessful) {
                        avatarMediaId = response.body()?.id
                    }
                }

                val registrationData = RegistrationData(
                    login = login,
                    name = name,
                    password = password,
                    avatar = avatarMediaId
                )

                val response = api.register(registrationData)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        auth.setAuth(body.id, body.token)
                        _authState.value = auth.authStateFlow.value
                    }
                } else {
                    _error.value = "Пользователь с таким логином уже зарегистрирован"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        auth.removeAuth()
        _authState.value = auth.authStateFlow.value
    }

    fun clearError() {
        _error.value = null
    }
}