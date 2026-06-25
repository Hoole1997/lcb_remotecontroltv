package com.example.lcb.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lcb.app.databinding.ActivityModelListBinding
import com.example.lcb.app.remote.data.AssetTvRemoteRepository
import com.example.lcb.app.remote.data.SharedPreferencesSavedTvRepository
import com.example.lcb.app.remote.model.TvBrand
import com.example.lcb.app.remote.model.TvRemoteProfile
import com.example.lcb.app.remote.ui.AddRemoteDialog
import com.example.lcb.app.remote.ui.BrandVisuals
import com.example.lcb.app.remote.ui.adapter.ModelAdapter
import com.example.lcb.app.remote.ui.applySystemBarInsets
import com.example.lcb.app.remote.ui.brandDisplayName
import com.example.lcb.app.remote.ui.brandInitial

class ModelListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityModelListBinding
    private lateinit var remoteRepository: AssetTvRemoteRepository
    private lateinit var savedTvRepository: SharedPreferencesSavedTvRepository
    private lateinit var modelAdapter: ModelAdapter
    private lateinit var brand: TvBrand
    private var allProfiles: List<TvRemoteProfile> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityModelListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarInsets(binding.main)

        brand = intent.toBrand()
        remoteRepository = AssetTvRemoteRepository(this)
        savedTvRepository = SharedPreferencesSavedTvRepository(this)
        allProfiles = remoteRepository.getProfiles(brand)

        modelAdapter = ModelAdapter { /* Selection is handled inside the adapter. */ }
        binding.modelRecycler.layoutManager = LinearLayoutManager(this)
        binding.modelRecycler.adapter = modelAdapter

        bindBrandHeader()
        binding.backButton.setOnClickListener { finish() }
        binding.searchEditText.addTextChangedListener(searchWatcher())
        binding.addButton.setOnClickListener {
            modelAdapter.selectedProfile()?.let(::showAddDialog)
        }

        renderModels("")
    }

    private fun bindBrandHeader() {
        val visual = BrandVisuals.forName(brand.name)
        binding.brandLetterText.text = brand.name.brandInitial()
        binding.brandLetterText.backgroundTintList = android.content.res.ColorStateList.valueOf(visual.color)
        binding.brandNameText.text = brand.name.brandDisplayName()
        binding.brandCountText.text = "${brand.modelCount} 个可用遥控码"
        binding.addButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.remote_primary)
    }

    private fun showAddDialog(profile: TvRemoteProfile) {
        AddRemoteDialog.show(this, profile) { savedTv ->
            savedTvRepository.add(savedTv)
            Toast.makeText(this, "已添加 ${savedTv.displayName}", Toast.LENGTH_SHORT).show()
            startActivity(RemoteControlActivity.createIntent(this, savedTv.id))
            finish()
        }
    }

    private fun renderModels(query: String) {
        val filtered = allProfiles.filter { profile ->
            profile.displayName.contains(query, ignoreCase = true) ||
                profile.commands.any { it.name.contains(query, ignoreCase = true) }
        }
        modelAdapter.submitList(filtered, keepSelection = true)
    }

    private fun searchWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                renderModels(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) = Unit
        }
    }

    private fun Intent.toBrand(): TvBrand {
        return TvBrand(
            name = getStringExtra(EXTRA_BRAND_NAME).orEmpty(),
            assetPath = getStringExtra(EXTRA_BRAND_ASSET_PATH).orEmpty(),
            modelCount = getIntExtra(EXTRA_BRAND_MODEL_COUNT, 0),
        )
    }

    companion object {
        private const val EXTRA_BRAND_NAME = "brand_name"
        private const val EXTRA_BRAND_ASSET_PATH = "brand_asset_path"
        private const val EXTRA_BRAND_MODEL_COUNT = "brand_model_count"

        fun createIntent(context: Context, brand: TvBrand): Intent {
            return Intent(context, ModelListActivity::class.java)
                .putExtra(EXTRA_BRAND_NAME, brand.name)
                .putExtra(EXTRA_BRAND_ASSET_PATH, brand.assetPath)
                .putExtra(EXTRA_BRAND_MODEL_COUNT, brand.modelCount)
        }
    }
}
