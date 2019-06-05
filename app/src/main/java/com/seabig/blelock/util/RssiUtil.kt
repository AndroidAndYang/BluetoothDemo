package com.seabig.blelock.util

/**
 * @author: YJZ
 * @date: 2019/6/4 10:43
 * @Des: 计算蓝牙设备离手机的距离
 */
object RssiUtil {

    /**
     * A - 发射端和接收端相隔1米时的信号强度
     */
    private const val A_Value = 60.0

    /**
     * n - 环境衰减因子
     */
    private const val n_Value = 2.0

    /**
     * 根据Rssi获得返回的距离,返回数据单位为m
     *
     * @param rssi RSSI
     * @return distance
     */
    fun getDistance(rssi: Int): Double {
        val power = (Math.abs(rssi) - A_Value) / (10 * n_Value)
        return Math.pow(10.0, power)
    }
}
