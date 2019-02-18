package com.jichore.app

fun getRandomString(length: Int): String {
    val allowedChars = "abcdefghiklmnopqrstuvwxyz"
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}