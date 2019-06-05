package com.seabig.blelock.callback;

/**
 * @author: YJZ
 * @date: 2019/6/5 9:38
 * @Des: 发送数据监听
 */
public interface IWriteListener {
    /**
     * 蓝牙未开启
     */
    int FAILED_BLUETOOTH_DISABLE = 1;
    /**
     * 特征无效
     */
    int FAILED_INVALID_CHARACTER = 3;
    /**
     * 操作失败
     */
    int FAILED_OPERATION = 5;

    void onSuccess();

    void onFail(int state);
}
