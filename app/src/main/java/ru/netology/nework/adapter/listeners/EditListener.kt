// adapter/listeners/EditListener.kt
package ru.netology.nework.adapter.listeners

import ru.netology.nework.dto.FeedItem

fun interface EditListener {
    fun onEdit(feedItem: FeedItem)
}