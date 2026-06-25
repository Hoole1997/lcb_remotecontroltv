package com.example.lcb.app.remote.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.example.lcb.app.databinding.SheetAddRemoteBinding
import com.example.lcb.app.remote.model.SavedTv
import com.example.lcb.app.remote.model.TvRemoteProfile
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.UUID

object AddRemoteSheet {
    fun show(
        activity: AppCompatActivity,
        profile: TvRemoteProfile,
        onSaved: (SavedTv) -> Unit,
    ) {
        val binding = SheetAddRemoteBinding.inflate(activity.layoutInflater)
        val dialog = BottomSheetDialog(activity)
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
        )

        binding.sheetSubtitle.text =
            "${profile.brand.brandDisplayName()} · ${profile.displayName}"
        binding.nameInput.setText("${profile.brand.brandDisplayName()} 电视")
        binding.sceneInput.setText("客厅")

        binding.saveButton.setOnClickListener {
            val scene = binding.sceneInput.text?.toString()?.trim().orEmpty().ifBlank { "客厅" }
            val name = binding.nameInput.text?.toString()?.trim().orEmpty().ifBlank { "$scene 电视" }
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
            dialog.dismiss()
        }

        dialog.show()
    }
}
