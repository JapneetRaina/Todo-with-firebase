package com.japneet.todoapplication.config

import android.content.Context
import android.content.SharedPreferences

class SharedPref(_context : Context) {
    var pref: SharedPreferences
    var editor: SharedPreferences.Editor
    private var PRIVATE_MODE = 0

    init {
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref.edit()
    }

    var imageUrl: String?
        get() = pref.getString("imageUrl", null)
        set(imageURl) {
            editor.putString("imageUrl", imageURl)
            editor.commit()
        }
    var textStyle: String?
        get() = pref.getString("TextStyle", null)
        set(textStyle) {
            editor.putString("TextStyle", textStyle)
            editor.commit()
        }

    companion object {
        private const val PREF_NAME = "AndroidHiveLogin"
    }
}