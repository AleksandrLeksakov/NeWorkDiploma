package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.dto.FeedItem
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.repository.Repository
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    val dataUsers: Flow<PagingData<FeedItem>> =
        repository.dataUsers.map {
            it.map { feedItem ->
                feedItem
            }
        }.flowOn(Dispatchers.Default)

    private val _dataUser = MutableLiveData<UserResponse>(null)
    val dataUser: LiveData<UserResponse> = _dataUser

    fun getUser(userId: Long) = viewModelScope.launch {
        _dataUser.value = repository.getUser(userId)
    }

}