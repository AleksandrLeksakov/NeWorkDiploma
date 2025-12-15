package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject

private val empty = Post(
    id = 0,
    authorId = 0,
    author = "",
    authorJob = null,
    authorAvatar = null,
    content = "",
    published = "",
    coordinates = null,
    link = null,
    mentionIds = emptyList(),
    mentionedMe = false,
    likeOwnerIds = emptyList(),
    likedByMe = false,
    attachment = null
)

private val noPhoto = PhotoModel()

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    private val auth: AppAuth,
) : ViewModel() {

    // Используем StateFlow для данных
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val data: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _dataState = MutableStateFlow<FeedModelState>(FeedModelState())
    val dataState: StateFlow<FeedModelState> = _dataState.asStateFlow()

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

            // Получаем посты из репозитория (временно эмулируем)
            val posts = listOf<Post>() // TODO: Заменить на реальные данные из репозитория
            _posts.value = posts

            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun refreshPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            repository.getAll()
            // TODO: Обновить _posts.value с реальными данными
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun save() {
        edited.value?.let { post ->
            viewModelScope.launch {
                try {
                    val mediaUpload = _photo.value?.uri?.let { uri ->
                        val file = File(uri.path ?: return@let null)
                        MediaUpload(file)
                    }

                    repository.save(post)

                    edited.value = empty
                    _photo.value = noPhoto
                    _postCreated.value = Unit

                    // Перезагружаем посты после сохранения
                    loadPosts()
                } catch (e: Exception) {
                    e.printStackTrace()
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
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

    fun likeById(post: Post) {
        viewModelScope.launch {
            try {
                if (post.likedByMe) {
                    repository.unlikeById(post.id)
                } else {
                    repository.likeById(post.id)
                }
                loadPosts()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeById(id)
                loadPosts()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun createNewPost(content: String) {
        val currentTime = java.time.Instant.now().toString()
        val newPost = empty.copy(
            content = content,
            published = currentTime
        )
        edited.value = newPost
    }
}