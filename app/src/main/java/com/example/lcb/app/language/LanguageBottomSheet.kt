package com.example.lcb.app.language

import androidx.appcompat.app.AppCompatActivity
import com.example.lcb.app.databinding.SheetLanguagePickerBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

object LanguageBottomSheet {
    fun show(
        activity: AppCompatActivity,
        selectedTag: String,
        onSelected: (AppLanguage) -> Unit,
    ) {
        val binding = SheetLanguagePickerBinding.inflate(activity.layoutInflater)
        val dialog = BottomSheetDialog(activity)
        val adapter = LanguageOptionAdapter { language ->
            onSelected(language)
            dialog.dismiss()
        }

        binding.languageRecyclerView.adapter = adapter
        adapter.submitList(AppLanguage.supported(activity), selectedTag)

        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT),
        )
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.show()
    }
}
