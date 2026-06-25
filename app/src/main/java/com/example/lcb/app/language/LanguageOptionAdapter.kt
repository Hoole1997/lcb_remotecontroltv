package com.example.lcb.app.language

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lcb.app.databinding.ItemLanguageOptionBinding

class LanguageOptionAdapter(
    private val onClick: (AppLanguage) -> Unit,
) : RecyclerView.Adapter<LanguageOptionAdapter.ViewHolder>() {
    private val items = mutableListOf<AppLanguage>()
    private var selectedTag: String = ""

    fun submitList(newItems: List<AppLanguage>, selectedTag: String) {
        items.clear()
        items.addAll(newItems)
        this.selectedTag = selectedTag
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemLanguageOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, item.matches(selectedTag), onClick)
    }

    class ViewHolder(
        private val binding: ItemLanguageOptionBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AppLanguage, selected: Boolean, onClick: (AppLanguage) -> Unit) {
            binding.countryCodeText.text = item.countryCode
            binding.countryNameText.text = item.countryName
            binding.languageNameText.text = item.languageName
            binding.optionRoot.isSelected = selected
            binding.checkIcon.visibility = if (selected) View.VISIBLE else View.GONE
            binding.root.setOnClickListener { onClick(item) }
        }
    }
}
