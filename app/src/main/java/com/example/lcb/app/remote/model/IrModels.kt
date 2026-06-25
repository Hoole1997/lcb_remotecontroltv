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
        .mapNotNull { command -> RemoteAction.matchIrName(command.name)?.let { it to command } }
        .sortedByDescending { it.first.priority }
        .distinctBy { it.first.action }
        .associate { it.first.action to it.second }
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

enum class RemoteAction {
    POWER,
    UP,
    DOWN,
    LEFT,
    RIGHT,
    OK,
    VOLUME_UP,
    VOLUME_DOWN,
    CHANNEL_UP,
    CHANNEL_DOWN,
    MUTE,
    SOURCE,
    HOME,
    BACK,
    MENU,
    SETTINGS,
    INFO;

    companion object {
        fun fromIrName(name: String): RemoteAction? {
            return matchIrName(name)?.action
        }

        fun matchIrName(name: String): RemoteActionMatch? {
            val normalized = NormalizedIrName(name)

            // Match compound aliases before directional keys. Examples:
            // `Right/vol+` should control volume, and `Left/back` should go back.
            return when {
                normalized.isPower() -> RemoteActionMatch(POWER, normalized.priorityForPower())
                normalized.isVolumeUp() -> RemoteActionMatch(VOLUME_UP, normalized.priorityForCompound())
                normalized.isVolumeDown() -> RemoteActionMatch(VOLUME_DOWN, normalized.priorityForCompound())
                normalized.isChannelUp() -> RemoteActionMatch(CHANNEL_UP, normalized.priorityForCompound())
                normalized.isChannelDown() -> RemoteActionMatch(CHANNEL_DOWN, normalized.priorityForCompound())
                normalized.isMute() -> RemoteActionMatch(MUTE)
                normalized.isOk() -> RemoteActionMatch(OK, normalized.priorityForCompound())
                normalized.isSource() -> RemoteActionMatch(SOURCE, normalized.priorityForCompound())
                normalized.isHome() -> RemoteActionMatch(HOME, normalized.priorityForCompound())
                normalized.isBack() -> RemoteActionMatch(BACK, normalized.priorityForCompound())
                normalized.isMenu() -> RemoteActionMatch(MENU, normalized.priorityForCompound())
                normalized.isSettings() -> RemoteActionMatch(SETTINGS, normalized.priorityForCompound())
                normalized.isInfo() -> RemoteActionMatch(INFO, normalized.priorityForCompound())
                normalized.isUp() -> RemoteActionMatch(UP)
                normalized.isDown() -> RemoteActionMatch(DOWN)
                normalized.isLeft() -> RemoteActionMatch(LEFT)
                normalized.isRight() -> RemoteActionMatch(RIGHT)
                else -> null
            }
        }
    }
}

data class RemoteActionMatch(
    val action: RemoteAction,
    val priority: Int = 100,
)

private class NormalizedIrName(rawName: String) {
    private val raw = rawName.trim().lowercase()
    private val words = raw
        .replace("+", " plus ")
        .replace("-", " minus ")
        .replace(Regex("[^a-z0-9]+"), " ")
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
    private val compact = raw.filter { it.isLetterOrDigit() || it == '+' || it == '-' }
    private val compactWithoutLeadingDigits = compact.dropWhile { it.isDigit() }

    private val keys = setOf(compact, compactWithoutLeadingDigits)

    fun priorityForPower(): Int = if (keys.any { it in primaryPowerKeys }) 120 else 70

    fun priorityForCompound(): Int = if (words.size <= 2) 100 else 80

    fun isPower(): Boolean {
        return keys.any { key ->
            key in primaryPowerKeys ||
                key in secondaryPowerKeys ||
                key.startsWith("poweron") ||
                key.startsWith("poweroff") ||
                key.endsWith("power") ||
                key.endsWith("pwr")
        } || words.any { it == "power" || it == "pwr" }
    }

    fun isVolumeUp(): Boolean {
        return keys.any { key ->
            key in volumeUpKeys ||
                key.contains("vol+") ||
                key.contains("volume+") ||
                key.contains("volup") ||
                key.contains("volumeup") ||
                key.contains("voll+")
        } || hasTokenSequence("vol", "plus") || hasTokenSequence("volume", "plus")
    }

    fun isVolumeDown(): Boolean {
        return keys.any { key ->
            key in volumeDownKeys ||
                key.contains("vol-") ||
                key.contains("volumedown") ||
                key.contains("voldown") ||
                key.contains("voldn") ||
                key.contains("volume-")
        } || hasTokenSequence("vol", "minus") || hasTokenSequence("volume", "minus")
    }

    fun isChannelUp(): Boolean {
        return keys.any { key ->
            key in channelUpKeys ||
                key.startsWith("ch+") ||
                key.startsWith("channel+") ||
                key.startsWith("p+") ||
                key.contains("chanup") ||
                key.contains("channelup") ||
                key.contains("chnup") ||
                key.contains("chnlnext") ||
                key.contains("chup")
        } || hasTokenSequence("ch", "plus") || hasTokenSequence("channel", "plus") ||
            hasTokenSequence("prog", "plus")
    }

    fun isChannelDown(): Boolean {
        return keys.any { key ->
            key in channelDownKeys ||
                key.startsWith("ch-") ||
                key.startsWith("channel-") ||
                key.startsWith("p-") ||
                key.contains("chandown") ||
                key.contains("channeldown") ||
                key.contains("chndown") ||
                key.contains("chnlprev") ||
                key.contains("chdown") ||
                key.contains("chprevious")
        } || hasTokenSequence("ch", "minus") || hasTokenSequence("channel", "minus") ||
            hasTokenSequence("prog", "minus")
    }

    fun isMute(): Boolean = keys.any { it == "mute" || it.endsWith("mute") }

    fun isOk(): Boolean {
        return keys.any { key ->
            key in okKeys ||
                key.endsWith("ok") ||
                key.endsWith("enter") ||
                key.endsWith("select") ||
                key.contains("enterok") ||
                key.contains("okenter") ||
                key.contains("entermiddle")
        } || words.any { it in okWordKeys }
    }

    fun isSource(): Boolean {
        return keys.any { key ->
            key in sourceKeys ||
                key.matches(Regex("hdmi[0-9]?")) ||
                key.matches(Regex("hdmi[0-9]")) ||
                key.startsWith("hdmi") && key.length <= 6
        } || words.any { it in sourceWordKeys }
    }

    fun isHome(): Boolean {
        return keys.any { key ->
            key in homeKeys ||
                key.endsWith("home") ||
                key.contains("smarthome") ||
                key.contains("smarttv") ||
                key.contains("myapps")
        }
    }

    fun isBack(): Boolean {
        val isMediaBack = words.any { it in mediaBackWords }
        return keys.any { key ->
            key in backKeys ||
                key.endsWith("back") && !isMediaBack ||
                key.endsWith("return") ||
                key.endsWith("exit") ||
                key.endsWith("cancel")
        } || words.any { it in backWordKeys } && !isMediaBack
    }

    fun isMenu(): Boolean {
        return keys.any { key ->
            key in menuKeys ||
                key.endsWith("menu") ||
                key.contains("qmenu") ||
                key.contains("syncmenu")
        } || words.any { it == "menu" || it == "more" }
    }

    fun isSettings(): Boolean {
        return keys.any { key ->
            key in settingsKeys ||
                key.endsWith("setup") ||
                key.endsWith("settings") ||
                key.endsWith("options") ||
                key.endsWith("tools")
        } || words.any { it in settingsWordKeys }
    }

    fun isInfo(): Boolean {
        return keys.any { key ->
            key in infoKeys ||
                key.endsWith("info") ||
                key.endsWith("guide") ||
                key.endsWith("help")
        } || words.any { it in infoWordKeys }
    }

    fun isUp(): Boolean = keys.any { it in upKeys }

    fun isDown(): Boolean = keys.any { it in downKeys }

    fun isLeft(): Boolean = keys.any { it in leftKeys }

    fun isRight(): Boolean = keys.any { it in rightKeys }

    private fun hasTokenSequence(first: String, second: String): Boolean {
        return words.windowed(2).any { it[0] == first && it[1] == second }
    }

    private companion object {
        val primaryPowerKeys = setOf("power", "powertoggle", "pwr", "open", "standby", "onoff")
        val secondaryPowerKeys = setOf("poweron", "poweroff", "tvpower", "tvpwr", "tvonoff", "lgonoff")
        val volumeUpKeys = setOf("vol+", "volume+", "volup", "volumeup", "voll+", "0volup")
        val volumeDownKeys = setOf("vol-", "volume-", "voldn", "voldown", "volumedown", "0voldn")
        val channelUpKeys = setOf("ch+", "channel+", "p+", "chnext", "chup", "chanup", "chnup", "prog+")
        val channelDownKeys = setOf("ch-", "channel-", "p-", "chprev", "chdown", "chandown", "chndown", "prog-")
        val okKeys = setOf("ok", "select", "enter", "confirm", "centre", "center", "ent")
        val okWordKeys = setOf("ok", "select", "enter", "confirm", "centre", "center", "ent")
        val sourceKeys = setOf("source", "sources", "input", "tvinput", "av", "tv", "dtv", "atv", "usb", "video")
        val sourceWordKeys = setOf("source", "sources", "input", "av", "dtv", "atv", "usb", "component", "antenna")
        val homeKeys = setOf("home", "smarthub", "apps", "app", "smart", "smarttv", "recentapps")
        val backKeys = setOf("back", "return", "returnback", "backreturn", "ret", "exit", "cancel")
        val backWordKeys = setOf("back", "return", "ret", "exit", "cancel")
        val mediaBackWords = setOf("skip", "seek", "fast", "rewind", "backward", "backwards", "backwd")
        val menuKeys = setOf("menu", "actionmenu", "more", "qmenu", "syncmenu", "fnmenu", "livemenu")
        val settingsKeys = setOf("settings", "setup", "tools", "options", "option", "quickmenu")
        val settingsWordKeys = setOf("settings", "setup", "tools", "options", "option")
        val infoKeys = setOf("info", "display", "guide", "help", "information", "tvguide", "ehelp")
        val infoWordKeys = setOf("info", "display", "guide", "help", "information", "ehelp")
        val upKeys = setOf("up", "arrowup", "uparrow")
        val downKeys = setOf("down", "arrowdown", "downarrow")
        val leftKeys = setOf("left", "arrowleft", "leftarrow")
        val rightKeys = setOf("right", "arrowright", "rightarrow")
    }
}
