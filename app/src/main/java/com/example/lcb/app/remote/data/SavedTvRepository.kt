package com.example.lcb.app.remote.data

import android.content.Context
import com.example.lcb.app.remote.model.SavedTv
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

interface SavedTvRepository {
    fun getSavedTvs(): List<SavedTv>
    fun add(savedTv: SavedTv)
    fun delete(id: String)
}

class SharedPreferencesSavedTvRepository(
    context: Context,
    private val gson: Gson = Gson(),
) : SavedTvRepository {
    private val preferences = context.applicationContext.getSharedPreferences(
        "tv_remote_saved_devices",
        Context.MODE_PRIVATE,
    )
    private val listType = object : TypeToken<List<SavedTv>>() {}.type

    override fun getSavedTvs(): List<SavedTv> {
        val json = preferences.getString(KEY_DEVICES, null) ?: return emptyList()
        return runCatching { gson.fromJson<List<SavedTv>>(json, listType) }
            .getOrDefault(emptyList())
    }

    override fun add(savedTv: SavedTv) {
        val updated = getSavedTvs()
            .filterNot { it.id == savedTv.id }
            .plus(savedTv)
        save(updated)
    }

    override fun delete(id: String) {
        save(getSavedTvs().filterNot { it.id == id })
    }

    private fun save(items: List<SavedTv>) {
        preferences.edit()
            .putString(KEY_DEVICES, gson.toJson(items))
            .apply()
    }

    private companion object {
        const val KEY_DEVICES = "devices"
    }
}
