package com.example.lcb.app.remote.ui

import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.lcb.app.remote.model.SavedTv
import com.example.lcb.app.remote.model.TvRemoteProfile
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.UUID

object AddRemoteDialog {
    fun show(
        activity: AppCompatActivity,
        profile: TvRemoteProfile,
        onSaved: (SavedTv) -> Unit,
    ) {
        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(activity.dp(8), activity.dp(8), activity.dp(8), 0)
        }
        val sceneInput = EditText(activity).apply {
            hint = "场景备注，例如 客厅 / 卧室 / 公司"
            setText("客厅")
            setSingleLine(true)
        }
        val nameInput = EditText(activity).apply {
            hint = "设备名称"
            setText("${profile.brand.brandDisplayName()} 电视")
            setSingleLine(true)
        }
        content.addView(sceneInput)
        content.addView(
            nameInput,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply { topMargin = activity.dp(12) },
        )

        MaterialAlertDialogBuilder(activity)
            .setTitle("添加遥控器")
            .setMessage("${profile.brand.brandDisplayName()} · ${profile.displayName}")
            .setView(content)
            .setNegativeButton("取消", null)
            .setPositiveButton("添加") { _, _ ->
                val scene = sceneInput.text?.toString()?.trim().orEmpty().ifBlank { "客厅" }
                val name = nameInput.text?.toString()?.trim().orEmpty().ifBlank { "$scene 电视" }
                onSaved(
                    SavedTv(
                        id = UUID.randomUUID().toString(),
                        displayName = name,
                        scene = scene,
                        brand = profile.brand,
                        modelName = profile.displayName,
                        assetPath = profile.assetPath,
                    ),
                )
            }
            .show()
    }
}
