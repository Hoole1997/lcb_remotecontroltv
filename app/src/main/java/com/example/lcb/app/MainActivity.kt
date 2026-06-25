package com.example.lcb.app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.lcb.app.databinding.ActivityMainBinding
import com.example.lcb.app.remote.ui.applySystemBarInsets

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarInsets(binding.main, binding.bottomNavigation)

        if (savedInstanceState == null) {
            showFragment(HomeFragment())
            binding.bottomNavigation.selectedItemId = R.id.remote_nav_tv
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.remote_nav_tv -> {
                    showFragment(HomeFragment())
                    true
                }
                R.id.remote_nav_settings -> {
                    showFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.navHostContainer.id, fragment)
            .commit()
    }
}
