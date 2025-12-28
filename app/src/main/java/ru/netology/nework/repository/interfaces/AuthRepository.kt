package ru.netology.nework.repository.interfaces

import kotlinx.coroutines.flow.StateFlow
import ru.netology.nework.model.AttachmentModel
import ru.netology.nework.model.AuthModel

interface AuthRepository {

    val dataAuth: StateFlow<AuthModel>

    suspend fun register(
        login: String,
        name: String,
        pass: String,
        attachmentModel: AttachmentModel?
    )

    suspend fun login(login: String, pass: String)

    fun logout()
}