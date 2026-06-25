package com.example.lcb.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.lcb.app.databinding.ActivityRemoteControlBinding
import com.example.lcb.app.databinding.ViewRemoteVerticalControlBinding
import com.example.lcb.app.remote.data.AssetTvRemoteRepository
import com.example.lcb.app.remote.data.SharedPreferencesSavedTvRepository
import com.example.lcb.app.remote.infrared.AndroidIrTransmitter
import com.example.lcb.app.remote.infrared.IrSendResult
import com.example.lcb.app.remote.model.RemoteAction
import com.example.lcb.app.remote.model.SavedTv
import com.example.lcb.app.remote.model.TvRemoteProfile
import com.example.lcb.app.remote.ui.applySystemBarInsets

class RemoteControlActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRemoteControlBinding
    private lateinit var profile: TvRemoteProfile
    private lateinit var transmitter: AndroidIrTransmitter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRemoteControlBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarInsets(binding.main)

        val savedTv = loadSavedTv()
        if (savedTv == null) {
            Toast.makeText(this, "未找到已添加电视", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val remoteRepository = AssetTvRemoteRepository(this)
        val loadedProfile = remoteRepository.getProfile(savedTv.assetPath)
        if (loadedProfile == null) {
            Toast.makeText(this, "遥控码文件读取失败", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        profile = loadedProfile
        transmitter = AndroidIrTransmitter(this)
        bindHeader(savedTv)
        bindButtons()
    }

    private fun bindHeader(savedTv: SavedTv) {
        binding.titleText.text = savedTv.displayName
        binding.subtitleText.text = savedTv.modelName
        binding.irStatusText.text = if (transmitter.hasEmitter()) "IR Ready" else "当前设备未检测到红外发射器"
        binding.backButton.setOnClickListener { finish() }
    }

    private fun bindButtons() {
        binding.powerButton.setOnClickListener { send(RemoteAction.POWER) }
        binding.upButton.bindAction(RemoteAction.UP)
        binding.downButton.bindAction(RemoteAction.DOWN)
        binding.leftButton.bindAction(RemoteAction.LEFT)
        binding.rightButton.bindAction(RemoteAction.RIGHT)
        binding.okButton.bindAction(RemoteAction.OK)

        val volume = ViewRemoteVerticalControlBinding.bind(binding.volumeControl.root)
        volume.labelText.text = "VOL"
        volume.plusButton.setOnClickListener { send(RemoteAction.VOLUME_UP) }
        volume.minusButton.setOnClickListener { send(RemoteAction.VOLUME_DOWN) }

        val channel = ViewRemoteVerticalControlBinding.bind(binding.channelControl.root)
        channel.labelText.text = "CH"
        channel.plusButton.setOnClickListener { send(RemoteAction.CHANNEL_UP) }
        channel.minusButton.setOnClickListener { send(RemoteAction.CHANNEL_DOWN) }

        binding.muteButton.bindAction(RemoteAction.MUTE)
        binding.sourceButton.bindAction(RemoteAction.SOURCE)
        binding.homeButton.bindAction(RemoteAction.HOME)
        binding.backRemoteButton.bindAction(RemoteAction.BACK)
        binding.menuButton.bindAction(RemoteAction.MENU)
        binding.settingsButton.bindAction(RemoteAction.SETTINGS)
    }

    private fun com.google.android.material.button.MaterialButton.bindAction(action: RemoteAction) {
        isEnabled = profile.supportedActions.containsKey(action)
        alpha = if (isEnabled) 1f else 0.36f
        setOnClickListener { send(action) }
    }

    private fun send(action: RemoteAction) {
        val command = profile.supportedActions[action]
        val result = if (command == null) {
            IrSendResult.Unsupported("当前遥控码没有 ${action.label} 按键")
        } else {
            transmitter.send(command)
        }
        result.message()?.let { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun IrSendResult.message(): String? {
        return when (this) {
            IrSendResult.Sent -> null
            IrSendResult.NoEmitter -> "当前设备没有红外发射器"
            IrSendResult.MissingPermission -> "缺少红外发送权限 TRANSMIT_IR"
            is IrSendResult.Unsupported -> reason
            is IrSendResult.Failed -> "发送失败: ${error.message.orEmpty()}"
        }
    }

    private fun loadSavedTv(): SavedTv? {
        val id = intent.getStringExtra(EXTRA_SAVED_TV_ID) ?: return null
        return SharedPreferencesSavedTvRepository(this).getSavedTvs().firstOrNull { it.id == id }
    }

    companion object {
        private const val EXTRA_SAVED_TV_ID = "saved_tv_id"

        fun createIntent(context: Context, savedTvId: String): Intent {
            return Intent(context, RemoteControlActivity::class.java)
                .putExtra(EXTRA_SAVED_TV_ID, savedTvId)
        }
    }
}
