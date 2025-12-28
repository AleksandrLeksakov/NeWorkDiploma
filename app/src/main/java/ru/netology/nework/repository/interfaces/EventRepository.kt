package ru.netology.nework.repository.interfaces

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.FeedItem
import ru.netology.nework.model.AttachmentModel

interface EventRepository {

    val dataEvent: Flow<PagingData<FeedItem>>

    suspend fun saveEvent(event: Event)

    suspend fun saveEventWithAttachment(event: Event, attachmentModel: AttachmentModel)

    suspend fun deleteEvent(id: Long)

    suspend fun likeEvent(event: Event)
}