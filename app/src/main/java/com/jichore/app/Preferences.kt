package com.jichore.app

import android.content.Context
import com.jichore.app.Consts.APP_PREFERENCE


fun getProgress(context: Context, key: Int): Int {
    val sharedPref = context.getSharedPreferences(APP_PREFERENCE, Context.MODE_PRIVATE)
    return sharedPref.getInt(key.toString(), 100)
}

fun saveProgress(context: Context, filter: Int, value: Int) {
    val sharedPref = context.getSharedPreferences(APP_PREFERENCE, Context.MODE_PRIVATE)
    val editor = sharedPref.edit()
    editor.putInt(filter.toString(), value)

    editor.apply()
}