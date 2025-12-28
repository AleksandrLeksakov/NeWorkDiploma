package ru.netology.nework.util

object AppConst {
    // Fragment result keys
    const val MENTIONED = "mentioned"
    const val SPEAKERS = "speakers"
    const val LIKERS = "likers"
    const val PARTICIPANT = "participants"
    const val USERS_FRAGMENT_RESULT = "usersFragmentResult"
    const val SELECT_USER = "selectUser"
    const val MAP_POINT = "point"
    const val MAPS_FRAGMENT_RESULT = "mapsFragmentResult"
    const val USER_ID = "userId"
    const val EDIT_POST = "editPost"
    const val EDIT_EVENT = "editEvent"

    // Media constraints
    /** Максимальный размер видео файла в байтах (15 МБ) */
    const val MAX_VIDEO_SIZE_BYTES = 15L * 1024 * 1024 // 15_728_640

    /** Максимальный размер изображения в пикселях */
    const val MAX_IMAGE_SIZE_PX = 2048

    // Paging
    /** Размер страницы для пагинации */
    const val PAGE_SIZE = 4
}