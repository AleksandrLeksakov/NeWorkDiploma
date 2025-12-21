package ru.netology.nmedia.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dto.Media
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState

    fun uploadImage(uri: Uri) = viewModelScope.launch {
        try {
            _uploadState.value = UploadState.Loading

            Log.d("MediaViewModel", "🔄 Начало загрузки изображения")
            Log.d("MediaViewModel", "URI: $uri")

            // Копируем файл во временное хранилище
            val tempFile = copyUriToTempFile(uri)
            if (tempFile == null) {
                Log.e("MediaViewModel", "❌ Не удалось создать временный файл")
                _uploadState.value = UploadState.Error("Не удалось создать файл")
                return@launch
            }

            Log.d("MediaViewModel", "📁 Временный файл: ${tempFile.path}")
            Log.d("MediaViewModel", "📏 Размер файла: ${tempFile.length()} байт")

            // ВАЖНО: Внимание на имя поля "media" - должно совпадать с ожиданиями сервера
            val requestFile = tempFile.asRequestBody("image/*".toMediaType())
            val mediaPart = MultipartBody.Part.createFormData("media", "photo.jpg", requestFile)

            Log.d("MediaViewModel", "📤 Отправка запроса на сервер...")
            val response = apiService.upload(mediaPart)

            Log.d("MediaViewModel", "📥 Ответ сервера: ${response.code()}")
            Log.d("MediaViewModel", "Тело ответа: ${response.body()}")

            if (response.isSuccessful) {
                val media = response.body()
                if (media != null) {
                    Log.d("MediaViewModel", "✅ Успех! ID: ${media.id}, URL: ${media.url}")
                    _uploadState.value = UploadState.Success(media)
                } else {
                    Log.e("MediaViewModel", "❌ Пустое тело ответа")
                    _uploadState.value = UploadState.Error("Пустой ответ от сервера")
                }
            } else {
                Log.e("MediaViewModel", "❌ Ошибка сервера: ${response.code()} ${response.message()}")
                _uploadState.value = UploadState.Error("Ошибка сервера: ${response.code()}")
            }

        } catch (e: Exception) {
            Log.e("MediaViewModel", "💥 Исключение при загрузке", e)
            _uploadState.value = UploadState.Error("Ошибка: ${e.message}")
        }
    }

    private suspend fun copyUriToTempFile(uri: Uri): File? = withContext(Dispatchers.IO) {
        return@withContext try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.e("MediaViewModel", "Не удалось открыть InputStream")
                null
            } else {
                // Создаем временный файл с уникальным именем
                val timestamp = System.currentTimeMillis()
                val tempFile = File.createTempFile("upload_${timestamp}_", ".jpg", context.cacheDir)

                inputStream.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }

                Log.d("MediaViewModel", "Создан временный файл: ${tempFile.absolutePath}")
                tempFile
            }
        } catch (e: Exception) {
            Log.e("MediaViewModel", "Ошибка копирования файла", e)
            null
        }
    }

    sealed class UploadState {
        object Idle : UploadState()
        object Loading : UploadState()
        data class Success(val media: Media) : UploadState()
        data class Error(val message: String) : UploadState()
    }
}