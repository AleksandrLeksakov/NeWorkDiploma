package ru.netology.nework.repository.interfaces

import ru.netology.nework.dto.Post

interface WallRepository {

    suspend fun getUserWall(userId: Long): List<Post>
}