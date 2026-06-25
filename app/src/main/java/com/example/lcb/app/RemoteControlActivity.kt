package com.example.lcb.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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
import com.example.lcb.app.remote.ui.RemoteHapticFeedback
import com.example.lcb.app.remote.ui.applySystemBarInsets

class RemoteControlActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRemoteControlBinding
    private lateinit var profile: TvRemoteProfile
    private lateinit var transmitter: AndroidIrTransmitter
    private lateinit var hapticFeedback: RemoteHapticFeedback

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
        hapticFeedback = RemoteHapticFeedback(this)
        bindHeader(savedTv)
        bindInfraredCapabilityTip()
        bindButtons()
    }

    private fun bindHeader(savedTv: SavedTv) {
        binding.toolbar.title = savedTv.displayName
        binding.toolbar.subtitle = savedTv.modelName
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_delete_device) {
                confirmDelete(savedTv)
                true
            } else {
                false
            }
        }
    }

    private fun bindInfraredCapabilityTip() {
        binding.noIrTip.visibility = if (isInfraredSupported()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun isInfraredSupported(): Boolean = transmitter.hasEmitter()

    private fun confirmDelete(savedTv: SavedTv) {
        AlertDialog.Builder(this)
            .setTitle("删除设备")
            .setMessage("确定要删除「${savedTv.displayName}」吗？")
            .setNegativeButton("取消", null)
            .setPositiveButton("删除") { _, _ ->
                SharedPreferencesSavedTvRepository(this).delete(savedTv.id)
                Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show()
                finish()
            }
            .show()
    }

    private fun bindButtons() {
        binding.powerButton.bindAction(RemoteAction.POWER)
        binding.upButton.bindAction(RemoteAction.UP)
        binding.downButton.bindAction(RemoteAction.DOWN)
        binding.leftButton.bindAction(RemoteAction.LEFT)
        binding.rightButton.bindAction(RemoteAction.RIGHT)
        binding.okButton.bindAction(RemoteAction.OK)

        val volume = ViewRemoteVerticalControlBinding.bind(binding.volumeControl.root)
        volume.labelText.text = "VOL"
        volume.plusButton.bindAction(RemoteAction.VOLUME_UP)
        volume.minusButton.bindAction(RemoteAction.VOLUME_DOWN)

        val channel = ViewRemoteVerticalControlBinding.bind(binding.channelControl.root)
        channel.labelText.text = "CH"
        channel.plusButton.bindAction(RemoteAction.CHANNEL_UP)
        channel.minusButton.bindAction(RemoteAction.CHANNEL_DOWN)

        binding.muteButton.bindAction(RemoteAction.MUTE)
        binding.sourceButton.bindAction(RemoteAction.SOURCE)
        binding.homeButton.bindAction(RemoteAction.HOME)
        binding.backRemoteButton.bindAction(RemoteAction.BACK)
        binding.menuButton.bindAction(RemoteAction.MENU)
        binding.settingsButton.bindAction(RemoteAction.SETTINGS)
    }

    private fun com.google.android.material.button.MaterialButton.bindAction(action: RemoteAction) {
        val supported = profile.supportedActions.containsKey(action)
        isEnabled = supported
        alpha = if (supported) 1f else 0.4f
        setOnClickListener { send(action) }
    }

    private fun send(action: RemoteAction) {
        val command = profile.supportedActions[action] ?: return
        hapticFeedback.performKeyPress()
        transmitter.send(command).message()?.let { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun IrSendResult.message(): String? {
        return when (this) {
            IrSendResult.Sent -> null
            IrSendResult.NoEmitter -> "当前设备没有红外发射器"
            IrSendResult.MissingPermission -> "缺少红外发送权限 TRANSMIT_IR"
            is IrSendResult.Unsupported -> null
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
