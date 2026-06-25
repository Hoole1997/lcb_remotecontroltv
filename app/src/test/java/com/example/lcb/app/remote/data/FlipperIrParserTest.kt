package com.example.lcb.app.remote.data

import com.example.lcb.app.remote.model.RemoteAction
import com.example.lcb.app.remote.model.TvRemoteProfile
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FlipperIrParserTest {
    @Test
    fun xiaomiRawProfileMapsOpenAndMoreToRemoteActions() {
        val commands = FlipperIrParser().parse(readAssetText("irdb/tv/Xiaomi/Xiaomi_TV.ir"))
        val profile = TvRemoteProfile(
            brand = "Xiaomi",
            fileName = "Xiaomi_TV.ir",
            displayName = "Xiaomi TV",
            assetPath = "irdb/tv/Xiaomi/Xiaomi_TV.ir",
            commands = commands,
        )

        assertEquals("Open", profile.supportedActions[RemoteAction.POWER]?.name)
        assertEquals("More", profile.supportedActions[RemoteAction.MENU]?.name)
        assertTrue(profile.supportedActions.containsKey(RemoteAction.HOME))
    }

    private fun readAssetText(path: String): String {
        val appModuleFile = File("src/main/assets", path)
        val rootProjectFile = File("app/src/main/assets", path)
        return when {
            appModuleFile.isFile -> appModuleFile
            rootProjectFile.isFile -> rootProjectFile
            else -> error("Missing test asset: $path")
        }.readText()
    }
}
