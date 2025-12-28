package ru.netology.nework.adapter.listeners

import ru.netology.nework.dto.FeedItem

fun interface LikeListener {
    fun onLike(feedItem: FeedItem)
}