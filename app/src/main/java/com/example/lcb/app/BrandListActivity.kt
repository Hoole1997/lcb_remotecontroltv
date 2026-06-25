package com.example.lcb.app

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lcb.app.databinding.ActivityBrandListBinding
import com.example.lcb.app.remote.data.AssetTvRemoteRepository
import com.example.lcb.app.remote.data.SharedPreferencesSavedTvRepository
import com.example.lcb.app.remote.model.TvBrand
import com.example.lcb.app.remote.model.TvRemoteProfile
import com.example.lcb.app.remote.ui.AddRemoteDialog
import com.example.lcb.app.remote.ui.adapter.BrandAdapter
import com.example.lcb.app.remote.ui.adapter.CommonBrandAdapter
import com.example.lcb.app.remote.ui.applySystemBarInsets

class BrandListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBrandListBinding
    private lateinit var remoteRepository: AssetTvRemoteRepository
    private lateinit var savedTvRepository: SharedPreferencesSavedTvRepository
    private lateinit var brandAdapter: BrandAdapter
    private lateinit var commonBrandAdapter: CommonBrandAdapter
    private var allBrands: List<TvBrand> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityBrandListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarInsets(binding.main)

        remoteRepository = AssetTvRemoteRepository(this)
        savedTvRepository = SharedPreferencesSavedTvRepository(this)
        allBrands = remoteRepository.getBrands()

        commonBrandAdapter = CommonBrandAdapter(::openBrand)
        brandAdapter = BrandAdapter(::openBrand)

        binding.commonBrandRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.commonBrandRecycler.adapter = commonBrandAdapter
        binding.brandRecycler.layoutManager = LinearLayoutManager(this)
        binding.brandRecycler.adapter = brandAdapter

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.searchEditText.addTextChangedListener(searchWatcher())

        commonBrandAdapter.submitList(commonBrands())
        renderBrands("")
    }

    private fun openBrand(brand: TvBrand) {
        val profiles = remoteRepository.getProfiles(brand)
        if (profiles.size == 1) {
            showAddDialog(profiles.first())
        } else {
            startActivity(ModelListActivity.createIntent(this, brand))
        }
    }

    private fun showAddDialog(profile: TvRemoteProfile) {
        AddRemoteDialog.show(this, profile) { savedTv ->
            savedTvRepository.add(savedTv)
            Toast.makeText(this, "已添加 ${savedTv.displayName}", Toast.LENGTH_SHORT).show()
            startActivity(RemoteControlActivity.createIntent(this, savedTv.id))
            finish()
        }
    }

    private fun renderBrands(query: String) {
        val filtered = allBrands.filter { brand ->
            brand.name.contains(query, ignoreCase = true)
        }
        brandAdapter.submitList(filtered)
    }

    private fun commonBrands(): List<TvBrand> {
        val names = listOf("Samsung", "LG", "Sony", "TCL", "Hisense")
        return names.mapNotNull { name -> allBrands.firstOrNull { it.name.equals(name, ignoreCase = true) } }
    }

    private fun searchWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                renderBrands(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) = Unit
        }
    }
}
