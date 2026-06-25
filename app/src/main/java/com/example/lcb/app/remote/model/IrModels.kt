package com.example.lcb.app.remote.model

data class TvBrand(
    val name: String,
    val assetPath: String,
    val modelCount: Int,
)

data class TvRemoteProfile(
    val brand: String,
    val fileName: String,
    val displayName: String,
    val assetPath: String,
    val commands: List<IrCommand>,
) {
    val supportedActions: Map<RemoteAction, IrCommand> = commands
        .mapNotNull { command -> RemoteAction.fromIrName(command.name)?.let { it to command } }
        .distinctBy { it.first }
        .toMap()
}

data class SavedTv(
    val id: String,
    val displayName: String,
    val scene: String,
    val brand: String,
    val modelName: String,
    val assetPath: String,
)

data class IrCommand(
    val name: String,
    val signal: IrSignal,
)

sealed class IrSignal {
    data class Parsed(
        val protocol: String,
        val address: ByteArray,
        val command: ByteArray,
    ) : IrSignal()

    data class Raw(
        val frequency: Int,
        val dutyCycle: Double?,
        val patternMicros: IntArray,
    ) : IrSignal()
}

enum class RemoteAction(val label: String) {
    POWER("Power"),
    UP("Up"),
    DOWN("Down"),
    LEFT("Left"),
    RIGHT("Right"),
    OK("OK"),
    VOLUME_UP("VOL +"),
    VOLUME_DOWN("VOL -"),
    CHANNEL_UP("CH +"),
    CHANNEL_DOWN("CH -"),
    MUTE("Mute"),
    SOURCE("Source"),
    HOME("Home"),
    BACK("Back"),
    MENU("Menu"),
    SETTINGS("Settings"),
    INFO("Info");

    companion object {
        fun fromIrName(name: String): RemoteAction? {
            val normalized = name
                .trim()
                .lowercase()
                .replace("-", "_")
                .replace(" ", "_")

            return when (normalized) {
                "power", "power_toggle", "pwr" -> POWER
                "up", "arrow_up" -> UP
                "down", "arrow_down" -> DOWN
                "left", "arrow_left" -> LEFT
                "right", "arrow_right" -> RIGHT
                "ok", "select", "enter", "confirm", "centre", "center" -> OK
                "vol_up", "volume_up", "vol+", "volume+" -> VOLUME_UP
                "vol_dn", "vol_down", "volume_down", "vol-", "volume-" -> VOLUME_DOWN
                "ch_next", "ch_up", "channel_up", "prog_up", "p+" -> CHANNEL_UP
                "ch_prev", "ch_down", "channel_down", "prog_down", "p-" -> CHANNEL_DOWN
                "mute" -> MUTE
                "source", "input", "tv_input", "av" -> SOURCE
                "home", "smarthub", "smart_hub", "apps" -> HOME
                "back", "return", "ret" -> BACK
                "menu" -> MENU
                "settings", "setup", "tools", "options", "option" -> SETTINGS
                "info", "display" -> INFO
                else -> null
            }
        }
    }
}
