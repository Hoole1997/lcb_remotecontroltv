package com.example.lcb.app.remote.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lcb.app.databinding.ItemCommonBrandBinding
import com.example.lcb.app.remote.model.TvBrand
import com.example.lcb.app.remote.ui.BrandVisuals
import com.example.lcb.app.remote.ui.brandDisplayName
import com.example.lcb.app.remote.ui.brandInitial

class CommonBrandAdapter(
    private val onClick: (TvBrand) -> Unit,
) : RecyclerView.Adapter<CommonBrandAdapter.ViewHolder>() {
    private val items = mutableListOf<TvBrand>()

    fun submitList(newItems: List<TvBrand>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemCommonBrandBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], onClick)
    }

    class ViewHolder(private val binding: ItemCommonBrandBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TvBrand, onClick: (TvBrand) -> Unit) {
            val visual = BrandVisuals.forName(item.name)
            binding.letterText.text = item.name.brandInitial()
            binding.letterText.setTextColor(visual.color)
            binding.nameText.text = item.name.brandDisplayName()
            binding.root.setOnClickListener { onClick(item) }
        }
    }
}
