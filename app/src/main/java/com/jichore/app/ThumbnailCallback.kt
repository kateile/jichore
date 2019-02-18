package com.jichore.app

import kotlinx.coroutines.Job

interface ThumbnailCallback {
    fun onThumbnailClick(effect: Int): Job
}