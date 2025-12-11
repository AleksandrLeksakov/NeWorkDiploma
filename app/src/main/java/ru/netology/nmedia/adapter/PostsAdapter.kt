package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.extensions.formatPublishedDate
import ru.netology.nmedia.extensions.getLikeText
import ru.netology.nmedia.view.loadCircleCrop

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onShare(post: Post) {}
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
    private val appAuth: AppAuth // Добавляем AppAuth для проверки владельца
) : PagingDataAdapter<Post, PostViewHolder>(PostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener, appAuth)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
    private val appAuth: AppAuth
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.formatPublishedDate()
            content.text = post.content
            post.authorAvatar?.let { avatarUrl ->
                // Собираем полный URL
                val fullUrl = if (avatarUrl.startsWith("http")) {
                    avatarUrl
                } else {
                    "${BuildConfig.BASE_URL}/avatars/$avatarUrl"
                }
                // Загружаем с круговой обрезкой
                avatar.loadCircleCrop(fullUrl)
            } ?: run {
                // Если нет аватара, устанавливаем дефолтную иконку
                avatar.setImageResource(R.drawable.ic_image_placeholder)
            }

            // Настраиваем кнопку лайка
            like.isChecked = post.likedByMe
            like.text = post.getLikeText() // Используем extension

            // Определяем, принадлежит ли пост текущему пользователю
            val currentUserId = appAuth.authStateFlow.value?.id
            val isOwnedByMe = currentUserId != null && post.authorId == currentUserId

            // Показываем/скрываем меню в зависимости от владельца
            menu.visibility = if (isOwnedByMe) View.VISIBLE else View.INVISIBLE

            // Обработчик меню (три точки)
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    menu.setGroupVisible(R.id.owned, isOwnedByMe)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }

            // Обработчик лайка
            like.setOnClickListener {
                onInteractionListener.onLike(post)
            }

            // Обработчик шаринга
            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}