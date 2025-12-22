package ru.netology.nework.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.model.AttachmentModel
import ru.netology.nework.model.AuthModel
import ru.netology.nework.repository.Repository
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    val dataAuth: LiveData<AuthModel> = repository.dataAuth.asLiveData(Dispatchers.Default)

    private val _photoData: MutableLiveData<AttachmentModel?> = MutableLiveData(null)
    val photoData: LiveData<AttachmentModel?>
        get() = _photoData

    fun register(login: String, name: String, pass: String) {
        viewModelScope.launch {
            val photo = _photoData.value
            repository.register(login, name, pass, photo)
        }
    }

    fun login(login: String, pass: String) {
        viewModelScope.launch {
            repository.login(login, pass)
        }
    }

    fun setPhoto(uri: Uri, file: File) {
        _photoData.value = AttachmentModel(AttachmentType.IMAGE, uri, file)
    }

    fun logout() {
        repository.logout()
    }
}