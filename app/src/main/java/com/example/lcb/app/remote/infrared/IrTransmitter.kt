package com.example.lcb.app.remote.infrared

import android.content.Context
import android.hardware.ConsumerIrManager
import com.example.lcb.app.remote.model.IrCommand
import com.example.lcb.app.remote.model.IrSignal

data class EncodedIrSignal(
    val frequency: Int,
    val patternMicros: IntArray,
)

sealed class IrSendResult {
    data object Sent : IrSendResult()
    data object NoEmitter : IrSendResult()
    data object MissingPermission : IrSendResult()
    data class Unsupported(val reason: String) : IrSendResult()
    data class Failed(val error: Throwable) : IrSendResult()
}

interface IrTransmitter {
    fun hasEmitter(): Boolean
    fun send(command: IrCommand): IrSendResult
}

class AndroidIrTransmitter(
    context: Context,
    private val encoder: FlipperIrEncoder = FlipperIrEncoder(),
) : IrTransmitter {
    private val manager = context.applicationContext
        .getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager

    override fun hasEmitter(): Boolean = manager?.hasIrEmitter() == true

    override fun send(command: IrCommand): IrSendResult {
        val irManager = manager ?: return IrSendResult.NoEmitter
        if (!irManager.hasIrEmitter()) return IrSendResult.NoEmitter

        val encoded = when (val signal = command.signal) {
            is IrSignal.Raw -> EncodedIrSignal(signal.frequency, signal.patternMicros)
            is IrSignal.Parsed -> encoder.encode(signal).getOrElse {
                return IrSendResult.Unsupported(it.message ?: "Unsupported IR protocol")
            }
        }

        return runCatching {
            irManager.transmit(encoded.frequency, encoded.patternMicros)
        }.fold(
            onSuccess = { IrSendResult.Sent },
            onFailure = {
                if (it is SecurityException) {
                    IrSendResult.MissingPermission
                } else {
                    IrSendResult.Failed(it)
                }
            },
        )
    }
}
