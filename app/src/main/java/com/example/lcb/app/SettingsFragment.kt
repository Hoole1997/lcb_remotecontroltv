package com.example.lcb.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import com.example.lcb.app.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private data class Language(val tag: String, val label: String)

    private val languages = listOf(
        Language("", "跟随系统"),
        Language("zh-CN", "简体中文"),
        Language("en", "English"),
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.versionText.text = "版本 ${appVersionName()}"
        updateLanguageValue()

        binding.rowFeedback.setOnClickListener { sendFeedback() }
        binding.rowLanguage.setOnClickListener { showLanguageDialog() }
        binding.rowPrivacy.setOnClickListener { openPrivacyPolicy() }
    }

    private fun appVersionName(): String {
        val context = requireContext()
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    private fun sendFeedback() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@lcb-remote.app"))
            putExtra(Intent.EXTRA_SUBJECT, "${getString(R.string.app_name)} 反馈")
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "未找到可用的邮件应用", Toast.LENGTH_SHORT).show()
        }
    }

    private fun currentLanguage(): Language {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) return languages.first()
        val current = locales[0]?.language ?: return languages.first()
        return languages.firstOrNull { it.tag.startsWith(current) && it.tag.isNotEmpty() }
            ?: languages.first()
    }

    private fun updateLanguageValue() {
        binding.languageValue.text = currentLanguage().label
    }

    private fun showLanguageDialog() {
        val labels = languages.map { it.label }.toTypedArray()
        val checked = languages.indexOf(currentLanguage()).coerceAtLeast(0)
        AlertDialog.Builder(requireContext())
            .setTitle("选择语言")
            .setSingleChoiceItems(labels, checked) { dialog, which ->
                applyLanguage(languages[which])
                dialog.dismiss()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun applyLanguage(language: Language) {
        val locales = if (language.tag.isEmpty()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(language.tag)
        }
        AppCompatDelegate.setApplicationLocales(locales)
        updateLanguageValue()
    }

    private fun openPrivacyPolicy() {
        val uri = Uri.parse("https://sites.google.com/view/lcb-tv-remote/privacy-policy")
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "未找到可用的浏览器", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
