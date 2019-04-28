package com.jichore.app

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WorkerViewModel : ViewModel() {

    val seekBarProgress: MutableLiveData<Int>? = null
    val effectType: MutableLiveData<Int>? = null

    fun setSeekBarProgress(number: Int) {
        seekBarProgress?.value = number
    }

    fun setEffectType(number: Int) {
        effectType?.value = number
    }
}