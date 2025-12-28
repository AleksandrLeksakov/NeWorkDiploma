package ru.netology.nework.adapter.listeners

import ru.netology.nework.dto.UserResponse

fun interface UserSelectListener {
    fun onSelectUser(userResponse: UserResponse)
}