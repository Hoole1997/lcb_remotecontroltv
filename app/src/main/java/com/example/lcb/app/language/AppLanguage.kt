package com.example.lcb.app.language

import android.content.Context
import androidx.core.os.ConfigurationCompat
import com.example.lcb.app.R
import java.util.Locale

data class AppLanguage(
    val tag: String,
    val countryCode: String,
    val countryName: String,
    val languageName: String,
) {
    fun matches(selectedTag: String): Boolean {
        if (tag.isEmpty()) return selectedTag.isEmpty()
        val selected = Locale.forLanguageTag(selectedTag)
        val current = Locale.forLanguageTag(tag)
        return selected.language == current.language &&
            (selected.country.isEmpty() || selected.country == current.country)
    }

    companion object {
        private val supportedLocaleTags = listOf(
            "en-US",
            "zh-CN",
            "hi-IN",
            "es-ES",
            "ar-SA",
            "pt-BR",
            "bn-BD",
            "ru-RU",
            "id-ID",
            "ja-JP",
            "ko-KR",
            "de-DE",
            "fr-FR",
            "vi-VN",
            "tr-TR",
        )

        fun supported(context: Context): List<AppLanguage> {
            val displayLocale = ConfigurationCompat.getLocales(context.resources.configuration)[0]
                ?: Locale.getDefault()
            return listOf(system(context)) + supportedLocaleTags.map { tag ->
                val locale = Locale.forLanguageTag(tag)
                AppLanguage(
                    tag = tag,
                    countryCode = locale.country.ifEmpty { locale.language }.uppercase(Locale.ROOT),
                    countryName = locale.getDisplayCountry(displayLocale).ifBlank { tag },
                    languageName = locale.getDisplayLanguage(displayLocale).ifBlank { tag },
                )
            }
        }

        private fun system(context: Context): AppLanguage {
            val systemLocale = Locale.getDefault()
            return AppLanguage(
                tag = "",
                countryCode = "SYS",
                countryName = context.getString(R.string.language_follow_system),
                languageName = systemLocale.getDisplayLanguage(systemLocale),
            )
        }
    }
}
