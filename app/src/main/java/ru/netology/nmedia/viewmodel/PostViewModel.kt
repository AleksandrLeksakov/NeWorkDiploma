package ru.netology.nmedia.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
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

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    private val appAuth: AppAuth
) : ViewModel() {

    // Получаем данные текущего пользователя
    private val currentUserId: Long get() = appAuth.currentUserId
    private val currentUserLogin: String get() = appAuth.currentUserLogin ?: "Unknown"
    private val currentUserAvatar: String? get() = appAuth.currentUserAvatar

    // Empty пост должен использовать данные текущего пользователя
    private val empty: Post
        get() = Post(
            id = 0,
            authorId = currentUserId,
            author = currentUserLogin,
            authorJob = null,
            authorAvatar = currentUserAvatar,
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

    val data: Flow<PagingData<Post>> = repository.data
        .cachedIn(viewModelScope)

    private val _dataState = MutableLiveData<FeedModelState>(FeedModelState())
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    init {
        Log.d("PostViewModel", "=== ViewModel created ===")
        Log.d("PostViewModel", "Initial currentUserId: $currentUserId")
        Log.d("PostViewModel", "Initial currentUserLogin: $currentUserLogin")
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

    fun save() {
        Log.d("PostViewModel", "=== save() called ===")
        Log.d("PostViewModel", "Current user ID: $currentUserId")
        Log.d("PostViewModel", "Current user login: $currentUserLogin")

        // Проверка авторизации
        if (currentUserId == 0L) {
            Log.e("PostViewModel", "❌ ERROR: currentUserId is 0!")
            _dataState.value = FeedModelState(error = true)
            return
        }

        edited.value?.let { post ->
            viewModelScope.launch {
                try {
                    Log.d("PostViewModel", "Creating post with:")
                    Log.d("PostViewModel", "  - authorId: $currentUserId")
                    Log.d("PostViewModel", "  - author: $currentUserLogin")
                    Log.d("PostViewModel", "  - content: ${post.content}")
                    Log.d("PostViewModel", "  - edited value authorId: ${edited.value?.authorId}")

                    // РАСКОММЕНТИРОВАТЬ: Обработка медиа
                    val mediaUpload = _photo.value?.uri?.let { uri ->
                        Log.d("PostViewModel", "Processing media from URI: $uri")
                        val file = File(uri.path ?: return@let null)
                        if (file.exists()) {
                            Log.d("PostViewModel", "File exists, size: ${file.length()} bytes")
                            MediaUpload(file)
                        } else {
                            Log.w("PostViewModel", "File does not exist: ${file.absolutePath}")
                            null
                        }
                    }

                    // Если есть медиа, сначала загружаем его
                    var attachmentUrl: String? = null
                    mediaUpload?.let {
                        try {
                            Log.d("PostViewModel", "Uploading media...")
                            val media = repository.upload(it)
                            attachmentUrl = media.url
                            Log.d("PostViewModel", "Media uploaded successfully: $attachmentUrl")
                        } catch (e: Exception) {
                            Log.e("PostViewModel", "❌ Media upload failed: ${e.message}", e)
                            // Можно продолжить без медиа или показать ошибку
                            // Пока просто логируем и продолжаем без медиа
                        }
                    }

                    // Создаем пост с вложением (если есть)
                    val postToSave = if (attachmentUrl != null) {
                        post.copy(
                            authorId = currentUserId,
                            author = currentUserLogin,
                            authorAvatar = currentUserAvatar,
                            attachment = ru.netology.nmedia.dto.Attachment(
                                url = attachmentUrl,
                                type = ru.netology.nmedia.enumeration.AttachmentType.IMAGE
                            )
                        )
                    } else {
                        post.copy(
                            authorId = currentUserId,
                            author = currentUserLogin,
                            authorAvatar = currentUserAvatar,
                            attachment = null
                        )
                    }

                    Log.d("PostViewModel", "Final post to save:")
                    Log.d("PostViewModel", "  - authorId: ${postToSave.authorId}")
                    Log.d("PostViewModel", "  - author: ${postToSave.author}")
                    Log.d("PostViewModel", "  - content: ${postToSave.content}")
                    Log.d("PostViewModel", "  - published: ${postToSave.published}")
                    Log.d("PostViewModel", "  - has attachment: ${postToSave.attachment != null}")

                    repository.save(postToSave)

                    // Сброс с актуальными данными пользователя
                    edited.value = empty
                    _photo.value = noPhoto
                    _postCreated.value = Unit

                    Log.d("PostViewModel", "✅ Post saved successfully!")

                } catch (e: Exception) {
                    Log.e("PostViewModel", "❌ Error saving post: ${e.message}", e)
                    e.printStackTrace()
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
    }

    fun edit(post: Post) {
        edited.value = post
        Log.d("PostViewModel", "edit() called, authorId in edited: ${post.authorId}")
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }

        // Обновляем с правильными данными пользователя
        edited.value = edited.value?.copy(
            content = text,
            authorId = currentUserId,
            author = currentUserLogin,
            authorAvatar = currentUserAvatar
        )

        Log.d("PostViewModel", "Content changed")
        Log.d("PostViewModel", "  - new authorId: ${edited.value?.authorId}")
        Log.d("PostViewModel", "  - new author: ${edited.value?.author}")
        Log.d("PostViewModel", "  - new content: ${edited.value?.content}")
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

    fun createNewPost(content: String) {
        val currentTime = java.time.Instant.now().toString()
        val newPost = empty.copy(
            content = content,
            published = currentTime
        )
        edited.value = newPost
        Log.d("PostViewModel", "createNewPost called")
        Log.d("PostViewModel", "  - authorId in newPost: ${newPost.authorId}")
        Log.d("PostViewModel", "  - author in newPost: ${newPost.author}")
        Log.d("PostViewModel", "  - content: ${newPost.content}")
    }

    // Метод для отладки
    fun debugAuthInfo() {
        val authState = appAuth.authStateFlow.value
        Log.d("PostViewModel", "=== DEBUG Auth Info ===")
        Log.d("PostViewModel", "User ID: ${authState.id}")
        Log.d("PostViewModel", "User Login: ${authState.login}")
        Log.d("PostViewModel", "Token exists: ${authState.token != null}")
        Log.d("PostViewModel", "Token: ${authState.token?.take(10)}...")
        Log.d("PostViewModel", "CurrentUserId property: $currentUserId")
        Log.d("PostViewModel", "CurrentUserLogin property: $currentUserLogin")
        Log.d("PostViewModel", "=========================")
    }

    // Проверка состояния edited
    fun debugEditedState() {
        Log.d("PostViewModel", "=== DEBUG Edited State ===")
        Log.d("PostViewModel", "edited.value: ${edited.value}")
        Log.d("PostViewModel", "  - authorId: ${edited.value?.authorId}")
        Log.d("PostViewModel", "  - author: ${edited.value?.author}")
        Log.d("PostViewModel", "  - content: ${edited.value?.content}")
        Log.d("PostViewModel", "==========================")
    }
}