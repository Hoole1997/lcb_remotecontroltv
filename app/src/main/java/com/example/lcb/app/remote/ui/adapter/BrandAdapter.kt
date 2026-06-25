package com.example.lcb.app.remote.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lcb.app.databinding.ItemBrandRowBinding
import com.example.lcb.app.remote.model.TvBrand
import com.example.lcb.app.remote.ui.BrandVisuals
import com.example.lcb.app.remote.ui.brandDisplayName
import com.example.lcb.app.remote.ui.brandInitial

class BrandAdapter(
    private val onClick: (TvBrand) -> Unit,
) : RecyclerView.Adapter<BrandAdapter.ViewHolder>() {
    private val items = mutableListOf<TvBrand>()

    fun submitList(newItems: List<TvBrand>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemBrandRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], isLast = position == items.lastIndex, onClick)
    }

    class ViewHolder(private val binding: ItemBrandRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TvBrand, isLast: Boolean, onClick: (TvBrand) -> Unit) {
            val visual = BrandVisuals.forName(item.name)
            binding.letterText.text = item.name.brandInitial()
            binding.letterText.setTextColor(visual.color)
            binding.nameText.text = item.name.brandDisplayName()
            binding.countText.text = "${item.modelCount} 个型号"
            binding.divider.visibility = if (isLast) View.GONE else View.VISIBLE
            binding.root.setOnClickListener { onClick(item) }
        }
    }
}
