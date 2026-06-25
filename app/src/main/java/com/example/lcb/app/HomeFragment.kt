package com.example.lcb.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lcb.app.databinding.FragmentHomeBinding
import com.example.lcb.app.remote.data.SharedPreferencesSavedTvRepository
import com.example.lcb.app.remote.ui.adapter.SavedTvAdapter

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var savedTvRepository: SharedPreferencesSavedTvRepository
    private lateinit var savedTvAdapter: SavedTvAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedTvRepository = SharedPreferencesSavedTvRepository(requireContext())
        savedTvAdapter = SavedTvAdapter { tv ->
            startActivity(RemoteControlActivity.createIntent(requireContext(), tv.id))
        }
        binding.savedTvRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.savedTvRecycler.adapter = savedTvAdapter

        binding.homeToolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_add_tv) {
                openBrandList()
                true
            } else {
                false
            }
        }
        binding.emptyAddButton.setOnClickListener { openBrandList() }
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
        startActivity(Intent(requireContext(), BrandListActivity::class.java))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.savedTvRecycler.adapter = null
        _binding = null
    }
}
