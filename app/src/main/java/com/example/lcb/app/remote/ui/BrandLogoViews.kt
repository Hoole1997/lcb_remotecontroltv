package com.example.lcb.app.remote.ui

import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible

/**
 * Binds a packaged brand logo when available and falls back to the previous
 * initial-based tile for brands that do not have a verified logo asset yet.
 */
fun bindBrandLogo(
    logoImage: ImageView,
    fallbackInitialText: TextView,
    brandName: String,
    fallbackColor: Int,
) {
    val logoResId = BrandLogoResolver.logoForName(brandName)
    logoImage.isVisible = logoResId != null
    fallbackInitialText.isVisible = logoResId == null
    if (logoResId != null) {
        logoImage.setImageResource(logoResId)
    } else {
        fallbackInitialText.text = brandName.brandInitial()
        fallbackInitialText.setTextColor(fallbackColor)
    }
}
