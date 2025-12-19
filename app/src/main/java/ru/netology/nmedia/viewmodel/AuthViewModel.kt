package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Credentials
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val apiService: ApiService,
    private val appAuth: AppAuth
) : ViewModel() {

    private val _authError = MutableLiveData<String>()
    val authError: LiveData<String> = _authError

    private val _authSuccess = MutableLiveData<Unit>()
    val authSuccess: LiveData<Unit> = _authSuccess

    private val _registrationError = MutableLiveData<String>()
    val registrationError: LiveData<String> = _registrationError

    private val _registrationSuccess = MutableLiveData<Unit>()
    val registrationSuccess: LiveData<Unit> = _registrationSuccess

    fun authenticate(login: String, password: String) = viewModelScope.launch {
        try {
            val response = apiService.auth(Credentials(login, password))

            if (response.isSuccessful) {
                response.body()?.let { authResponse ->
                    appAuth.setAuth(authResponse.id, authResponse.token)
                    _authSuccess.postValue(Unit)
                } ?: run {
                    _authError.postValue("Неправильный логин или пароль")
                }
            } else {
                _authError.postValue("Неправильный логин или пароль")
            }
        } catch (e: Exception) {
            _authError.postValue("Ошибка сети: ${e.message}")
        }
    }

    fun register(login: String, password: String, name: String, avatarUri: Uri?) = viewModelScope.launch {
        try {
            val loginBody = login.toRequestBody("text/plain".toMediaType())
            val passwordBody = password.toRequestBody("text/plain".toMediaType())
            val nameBody = name.toRequestBody("text/plain".toMediaType())

            val avatarPart = avatarUri?.let { uri ->
                val file = File(uri.path ?: return@let null)
                val requestFile = file.asRequestBody("image/*".toMediaType())
                MultipartBody.Part.createFormData("avatar", file.name, requestFile)
            }

            val response = apiService.register(loginBody, passwordBody, nameBody, avatarPart)

            if (response.isSuccessful) {
                response.body()?.let { authResponse ->
                    appAuth.setAuth(authResponse.id, authResponse.token)
                    _registrationSuccess.postValue(Unit)
                } ?: run {
                    _registrationError.postValue("Ошибка регистрации")
                }
            } else {
                _registrationError.postValue("Пользователь с таким логином уже зарегистрирован")
            }
        } catch (e: Exception) {
            _registrationError.postValue("Ошибка сети: ${e.message}")
        }
    }

    fun logout() {
        appAuth.removeAuth()
    }

    fun isAuthenticated(): Boolean = appAuth.isAuthenticated()
}