package ru.netology.nework.auth

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenHolder @Inject constructor() {
    var token: String? = null
}