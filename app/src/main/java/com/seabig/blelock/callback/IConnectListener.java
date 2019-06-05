package com.seabig.blelock.callback;

/**
 * @author: YJZ
 * @date: 2019/6/4 16:31
 * @Des: 蓝牙连接监听
 **/
public interface IConnectListener {
    /**
     * 获得通知之后
     */
    void onConnSuccess();

    /**
     * 断开或连接失败
     */
    void onConnFailed();
}
