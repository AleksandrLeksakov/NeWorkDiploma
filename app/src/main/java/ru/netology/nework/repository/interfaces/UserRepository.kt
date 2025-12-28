package ru.netology.nework.repository.interfaces

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.FeedItem
import ru.netology.nework.dto.UserResponse

interface UserRepository {

    val dataUsers: Flow<PagingData<FeedItem>>

    suspend fun getUser(id: Long): UserResponse
}