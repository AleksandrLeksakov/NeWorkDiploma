package ru.netology.nework.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.model.AttachmentModel
import ru.netology.nework.repository.Repository
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _photoData = MutableLiveData<AttachmentModel?>(null)
    val photoData: LiveData<AttachmentModel?> = _photoData

    private val _registerState = MutableLiveData<RegisterState>(RegisterState.Idle)
    val registerState: LiveData<RegisterState> = _registerState

    fun register(login: String, name: String, pass: String) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            try {
                val photo = _photoData.value
                repository.register(login, name, pass, photo)
                clearPhoto()
                _registerState.value = RegisterState.Success
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error(e.message ?: "Ошибка регистрации")
            }
        }
    }

    fun setPhoto(uri: Uri, file: File) {
        _photoData.value = AttachmentModel(AttachmentType.IMAGE, uri, file)
    }

    fun clearPhoto() {
        _photoData.value = null
    }

    fun resetState() {
        _registerState.value = RegisterState.Idle
    }

    sealed class RegisterState {
        object Idle : RegisterState()
        object Loading : RegisterState()
        object Success : RegisterState()
        data class Error(val message: String) : RegisterState()
    }
}