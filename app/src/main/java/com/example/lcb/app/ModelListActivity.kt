package com.example.lcb.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lcb.app.databinding.ActivityModelListBinding
import com.example.lcb.app.remote.data.AssetTvRemoteRepository
import com.example.lcb.app.remote.data.SharedPreferencesSavedTvRepository
import com.example.lcb.app.remote.model.TvBrand
import com.example.lcb.app.remote.model.TvRemoteProfile
import com.example.lcb.app.remote.ui.AddRemoteSheet
import com.example.lcb.app.remote.ui.BrandVisuals
import com.example.lcb.app.remote.ui.adapter.ModelAdapter
import com.example.lcb.app.remote.ui.applySystemBarInsets
import com.example.lcb.app.remote.ui.bindBrandLogo
import com.example.lcb.app.remote.ui.brandDisplayName

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
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.searchEditText.addTextChangedListener(searchWatcher())
        binding.contentScroll.setOnScrollChangeListener { _, _, _, _, _ ->
            if (binding.searchEditText.isFocused) {
                binding.searchEditText.clearFocus()
                hideKeyboard()
            }
        }
        binding.addButton.setOnClickListener {
            modelAdapter.selectedProfile()?.let(::showAddDialog)
        }

        renderModels("")
    }

    private fun bindBrandHeader() {
        val visual = BrandVisuals.forName(brand.name)
        bindBrandLogo(binding.brandLogoImage, binding.brandFallbackInitialText, brand.name, visual.color)
        binding.brandNameText.text = brand.name.brandDisplayName()
        binding.brandCountText.text = "${brand.modelCount} 个可用遥控码"
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    private fun showAddDialog(profile: TvRemoteProfile) {
        AddRemoteSheet.show(this, profile) { savedTv ->
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
