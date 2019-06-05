package com.seabig.blelock.ui

import android.text.TextUtils
import android.view.View
import com.seabig.blelock.R
import com.seabig.blelock.base.BaseActivity
import com.seabig.blelock.callback.IReceiverListener
import com.seabig.blelock.callback.IWriteListener
import com.seabig.blelock.dtmgr.BLEStatus
import com.seabig.blelock.util.AESUtil
import com.seabig.blelock.util.HexUtil
import com.seabig.blelock.util.helper.BleController
import kotlinx.android.synthetic.main.activity_command.*
import java.lang.StringBuilder

/**
 *@author: YJZ
 *@date: 2019/6/4 17:24
 *@Des: 发送蓝牙命令
 */
class CommandActivity : BaseActivity(), View.OnClickListener {

    private val tag = this::class.java.simpleName

    lateinit var bleController: BleController

    private var token: String = ""

    override fun initLayoutId() = R.layout.activity_command

    override fun onSettingUpView() {

        get_life.setOnClickListener(this)
        get_token.setOnClickListener(this)
        open_lock.setOnClickListener(this)
        close_lock.setOnClickListener(this)

        onSettingUpData()
    }

    private fun onSettingUpData() {

        val stringBuilder = StringBuilder()

        bleController = BleController.getInstance()

        // 注册蓝牙返回数据的监听
        bleController.registerReceiverListener(tag, object : IReceiverListener {
            override fun onReceiver(value: ByteArray) {
                val decrypt = AESUtil.defaultDecrypt(value)
                if (decrypt == null || decrypt.isEmpty()) {
                    e("decrypt is null")
                    return
                }
                val decryptStr = HexUtil.bytesToHexString(decrypt)
                e("decryptStr = $decryptStr")
                when {
                    // 获取token并开锁
                    decryptStr.startsWith(BLEStatus.GET_TOKEN_INDEX) -> {
                        stringBuilder.append("token : $decryptStr \n")
                        token = decryptStr.substring(6, 6 + 8)
                        showToast("获取Token成功")
                    }

                    // 开锁
                    decryptStr.startsWith(BLEStatus.GET_OPEN_INDEX) -> {
                        stringBuilder.append("开锁 : $decryptStr \n")
                        showToast("开锁成功")
                    }

                    // 获取电量
                    decryptStr.startsWith(BLEStatus.GET_LIFE_INDEX) -> {

                    }

                    // 关锁
                    decryptStr.startsWith(BLEStatus.GET_CLOSE_INDEX) -> {
                        stringBuilder.append("关锁 : $decryptStr \n")
                        showToast("关锁成功")
                    }
                }
                receiver_content_tv.text = stringBuilder.toString()
            }
        })
    }

    override fun onClick(v: View) {
        when (v.id) {
            // 获取token
            R.id.get_token -> {
                val command = ed_command.text.toString()
                if (TextUtils.isEmpty(command)) {
                    showToast("内容不能为空")
                    return
                }
                write(command)
            }
            // 开锁
            R.id.open_lock -> {
                if (TextUtils.isEmpty(token)) {
                    showToast("token is null")
                    return
                }
                val openLockCommand = "050106" + "303030303030" + token + "000000"
                write(openLockCommand)
            }
            // 关锁
            R.id.close_lock -> {
                if (TextUtils.isEmpty(token)) {
                    showToast("token is null")
                    return
                }
                val closeLockCommand = "050c0101" + token + "0000000000000000"
                write(closeLockCommand)
            }
            // 获取电量
            R.id.get_life -> {
                if (TextUtils.isEmpty(token)) {
                    showToast("token is null")
                    return
                }
            }
        }
    }

    private fun write(command: String) {
        bleController.write(command, object : IWriteListener {
            override fun onSuccess() {
                showToast("发送成功！")
            }

            override fun onFail(state: Int) {
                showToast("发送失败：status = $state")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        bleController.unRegisterReceiverListener("command")
    }
}