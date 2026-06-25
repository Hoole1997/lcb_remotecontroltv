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
import androidx.fragment.app.Fragment
import com.example.lcb.app.databinding.FragmentSettingsBinding

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

        binding.versionText.text = "版本 ${appVersionName()}"

        binding.rowFeedback.setOnClickListener { sendFeedback() }
        binding.rowRate.setOnClickListener { openStoreListing() }
        binding.rowAbout.setOnClickListener { showAbout() }
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

    private fun openStoreListing() {
        val context = requireContext()
        val marketUri = Uri.parse("market://details?id=${context.packageName}")
        try {
            startActivity(Intent(Intent.ACTION_VIEW, marketUri))
        } catch (e: ActivityNotFoundException) {
            val webUri = Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
            try {
                startActivity(Intent(Intent.ACTION_VIEW, webUri))
            } catch (e2: ActivityNotFoundException) {
                Toast.makeText(context, "未找到应用商店", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAbout() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.app_name))
            .setMessage("版本 ${appVersionName()}\n\n一款简洁的红外万能电视遥控器，无需联网即可控制你的电视。")
            .setPositiveButton("确定", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
