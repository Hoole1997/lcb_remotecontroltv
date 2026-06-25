package com.example.lcb.app.remote.infrared

import com.example.lcb.app.remote.model.IrSignal

class FlipperIrEncoder {
    fun encode(signal: IrSignal.Parsed): Result<EncodedIrSignal> {
        val protocol = signal.protocol.trim().removeSuffix("\r")
        val address = signal.address.littleEndianInt()
        val command = signal.command.littleEndianInt()

        return runCatching {
            when (protocol) {
                "NEC" -> encodeNec(address, command)
                "NECext" -> encodeNecExtended(address, command)
                "Samsung32" -> encodeSamsung32(address, command)
                "SIRC" -> encodeSirc(address, command, 12)
                "SIRC15" -> encodeSirc(address, command, 15)
                "SIRC20" -> encodeSirc(address, command, 20)
                "Kaseikyo" -> encodeKaseikyo(address, command)
                "RCA" -> encodeRca(address, command)
                "Pioneer" -> encodePioneer(address, command)
                "RC5" -> encodeRc5(address, command, extended = false)
                "RC5X" -> encodeRc5(address, command, extended = true)
                "RC6" -> encodeRc6(address, command)
                else -> error("Unsupported IR protocol: $protocol")
            }
        }
    }

    private fun encodeNec(address: Int, command: Int): EncodedIrSignal {
        val data = (address and 0xFF) or
            (((address and 0xFF).inv() and 0xFF) shl 8) or
            ((command and 0xFF) shl 16) or
            (((command and 0xFF).inv() and 0xFF) shl 24)
        return pdwm(
            frequency = 38_000,
            data = data,
            bits = 32,
            preambleMark = 9_000,
            preambleSpace = 4_500,
            bit0Mark = 560,
            bit0Space = 560,
            bit1Mark = 560,
            bit1Space = 1_690,
            addStopMark = true,
        )
    }

    private fun encodeNecExtended(address: Int, command: Int): EncodedIrSignal {
        val data = (address and 0xFFFF) or ((command and 0xFFFF) shl 16)
        return pdwm(
            frequency = 38_000,
            data = data,
            bits = 32,
            preambleMark = 9_000,
            preambleSpace = 4_500,
            bit0Mark = 560,
            bit0Space = 560,
            bit1Mark = 560,
            bit1Space = 1_690,
            addStopMark = true,
        )
    }

    private fun encodeSamsung32(address: Int, command: Int): EncodedIrSignal {
        val data = (address and 0xFF) or
            ((address and 0xFF) shl 8) or
            ((command and 0xFF) shl 16) or
            (((command and 0xFF).inv() and 0xFF) shl 24)
        return pdwm(
            frequency = 38_000,
            data = data,
            bits = 32,
            preambleMark = 4_500,
            preambleSpace = 4_500,
            bit0Mark = 550,
            bit0Space = 550,
            bit1Mark = 550,
            bit1Space = 1_650,
            addStopMark = true,
        )
    }

    private fun encodeSirc(address: Int, command: Int, bits: Int): EncodedIrSignal {
        val data = when (bits) {
            12 -> (command and 0x7F) or ((address and 0x1F) shl 7)
            15 -> (command and 0x7F) or ((address and 0xFF) shl 7)
            20 -> (command and 0x7F) or ((address and 0x1FFF) shl 7)
            else -> error("Unsupported SIRC bit length")
        }
        val frame = pdwm(
            frequency = 40_000,
            data = data,
            bits = bits,
            preambleMark = 2_400,
            preambleSpace = 600,
            bit0Mark = 600,
            bit0Space = 600,
            bit1Mark = 1_200,
            bit1Space = 600,
            addStopMark = false,
        ).patternMicros

        // Sony remotes conventionally repeat a frame; sending three improves reliability.
        val repeated = frame.toMutableList()
        repeat(2) {
            val elapsed = frame.sum()
            repeated.add((45_000 - elapsed).coerceAtLeast(10_000))
            repeated.addAll(frame.toList())
        }
        return EncodedIrSignal(40_000, repeated.toIntArray())
    }

    private fun encodeKaseikyo(address: Int, command: Int): EncodedIrSignal {
        val id = (address ushr 24) and 0x03
        val vendorId = (address ushr 8) and 0xFFFF
        val genre1 = (address ushr 4) and 0x0F
        val genre2 = address and 0x0F
        val byte0 = vendorId and 0xFF
        val byte1 = (vendorId ushr 8) and 0xFF
        val vendorParity = ((byte0 xor byte1) and 0x0F) xor ((byte0 xor byte1) ushr 4)
        val dataBytes = intArrayOf(
            byte0,
            byte1,
            (vendorParity and 0x0F) or (genre1 shl 4),
            (genre2 and 0x0F) or ((command and 0x0F) shl 4),
            (id shl 6) or ((command ushr 4) and 0x3F),
            0,
        )
        dataBytes[5] = dataBytes[2] xor dataBytes[3] xor dataBytes[4]
        return pdwmBytes(
            frequency = 38_000,
            bytes = dataBytes,
            bits = 48,
            preambleMark = 3_456,
            preambleSpace = 1_728,
            bit0Mark = 432,
            bit0Space = 432,
            bit1Mark = 432,
            bit1Space = 1_296,
            addStopMark = true,
        )
    }

    private fun encodeRca(address: Int, command: Int): EncodedIrSignal {
        val data = (address and 0x0F) or
            ((command and 0xFF) shl 4) or
            (((address and 0x0F).inv() and 0x0F) shl 12) or
            (((command and 0xFF).inv() and 0xFF) shl 16)
        return pdwm(
            frequency = 38_000,
            data = data,
            bits = 24,
            preambleMark = 4_000,
            preambleSpace = 4_000,
            bit0Mark = 500,
            bit0Space = 1_000,
            bit1Mark = 500,
            bit1Space = 2_000,
            addStopMark = true,
        )
    }

    private fun encodePioneer(address: Int, command: Int): EncodedIrSignal {
        val bytes = intArrayOf(
            address and 0xFF,
            (address and 0xFF).inv() and 0xFF,
            command and 0xFF,
            (command and 0xFF).inv() and 0xFF,
            0,
        )
        return pdwmBytes(
            frequency = 40_000,
            bytes = bytes,
            bits = 33,
            preambleMark = 8_500,
            preambleSpace = 4_225,
            bit0Mark = 500,
            bit0Space = 500,
            bit1Mark = 500,
            bit1Space = 1_500,
            addStopMark = false,
        )
    }

    private fun encodeRc5(address: Int, command: Int, extended: Boolean): EncodedIrSignal {
        val bits = mutableListOf<Boolean>()
        bits.add(true)
        bits.add(!extended)
        bits.add(false)
        appendMsb(bits, address and 0x1F, 5)
        appendMsb(bits, command and 0x3F, 6)
        return manchester(
            frequency = 36_000,
            preambleMark = 0,
            preambleSpace = 0,
            halfBit = 888,
            bits = bits,
            invert = true,
        )
    }

    private fun encodeRc6(address: Int, command: Int): EncodedIrSignal {
        val bits = mutableListOf<Boolean>()
        bits.add(true)
        appendMsb(bits, 0, 3)
        bits.add(false)
        appendMsb(bits, address and 0xFF, 8)
        appendMsb(bits, command and 0xFF, 8)
        return manchester(
            frequency = 36_000,
            preambleMark = 2_666,
            preambleSpace = 889,
            halfBit = 444,
            bits = bits,
            invert = false,
            doubleBitIndex = 4,
        )
    }

    private fun pdwm(
        frequency: Int,
        data: Int,
        bits: Int,
        preambleMark: Int,
        preambleSpace: Int,
        bit0Mark: Int,
        bit0Space: Int,
        bit1Mark: Int,
        bit1Space: Int,
        addStopMark: Boolean,
    ): EncodedIrSignal {
        val pattern = mutableListOf<Int>()
        pattern.add(preambleMark)
        pattern.add(preambleSpace)
        repeat(bits) { index ->
            val one = ((data ushr index) and 1) == 1
            pattern.add(if (one) bit1Mark else bit0Mark)
            pattern.add(if (one) bit1Space else bit0Space)
        }
        if (addStopMark) pattern.add(bit0Mark)
        return EncodedIrSignal(frequency, pattern.toIntArray())
    }

    private fun pdwmBytes(
        frequency: Int,
        bytes: IntArray,
        bits: Int,
        preambleMark: Int,
        preambleSpace: Int,
        bit0Mark: Int,
        bit0Space: Int,
        bit1Mark: Int,
        bit1Space: Int,
        addStopMark: Boolean,
    ): EncodedIrSignal {
        val pattern = mutableListOf<Int>()
        pattern.add(preambleMark)
        pattern.add(preambleSpace)
        repeat(bits) { index ->
            val one = ((bytes[index / 8] ushr (index % 8)) and 1) == 1
            pattern.add(if (one) bit1Mark else bit0Mark)
            pattern.add(if (one) bit1Space else bit0Space)
        }
        if (addStopMark) pattern.add(bit0Mark)
        return EncodedIrSignal(frequency, pattern.toIntArray())
    }

    private fun manchester(
        frequency: Int,
        preambleMark: Int,
        preambleSpace: Int,
        halfBit: Int,
        bits: List<Boolean>,
        invert: Boolean,
        doubleBitIndex: Int? = null,
    ): EncodedIrSignal {
        val pattern = mutableListOf<Int>()
        if (preambleMark > 0) {
            pattern.add(preambleMark)
            pattern.add(preambleSpace)
        }

        bits.forEachIndexed { index, bit ->
            val logical = if (invert) !bit else bit
            val duration = if (doubleBitIndex == index) halfBit * 2 else halfBit
            val firstMark = !logical
            appendHalf(pattern, firstMark, duration)
            appendHalf(pattern, !firstMark, duration)
        }

        return EncodedIrSignal(frequency, pattern.mergeAdjacent().toIntArray())
    }

    private fun appendHalf(pattern: MutableList<Int>, mark: Boolean, duration: Int) {
        val shouldBeMarkIndex = pattern.size % 2 == 0
        if (mark == shouldBeMarkIndex) {
            pattern.add(duration)
        } else if (pattern.isNotEmpty()) {
            pattern[pattern.lastIndex] += duration
        } else {
            pattern.add(duration)
        }
    }

    private fun MutableList<Int>.mergeAdjacent(): MutableList<Int> {
        return filter { it > 0 }.toMutableList()
    }

    private fun appendMsb(bits: MutableList<Boolean>, value: Int, count: Int) {
        for (index in count - 1 downTo 0) {
            bits.add(((value ushr index) and 1) == 1)
        }
    }

    private fun ByteArray.littleEndianInt(): Int {
        var value = 0
        forEachIndexed { index, byte ->
            value = value or ((byte.toInt() and 0xFF) shl (index * 8))
        }
        return value
    }
}
