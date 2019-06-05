package com.seabig.blelock.callback;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

/**
 * @author: YJZ
 * @date: 2019/6/4 16:15
 * @Des: 蓝牙扫描回调
 **/
public class LeScanCallback implements BluetoothAdapter.LeScanCallback {

    private IBleScanListener bleScanListener;

    public LeScanCallback(IBleScanListener bleScanListener) {
        this.bleScanListener = bleScanListener;
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.e("TAG", "device = " + device.getAddress());
        if (bleScanListener != null) {
            bleScanListener.onScanLe(device, rssi, scanRecord);
        }
    }
}
