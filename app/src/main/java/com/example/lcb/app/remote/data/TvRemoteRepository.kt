package com.example.lcb.app.remote.data

import android.content.Context
import com.example.lcb.app.remote.model.TvBrand
import com.example.lcb.app.remote.model.TvRemoteProfile

interface TvRemoteRepository {
    fun getBrands(): List<TvBrand>
    fun getProfiles(brand: TvBrand): List<TvRemoteProfile>
    fun getProfile(assetPath: String): TvRemoteProfile?
}

class AssetTvRemoteRepository(
    context: Context,
    private val parser: FlipperIrParser = FlipperIrParser(),
) : TvRemoteRepository {
    private val assets = context.applicationContext.assets
    private val profileCache = linkedMapOf<String, TvRemoteProfile>()

    override fun getBrands(): List<TvBrand> {
        return assets.list(TV_ROOT)
            .orEmpty()
            .filter { name -> listIrFiles("$TV_ROOT/$name").isNotEmpty() }
            .map { brandName ->
                TvBrand(
                    name = brandName,
                    assetPath = "$TV_ROOT/$brandName",
                    modelCount = listIrFiles("$TV_ROOT/$brandName").size,
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    override fun getProfiles(brand: TvBrand): List<TvRemoteProfile> {
        return listIrFiles(brand.assetPath)
            .mapNotNull { fileName -> getProfile("${brand.assetPath}/$fileName") }
            .sortedBy { it.displayName.lowercase() }
    }

    override fun getProfile(assetPath: String): TvRemoteProfile? = runCatching {
        profileCache[assetPath]?.let { return@runCatching it }

        val brand = assetPath.substringBeforeLast("/").substringAfterLast("/")
        val fileName = assetPath.substringAfterLast("/")
        val content = assets.open(assetPath).bufferedReader().use { it.readText() }
        val profile = TvRemoteProfile(
            brand = brand,
            fileName = fileName,
            displayName = fileName.removeSuffix(".ir").replace('_', ' '),
            assetPath = assetPath,
            commands = parser.parse(content),
        )
        profileCache[assetPath] = profile
        profile
    }.getOrNull()

    private fun listIrFiles(path: String): List<String> {
        return assets.list(path)
            .orEmpty()
            .filter { it.endsWith(".ir", ignoreCase = true) }
            .sortedBy { it.lowercase() }
    }

    private companion object {
        const val TV_ROOT = "irdb/tv"
    }
}
