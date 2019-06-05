package com.seabig.blelock.callback;

import android.bluetooth.BluetoothDevice;

/**
 * @author: YJZ
 * @date: 2019/6/4 16:00
 * @Des: 蓝牙扫描监听
 **/
public interface IBleScanListener {

    void onScanLe(BluetoothDevice device, int rssi, byte[] scanRecord);

    void onScanSuccess();

}
