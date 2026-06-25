package com.example.lcb.app.remote.ui

import android.graphics.Color
import com.example.lcb.app.R

data class BrandVisual(
    val color: Int,
    val circleBackground: Int,
)

object BrandVisuals {
    fun forName(name: String): BrandVisual {
        return when (name.lowercase()) {
            "lg" -> BrandVisual(Color.parseColor("#B92A63"), R.drawable.bg_brand_circle_pink)
            "sony" -> BrandVisual(Color.parseColor("#202124"), R.drawable.bg_brand_circle_black)
            "tcl" -> BrandVisual(Color.parseColor("#F04B2F"), R.drawable.bg_brand_circle_orange)
            "panasonic", "philips" -> BrandVisual(Color.parseColor("#2F5DA8"), R.drawable.bg_brand_circle_teal)
            "sharp" -> BrandVisual(Color.parseColor("#D21D2B"), R.drawable.bg_brand_circle_teal)
            "xiaomi" -> BrandVisual(Color.parseColor("#F04B2F"), R.drawable.bg_brand_circle_orange)
            else -> BrandVisual(Color.parseColor("#0F806B"), R.drawable.bg_brand_circle_teal)
        }
    }
}

fun String.brandDisplayName(): String = replace('_', ' ')

fun String.brandInitial(): String = brandDisplayName().firstOrNull()?.uppercaseChar()?.toString().orEmpty()
