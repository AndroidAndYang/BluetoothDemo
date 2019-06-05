package com.seabig.blelock.ui

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View

import com.seabig.blelock.R
import com.seabig.blelock.adapter.BleDeviceAdapter
import com.seabig.blelock.base.BaseActivity
import com.seabig.blelock.callback.IBleScanListener
import com.seabig.blelock.callback.IConnectListener
import com.seabig.blelock.util.RssiUtil
import com.seabig.blelock.util.helper.BleController
import kotlinx.android.synthetic.main.activity_ble.*

/**
 * @author: YJZ
 * @date: 2019/6/4 9:50
 * @Des: 低功耗蓝牙开发
 */

class BleActivity : BaseActivity() {

    private lateinit var mBleAdapter: BleDeviceAdapter
    private lateinit var mBleController: BleController

    override fun initLayoutId() = R.layout.activity_ble

    override fun onSettingUpView() {

        // TODO 申请位置权限，不然周围的蓝牙设备扫描不出

        swipe_refresh.setOnRefreshListener {
            mBleAdapter.clear()
            mBleController.startScan(true, bleScanListener)
        }
        swipe_refresh.isRefreshing = true

        initRecyclerView()

        mBleController = BleController.getInstance()

        val init = mBleController.init(this)
        if (!init) {
            showToast("init fail!")
            return
        }

        mBleController.startScan(true, bleScanListener)
    }

    private fun initRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.ble_device_list)
        mBleAdapter = BleDeviceAdapter(this)
        mBleAdapter.setOnRecyclerViewItemClickListen(object : BleDeviceAdapter.OnRecyclerViewItemClickListen{
            override fun onItemClickListen(view: View, position: Int) {
                mBleController.connect(mBleAdapter[position]!!.address, object : IConnectListener {
                    override fun onConnSuccess() {
                        swipe_refresh.isRefreshing = false
                        startActivity(Intent(this@BleActivity, CommandActivity::class.java))
                    }

                    override fun onConnFailed() {
                        showToast("connect fail!")
                    }
                })
            }
        })
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mBleAdapter
    }

    /**
     * 蓝牙扫描监听
     */
    private var bleScanListener: IBleScanListener = object : IBleScanListener {
        override fun onScanLe(device: BluetoothDevice, rssi: Int, scanRecord: ByteArray) {
            e("device = " + device.address)
            mBleAdapter.add(device, RssiUtil.getDistance(rssi))
        }

        override fun onScanSuccess() {
            swipe_refresh.isRefreshing = false
        }
    }

    private var lastBackKeyDownTick: Long = 0

    /**
     * 获取退出程序按下时间间隔(单位：毫秒)
     */
    private fun onGetExitAppPressMSecs(): Long {
        return 1500
    }

    /**
     * 返回键监听
     */
    override fun onBackPressed() {
        val currentTick = System.currentTimeMillis()

        if (currentTick - lastBackKeyDownTick > onGetExitAppPressMSecs()) {
            showToast("再按一次退出")
            lastBackKeyDownTick = currentTick
        } else {
            mBleController.closeBleConnect()
            finish()
            System.exit(0)
        }
    }
}
