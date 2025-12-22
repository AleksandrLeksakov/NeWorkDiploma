package ru.netology.nework.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.nework.api.ApiService
import ru.netology.nework.dto.PushToken
import javax.inject.Inject

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

    @Inject
    lateinit var api: ApiService

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        sendPushToken(token)
    }

    private fun sendPushToken(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                api.sendPushToken(PushToken(token))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // Обработка push уведомлений
        message.notification?.let {
            // Показать уведомление
        }
    }
}