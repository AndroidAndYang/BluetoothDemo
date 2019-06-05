package com.seabig.blelock.adapter

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.seabig.blelock.R

import java.util.ArrayList
import java.util.Locale

/**
 * @author: YJZ
 * @date: 2019/6/4 14:52
 * @Des: RecyclerView Adapter
 */
class BleDeviceAdapter(private val mContext: Context) : RecyclerView.Adapter<BleDeviceAdapter.ViewHolder>() {

    private val mDeviceList: MutableList<BluetoothDevice>
    private val mRssiList: MutableList<Double>
    private var position: Int = 0

    private var mOnRecyclerViewItemClickListen: OnRecyclerViewItemClickListen? = null

    init {
        mDeviceList = ArrayList()
        mRssiList = ArrayList()
    }

    fun add(device: BluetoothDevice, rssi: Double) {
        if (!mDeviceList.contains(device)) {
            mDeviceList.add(position, device)
            mRssiList.add(position, rssi)
            position++
        }
        notifyItemInserted(position)
    }

    fun clear() {
        if (mDeviceList.size != 0) {
            mDeviceList.clear()
        }
        if (mRssiList.size != 0) {
            mRssiList.clear()
        }
        position = 0
        notifyDataSetChanged()
    }

    operator fun get(position: Int): BluetoothDevice? {
        return if (mDeviceList.size > 0) {
            mDeviceList[position]
        } else null
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.adapter_ble, viewGroup, false)
        val viewHolder = ViewHolder(view)
        //给控件设置点击事件
        if (mOnRecyclerViewItemClickListen != null) {
            viewHolder.itemView.setOnClickListener { mOnRecyclerViewItemClickListen!!.onItemClickListen(view, viewHolder.layoutPosition) }
        }
        return viewHolder
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.mBleNameTv.text = mDeviceList[position].name
        viewHolder.mBleAddressTv.text = mDeviceList[position].address
        viewHolder.mBleRssiTv.text = String.format(Locale.CHINA, "%.2f米", mRssiList[position])
    }

    override fun getItemCount(): Int {
        return mDeviceList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var mBleNameTv: TextView
        var mBleAddressTv: TextView
        var mBleRssiTv: TextView

        init {
            mBleNameTv = itemView.findViewById(R.id.ble_name)
            mBleAddressTv = itemView.findViewById(R.id.ble_mac_address)
            mBleRssiTv = itemView.findViewById(R.id.ble_rssi)
        }
    }

    /**
     * item 点击监听
     */
    interface OnRecyclerViewItemClickListen {
        /**
         * 定义RecycleView的item点击事件
         *
         * @param view     item
         * @param position 索引
         */
        fun onItemClickListen(view: View, position: Int)
    }

    fun setOnRecyclerViewItemClickListen(mOnRecyclerViewItemClickListen: OnRecyclerViewItemClickListen) {
        this.mOnRecyclerViewItemClickListen = mOnRecyclerViewItemClickListen
    }
}
