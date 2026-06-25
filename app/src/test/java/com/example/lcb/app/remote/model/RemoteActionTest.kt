package com.example.lcb.app.remote.model

import org.junit.Assert.assertEquals
import org.junit.Test

class RemoteActionTest {
    @Test
    fun xiaomiOpenCommandMapsToPower() {
        assertEquals(RemoteAction.POWER, RemoteAction.fromIrName("Open"))
    }

    @Test
    fun slashSeparatedReturnCommandMapsToBack() {
        assertEquals(RemoteAction.BACK, RemoteAction.fromIrName("Return/back"))
    }

    @Test
    fun actionMenuAliasesMapToMenu() {
        assertEquals(RemoteAction.MENU, RemoteAction.fromIrName("Action_menu"))
        assertEquals(RemoteAction.MENU, RemoteAction.fromIrName("More"))
    }

    @Test
    fun scannedPowerAliasesMapToPower() {
        assertEquals(RemoteAction.POWER, RemoteAction.fromIrName("Power_off_(3b)"))
        assertEquals(RemoteAction.POWER, RemoteAction.fromIrName("TV_PWR"))
        assertEquals(RemoteAction.POWER, RemoteAction.fromIrName("Escalade_tv_power"))
    }

    @Test
    fun scannedVolumeAliasesMapToVolumeActions() {
        assertEquals(RemoteAction.VOLUME_UP, RemoteAction.fromIrName("VOL +"))
        assertEquals(RemoteAction.VOLUME_UP, RemoteAction.fromIrName("Right/vol+"))
        assertEquals(RemoteAction.VOLUME_UP, RemoteAction.fromIrName("VolUp"))
        assertEquals(RemoteAction.VOLUME_DOWN, RemoteAction.fromIrName("VolDown"))
    }

    @Test
    fun scannedChannelAliasesMapToChannelActions() {
        assertEquals(RemoteAction.CHANNEL_UP, RemoteAction.fromIrName("CH+"))
        assertEquals(RemoteAction.CHANNEL_UP, RemoteAction.fromIrName("Ch+_uparrow"))
        assertEquals(RemoteAction.CHANNEL_UP, RemoteAction.fromIrName("Chan_up"))
        assertEquals(RemoteAction.CHANNEL_DOWN, RemoteAction.fromIrName("Chan_down"))
        assertEquals(RemoteAction.CHANNEL_DOWN, RemoteAction.fromIrName("Ch_previous"))
    }

    @Test
    fun scannedNavigationAliasesMapToExistingActions() {
        assertEquals(RemoteAction.BACK, RemoteAction.fromIrName("Exit"))
        assertEquals(RemoteAction.BACK, RemoteAction.fromIrName("Back_return"))
        assertEquals(RemoteAction.MENU, RemoteAction.fromIrName("Q.menu"))
        assertEquals(RemoteAction.SETTINGS, RemoteAction.fromIrName("D.SETUP"))
        assertEquals(RemoteAction.INFO, RemoteAction.fromIrName("TV Guide"))
    }

    @Test
    fun scannedSourceAliasesMapToSource() {
        assertEquals(RemoteAction.SOURCE, RemoteAction.fromIrName("Sources"))
        assertEquals(RemoteAction.SOURCE, RemoteAction.fromIrName("Hdmi1"))
        assertEquals(RemoteAction.SOURCE, RemoteAction.fromIrName("USB"))
        assertEquals(RemoteAction.SOURCE, RemoteAction.fromIrName("Tv"))
    }

    @Test
    fun mediaTransportAliasesDoNotHijackBackButton() {
        assertEquals(null, RemoteAction.fromIrName("Fast_back"))
        assertEquals(null, RemoteAction.fromIrName("Skip_backward"))
    }
}
