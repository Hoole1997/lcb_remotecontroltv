package com.example.lcb.app.remote.ui

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun Activity.applySystemBarInsets(root: View) {
    ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
        insets
    }
}

fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
