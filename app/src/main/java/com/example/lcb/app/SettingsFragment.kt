package com.example.lcb.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import com.example.lcb.app.databinding.FragmentSettingsBinding
import com.example.lcb.app.language.AppLanguage
import com.example.lcb.app.language.LanguageBottomSheet

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

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

        binding.versionText.text = getString(R.string.settings_version_format, appVersionName())
        updateLanguageValue()

        binding.rowFeedback.setOnClickListener { sendFeedback() }
        binding.rowLanguage.setOnClickListener { showLanguageSheet() }
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
            putExtra(Intent.EXTRA_EMAIL, arrayOf("biolumianescent@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject_format, getString(R.string.app_name)))
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.mail_app_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectedLanguageTag(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (locales.isEmpty) "" else locales[0]?.toLanguageTag().orEmpty()
    }

    private fun updateLanguageValue() {
        val selectedTag = selectedLanguageTag()
        val language = AppLanguage.supported(requireContext()).firstOrNull { it.matches(selectedTag) }
            ?: AppLanguage.supported(requireContext()).first()
        binding.languageValue.text = language.countryName
    }

    private fun showLanguageSheet() {
        LanguageBottomSheet.show(
            activity = requireActivity() as androidx.appcompat.app.AppCompatActivity,
            selectedTag = selectedLanguageTag(),
            onSelected = ::applyLanguage,
        )
    }

    private fun applyLanguage(language: AppLanguage) {
        val locales = if (language.tag.isEmpty()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(language.tag)
        }
        AppCompatDelegate.setApplicationLocales(locales)
        updateLanguageValue()
    }

    private fun openPrivacyPolicy() {
        val uri = Uri.parse("https://bioluminescents.com/privacy.html")
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.browser_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
