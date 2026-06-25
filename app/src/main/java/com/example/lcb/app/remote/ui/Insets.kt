package com.example.lcb.app.remote.ui

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun Activity.applySystemBarInsets(root: View, bottomBar: View? = null) {
    val baseBottomPadding = bottomBar?.paddingBottom ?: 0
    ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        if (bottomBar == null) {
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
        } else {
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            bottomBar.setPadding(
                bottomBar.paddingLeft,
                bottomBar.paddingTop,
                bottomBar.paddingRight,
                baseBottomPadding + systemBars.bottom,
            )
        }
        insets
    }
}

fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
