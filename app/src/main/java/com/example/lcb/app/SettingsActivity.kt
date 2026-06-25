package com.example.lcb.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.lcb.app.databinding.ActivitySettingsBinding
import com.example.lcb.app.remote.ui.applySystemBarInsets

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarInsets(binding.main)

        binding.versionText.text = "版本 ${appVersionName()}"

        binding.backButton.setOnClickListener { finish() }
        binding.rowFeedback.setOnClickListener { sendFeedback() }
        binding.rowRate.setOnClickListener { openStoreListing() }
        binding.rowAbout.setOnClickListener { showAbout() }
    }

    private fun appVersionName(): String {
        return try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0.0"
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
            Toast.makeText(this, "未找到可用的邮件应用", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openStoreListing() {
        val marketUri = Uri.parse("market://details?id=$packageName")
        try {
            startActivity(Intent(Intent.ACTION_VIEW, marketUri))
        } catch (e: ActivityNotFoundException) {
            val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            try {
                startActivity(Intent(Intent.ACTION_VIEW, webUri))
            } catch (e2: ActivityNotFoundException) {
                Toast.makeText(this, "未找到应用商店", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAbout() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_name))
            .setMessage("版本 ${appVersionName()}\n\n一款简洁的红外万能电视遥控器，无需联网即可控制你的电视。")
            .setPositiveButton("确定", null)
            .show()
    }
}
