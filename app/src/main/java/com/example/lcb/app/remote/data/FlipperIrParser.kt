package com.example.lcb.app.remote.data

import com.example.lcb.app.remote.model.IrCommand
import com.example.lcb.app.remote.model.IrSignal

class FlipperIrParser {
    fun parse(content: String): List<IrCommand> {
        val commands = mutableListOf<IrCommand>()
        val current = linkedMapOf<String, String>()

        fun flushCurrent() {
            if (current.isEmpty()) return
            buildCommand(current)?.let(commands::add)
            current.clear()
        }

        content.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            when {
                line.isEmpty() -> Unit
                line == "#" -> flushCurrent()
                line.startsWith("#") -> Unit
                ":" in line -> {
                    val key = line.substringBefore(":").trim().lowercase()
                    val value = line.substringAfter(":").trim()
                    if (key in commandKeys) {
                        current[key] = value
                    }
                }
            }
        }
        flushCurrent()

        return commands
    }

    private fun buildCommand(values: Map<String, String>): IrCommand? {
        val name = values["name"]?.takeIf { it.isNotBlank() } ?: return null
        val type = values["type"]?.lowercase() ?: return null

        val signal = when (type) {
            "parsed" -> {
                val protocol = values["protocol"]?.trim()?.takeIf { it.isNotBlank() } ?: return null
                val address = values["address"]?.toByteArrayFromHex() ?: return null
                val command = values["command"]?.toByteArrayFromHex() ?: return null
                IrSignal.Parsed(protocol = protocol, address = address, command = command)
            }
            "raw" -> {
                val frequency = values["frequency"]?.toIntOrNull() ?: return null
                val pattern = values["data"]
                    ?.split(Regex("\\s+"))
                    ?.mapNotNull { it.toIntOrNull() }
                    ?.toIntArray()
                    ?.takeIf { it.isNotEmpty() }
                    ?: return null
                IrSignal.Raw(
                    frequency = frequency,
                    dutyCycle = values["duty_cycle"]?.toDoubleOrNull(),
                    patternMicros = pattern,
                )
            }
            else -> return null
        }

        return IrCommand(name = name, signal = signal)
    }

    private fun String.toByteArrayFromHex(): ByteArray {
        return split(Regex("\\s+"))
            .mapNotNull { token -> token.toIntOrNull(16)?.toByte() }
            .toByteArray()
    }

    private companion object {
        val commandKeys = setOf(
            "name",
            "type",
            "protocol",
            "address",
            "command",
            "frequency",
            "duty_cycle",
            "data",
        )
    }
}
