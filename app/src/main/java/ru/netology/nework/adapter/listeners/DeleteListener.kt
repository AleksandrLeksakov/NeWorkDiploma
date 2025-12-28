package ru.netology.nework.adapter.listeners

import ru.netology.nework.dto.FeedItem

fun interface DeleteListener {
    fun onDelete(feedItem: FeedItem)
}