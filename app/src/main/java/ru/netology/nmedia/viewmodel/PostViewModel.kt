package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.DateUtils
import ru.netology.nmedia.util.SingleLiveEvent
import java.time.Instant
import javax.inject.Inject

// Исправьте empty Post - published должно быть String
private val empty = Post(
    id = 0,
    authorId = 0,
    author = "",
    authorJob = null,
    authorAvatar = null,
    content = "",
    published = DateUtils.currentTimeIso(),  // ISO строка
    coords = null,
    link = null,
    mentionIds = emptyList(),
    mentionedMe = false,
    likeOwnerIds = emptyList(),
    likedByMe = false,
    attachment = null,
    ownedByMe = false
)

private val noPhoto = PhotoModel()

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    auth: AppAuth,
) : ViewModel() {
    private val cached = repository
        .data
        .cachedIn(viewModelScope)

    val data: Flow<PagingData<Post>> = auth.authStateFlow
        .flatMapLatest { (myId, _) ->
            cached.map { pagingData ->
                pagingData.map { post ->
                    post.copy(ownedByMe = post.authorId == myId)
                }
            }
        }

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _prependState = MutableLiveData<FeedModelState>(FeedModelState())
    val prependState: LiveData<FeedModelState>
        get() = _prependState

    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    init {
        loadPosts()
    }

    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun refreshPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun refreshPrepend() = viewModelScope.launch {
        try {
            _prependState.value = FeedModelState(loading = true, refreshing = true)
            val newPosts = repository.refreshPrepend()
            _prependState.value = FeedModelState(
                refreshPrependCount = newPosts.size
            )
        } catch (e: Exception) {
            _prependState.value = FeedModelState(error = true)
        }
    }

    fun save() {
        edited.value?.let { post ->
            viewModelScope.launch {
                try {
                    // Создаем новый пост с текущим временем
                    val currentTime = Instant.now().toString()
                    val postToSave = post.copy(
                        published = currentTime,
                        // Добавляем недостающие поля если нужно
                        authorJob = post.authorJob ?: null,
                        likeOwnerIds = post.likeOwnerIds ?: emptyList(),
                        mentionIds = post.mentionIds ?: emptyList()
                    )

                    repository.save(
                        postToSave,
                        _photo.value?.uri?.let { MediaUpload(it.toFile()) }
                    )
                    _postCreated.value = Unit
                } catch (e: Exception) {
                    e.printStackTrace()
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        edited.value = empty
        _photo.value = noPhoto
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun changePhoto(uri: Uri?) {
        _photo.value = PhotoModel(uri)
    }

    fun likeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.likeById(id)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeById(id)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    // Дополнительный метод для создания нового поста
    fun createNewPost(content: String) {
        val currentTime = Instant.now().toString()
        val newPost = empty.copy(
            content = content,
            published = currentTime
        )
        edited.value = newPost
    }
}