package ru.netology.nework.adapter.listeners

import ru.netology.nework.dto.FeedItem

fun interface CardOpenListener {
    fun onOpenCard(feedItem: FeedItem)
}