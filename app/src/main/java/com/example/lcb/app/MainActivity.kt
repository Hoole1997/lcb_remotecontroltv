package com.example.lcb.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lcb.app.databinding.ActivityMainHomeBinding
import com.example.lcb.app.remote.data.SharedPreferencesSavedTvRepository
import com.example.lcb.app.remote.ui.adapter.SavedTvAdapter
import com.example.lcb.app.remote.ui.applySystemBarInsets

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainHomeBinding
    private lateinit var savedTvRepository: SharedPreferencesSavedTvRepository
    private lateinit var savedTvAdapter: SavedTvAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarInsets(binding.main)

        savedTvRepository = SharedPreferencesSavedTvRepository(this)
        savedTvAdapter = SavedTvAdapter { tv ->
            startActivity(RemoteControlActivity.createIntent(this, tv.id))
        }
        binding.savedTvRecycler.layoutManager = LinearLayoutManager(this)
        binding.savedTvRecycler.adapter = savedTvAdapter

        // 首页入口统一通过 Material 组件分发，避免把导航逻辑散落在装饰性 View 上。
        binding.homeToolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_add_tv) {
                openBrandList()
                true
            } else {
                false
            }
        }
        binding.bottomNavigation.selectedItemId = R.id.remote_nav_tv
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            item.itemId == R.id.remote_nav_tv
        }
        binding.emptyAddButton.setOnClickListener { openBrandList() }
        binding.homeFloatingAdd.setOnClickListener { openBrandList() }
    }

    override fun onResume() {
        super.onResume()
        renderSavedTvs()
    }

    private fun renderSavedTvs() {
        val savedTvs = savedTvRepository.getSavedTvs()
        val isEmpty = savedTvs.isEmpty()
        binding.emptyStateContainer.isVisible = isEmpty
        binding.dataStateContainer.isVisible = !isEmpty
        savedTvAdapter.submitList(savedTvs)
    }

    private fun openBrandList() {
        startActivity(Intent(this, BrandListActivity::class.java))
    }
}
