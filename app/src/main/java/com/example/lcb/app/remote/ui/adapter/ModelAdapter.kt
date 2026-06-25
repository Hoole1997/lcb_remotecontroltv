package com.example.lcb.app.remote.ui.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.lcb.app.R
import com.example.lcb.app.databinding.ItemModelRowBinding
import com.example.lcb.app.remote.model.IrSignal
import com.example.lcb.app.remote.model.TvRemoteProfile

class ModelAdapter(
    private val onClick: (TvRemoteProfile) -> Unit,
) : RecyclerView.Adapter<ModelAdapter.ViewHolder>() {
    private val items = mutableListOf<TvRemoteProfile>()
    var selectedAssetPath: String? = null
        private set

    fun submitList(newItems: List<TvRemoteProfile>, keepSelection: Boolean = false) {
        items.clear()
        items.addAll(newItems)
        if (!keepSelection || selectedAssetPath == null || items.none { it.assetPath == selectedAssetPath }) {
            selectedAssetPath = items.firstOrNull()?.assetPath
        }
        notifyDataSetChanged()
    }

    fun selectedProfile(): TvRemoteProfile? = items.firstOrNull { it.assetPath == selectedAssetPath }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemModelRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, selected = item.assetPath == selectedAssetPath, onClick = {
            selectedAssetPath = item.assetPath
            notifyDataSetChanged()
            onClick(item)
        })
    }

    class ViewHolder(private val binding: ItemModelRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TvRemoteProfile, selected: Boolean, onClick: () -> Unit) {
            val context = binding.root.context
            val primary = ContextCompat.getColor(context, R.color.remote_primary)
            val secondary = ContextCompat.getColor(context, R.color.remote_text_secondary)
            binding.card.strokeColor = if (selected) primary else ContextCompat.getColor(context, R.color.remote_outline)
            binding.remoteIcon.imageTintList = ColorStateList.valueOf(if (selected) primary else secondary)
            binding.nameText.text = item.displayName
            binding.protocolText.text = "协议: ${item.primaryProtocol()}"
            binding.recommendText.visibility = if (selected) View.VISIBLE else View.GONE
            binding.checkIcon.visibility = if (selected) View.VISIBLE else View.GONE
            binding.root.setOnClickListener { onClick() }
        }

        private fun TvRemoteProfile.primaryProtocol(): String {
            return commands
                .mapNotNull { (it.signal as? IrSignal.Parsed)?.protocol }
                .firstOrNull()
                ?: "Raw"
        }
    }
}
