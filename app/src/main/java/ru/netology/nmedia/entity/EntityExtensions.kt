package ru.netology.nmedia.entity

import ru.netology.nmedia.dto.Post

fun Post.toEntity(): PostEntity = PostEntity.fromDto(this)

fun List<Post>.toEntity(): List<PostEntity> = map { it.toEntity() }

fun PostEntity.toDto(): Post = this.toDto() // Используем существующий метод

fun List<PostEntity>.toDto(): List<Post> = map { it.toDto() }