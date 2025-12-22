package ru.netology.nework.viewmodel

import android.net.Uri
import android.util.Log
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
import ru.netology.nework.api.ApiService
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.AuthResponse
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
            Log.d("AuthViewModel", "Аутентификация: login=$login, pass=$password")
            val response = apiService.auth(login = login, pass = password)
            Log.d("AuthViewModel", "Ответ аутентификации: ${response.code()}")

            if (response.isSuccessful) {
                response.body()?.let { authResponse ->
                    Log.d("AuthViewModel", "Успешная аутентификация: id=${authResponse.id}, token=${authResponse.token.take(10)}...")

                    // Сервер может не возвращать login/name/avatar, поэтому используем переданные значения
                    // или значения из ответа если они есть
                    val userLogin = authResponse.login ?: login  // Используем из ответа или переданный
                    val userName = authResponse.name ?: login     // Используем из ответа или логин как имя

                    appAuth.setAuth(
                        id = authResponse.id,
                        token = authResponse.token,
                        login = userLogin,
                        name = userName,
                        avatar = authResponse.avatar  // Может быть null
                    )

                    _authSuccess.postValue(Unit)
                } ?: run {
                    Log.e("AuthViewModel", "Пустое тело ответа")
                    _authError.postValue("Неправильный логин или пароль")
                }
            } else {
                Log.e("AuthViewModel", "Ошибка аутентификации: ${response.code()}")
                when (response.code()) {
                    400 -> _authError.postValue("Неправильный пароль")
                    404 -> _authError.postValue("Юзер незарегистрирован")
                    else -> _authError.postValue("Ошибка: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Исключение при аутентификации", e)
            _authError.postValue("Ошибка сети: ${e.message}")
        }
    }

    fun register(login: String, password: String, name: String, avatarUri: Uri?) = viewModelScope.launch {
        try {
            Log.d("AuthViewModel", "Регистрация: login=$login, name=$name, pass=$password")

            // ВАЖНО: всегда создаем multipart часть, даже если аватара нет
            val avatarPart = if (avatarUri != null) {
                // С аватаром
                val file = File(avatarUri.path ?: "")
                if (file.exists()) {
                    Log.d("AuthViewModel", "Загрузка аватара: ${file.path}")
                    val requestFile = file.asRequestBody("image/*".toMediaType())
                    MultipartBody.Part.createFormData("avatar", file.name, requestFile)
                } else {
                    // Файл не существует - создаем пустую часть
                    Log.w("AuthViewModel", "Файл аватара не найден, создаем пустую часть")
                    createEmptyAvatarPart()
                }
            } else {
                // Без аватара - создаем пустую часть
                createEmptyAvatarPart()
            }

            val response = apiService.register(
                login = login,
                pass = password,
                name = name,
                avatar = avatarPart
            )

            handleRegistrationResponse(response, login, name)
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Исключение при регистрации", e)
            _registrationError.postValue("Ошибка сети: ${e.message}")
        }
    }

    private fun createEmptyAvatarPart(): MultipartBody.Part {
        // Создаем пустой файл для multipart
        val emptyBody = "".toRequestBody("application/octet-stream".toMediaType())
        return MultipartBody.Part.createFormData("avatar", "", emptyBody)
    }

    private fun handleRegistrationResponse(
        response: retrofit2.Response<AuthResponse>,
        login: String,
        name: String
    ) {
        Log.d("AuthViewModel", "Ответ регистрации: ${response.code()}")

        if (response.isSuccessful) {
            response.body()?.let { authResponse ->
                Log.d("AuthViewModel", "Успешная регистрация: id=${authResponse.id}")

                // Используем данные из ответа или переданные
                val userLogin = authResponse.login ?: login
                val userName = authResponse.name ?: name

                appAuth.setAuth(
                    id = authResponse.id,
                    token = authResponse.token,
                    login = userLogin,
                    name = userName,
                    avatar = authResponse.avatar
                )

                _registrationSuccess.postValue(Unit)
            } ?: run {
                Log.e("AuthViewModel", "Пустое тело ответа")
                _registrationError.postValue("Ошибка регистрации")
            }
        } else {
            Log.e("AuthViewModel", "Ошибка регистрации: ${response.code()}")
            when (response.code()) {
                403 -> _registrationError.postValue("Юзер уже зарегистрирован")
                415 -> _registrationError.postValue("Неправильный формат фото")
                else -> _registrationError.postValue("Ошибка регистрации: ${response.code()}")
            }
        }
    }

    fun logout() {
        appAuth.removeAuth()
    }

    fun isAuthenticated(): Boolean = appAuth.isAuthenticated()
}