package com.example.lcb.app.remote.ui.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.lcb.app.R
import com.example.lcb.app.databinding.ItemSavedTvCardBinding
import com.example.lcb.app.remote.model.SavedTv
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
        holder.bind(items[position], selected = position == 0, onClick)
    }

    class ViewHolder(private val binding: ItemSavedTvCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SavedTv, selected: Boolean, onClick: (SavedTv) -> Unit) {
            val context = binding.root.context
            val primary = ContextCompat.getColor(context, R.color.remote_primary)
            val secondary = ContextCompat.getColor(context, R.color.remote_text_secondary)
            val outline = ContextCompat.getColor(context, R.color.remote_outline)
            val softTeal = ContextCompat.getColor(context, R.color.remote_soft_teal)
            val chipGray = ContextCompat.getColor(context, R.color.remote_chip_gray)

            val density = context.resources.displayMetrics.density
            binding.card.strokeColor = if (selected) primary else outline
            binding.card.strokeWidth = ((if (selected) 2f else 1f) * density).toInt()
            binding.deviceIconContainer.backgroundTintList =
                ColorStateList.valueOf(if (selected) softTeal else chipGray)
            binding.deviceIcon.imageTintList = ColorStateList.valueOf(if (selected) primary else secondary)
            binding.deviceNameText.text = item.displayName
            binding.modelText.text = "${item.brand.brandDisplayName()} TV"
            binding.sceneChip.text = item.scene
            binding.sceneChip.setTextColor(if (selected) primary else secondary)
            binding.root.setOnClickListener { onClick(item) }
        }
    }
}
