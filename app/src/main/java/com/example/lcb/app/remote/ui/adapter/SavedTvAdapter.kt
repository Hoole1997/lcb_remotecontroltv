package com.example.lcb.app.remote.ui.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.lcb.app.R
import com.example.lcb.app.databinding.ItemSavedTvCardBinding
import com.example.lcb.app.remote.model.SavedTv
import com.example.lcb.app.remote.ui.BrandLogoResolver
import com.example.lcb.app.remote.ui.brandDisplayName

class SavedTvAdapter(
    private val onClick: (SavedTv) -> Unit,
) : RecyclerView.Adapter<SavedTvAdapter.ViewHolder>() {
    private val items = mutableListOf<SavedTv>()

    fun submitList(newItems: List<SavedTv>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemSavedTvCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], onClick)
    }

    class ViewHolder(private val binding: ItemSavedTvCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SavedTv, onClick: (SavedTv) -> Unit) {
            val context = binding.root.context
            val secondary = ContextCompat.getColor(context, R.color.remote_text_secondary)
            val chipGray = ContextCompat.getColor(context, R.color.remote_chip_gray)
            val logoResId = BrandLogoResolver.logoForName(item.brand)

            binding.deviceIconContainer.backgroundTintList = ColorStateList.valueOf(chipGray)
            if (logoResId != null) {
                binding.deviceIcon.setImageResource(logoResId)
                binding.deviceIcon.imageTintList = null
            } else {
                binding.deviceIcon.setImageResource(R.drawable.ic_remote_tv)
                binding.deviceIcon.imageTintList = ColorStateList.valueOf(secondary)
            }
            binding.deviceNameText.text = item.displayName
            binding.modelText.text = "${item.brand.brandDisplayName()} TV"
            binding.sceneChip.text = item.scene
            binding.sceneChip.setTextColor(secondary)
            binding.root.setOnClickListener { onClick(item) }
        }
    }
}
