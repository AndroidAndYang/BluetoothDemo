package com.seabig.blelock.callback;

/**
 * @author: YJZ
 * @date: 2019/6/5 9:59
 * @Des: 蓝牙设备数据返回监听
 **/
public interface IReceiverListener {

    void onReceiver(byte[] value);

}
