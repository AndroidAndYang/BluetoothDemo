package com.seabig.blelock.util.helper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.seabig.blelock.callback.IBleScanListener;
import com.seabig.blelock.callback.IConnectListener;
import com.seabig.blelock.callback.IReceiverListener;
import com.seabig.blelock.callback.IWriteListener;
import com.seabig.blelock.callback.LeScanCallback;
import com.seabig.blelock.callback.implement.ReceiverRequestQueueImpl;
import com.seabig.blelock.util.AESUtil;
import com.seabig.blelock.util.HexUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author: YJZ
 * @date: 2019/6/4 15:50
 * @Des: 蓝牙控制器
 **/
public class BleController {

    private final String TAG = this.getClass().getSimpleName();

    private static BleController mBleController;
    private Context mContext;

    private BluetoothManager mBleManager;
    private BluetoothAdapter mBleAdapter;
    private BluetoothGatt mBleGatt;
    private BluetoothGattCharacteristic mBleGattCharacteristic;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private IWriteListener writeCallback;

    private boolean mScanning;

    //默认扫描时间：10s
    private static final int SCAN_TIME = 10000;
    //默认连接超时时间:10s
    private static final int CONNECTION_TIME_OUT = 10000;
    //获取到所有服务的集合
    private HashMap<String, Map<String, BluetoothGattCharacteristic>> servicesMap = new HashMap<>();
    //连接请求是否ok
    private boolean isConnectOk = false;
    //是否是用户手动断开
    private boolean isMyBreak = false;
    //连接结果的回调
    private IConnectListener connectCallback;
    //读操作请求队列
    private ReceiverRequestQueueImpl mReceiverRequestQueue = new ReceiverRequestQueueImpl();
    //此属性一般不用修改
    private static final String BLUETOOTH_NOTIFY_D = "00002902-0000-1000-8000-00805f9b34fb";
    // 根据自己的蓝牙硬件来设置适合自己的UUID
    private static final String UUID_SERVICE = "0000fee7-0000-1000-8000-00805f9b34fb";
    private static final String UUID_NOTIFY = "000036f6-0000-1000-8000-00805f9b34fb";
    private static final String UUID_WRITE = "000036f5-0000-1000-8000-00805f9b34fb";

    public static synchronized BleController getInstance() {
        if (null == mBleController) {
            mBleController = new BleController();
        }
        return mBleController;
    }

    public boolean init(Context context) {

        mContext = context.getApplicationContext();
        mBleManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);

        if (null == mBleManager) {
            Log.e(TAG, "BluetoothManager init error!");
            return false;
        }

        mBleAdapter = mBleManager.getAdapter();
        if (null == mBleAdapter) {
            Log.e(TAG, "BluetoothManager init error!");
            return false;
        }
        return true;
    }

    /**
     * 扫描设备
     *
     * @param time         指定扫描时间
     * @param scanCallback 扫描回调
     */
    private void startScan(int time, final boolean enable, final IBleScanListener scanCallback) {
        if (isBlueEnable()) {
            // todo 蓝牙开启需要调用Intent ,startActivityForResult 优雅的申请
            // 偷懒方式
            mBleAdapter.enable();
            Log.e(TAG, "Bluetooth is not open!");
        }
        if (null != mBleGatt) {
            mBleGatt.close();
        }
        reset();
        final LeScanCallback bleDeviceScanCallback = new LeScanCallback(scanCallback);
        if (enable) {
            if (mScanning) return;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    //time后停止扫描
                    mBleAdapter.stopLeScan(bleDeviceScanCallback);
                    scanCallback.onScanSuccess();
                }
            }, time <= 0 ? SCAN_TIME : time);
            mScanning = true;
            mBleAdapter.startLeScan(bleDeviceScanCallback);
        } else {
            mScanning = false;
            mBleAdapter.stopLeScan(bleDeviceScanCallback);
        }
    }


    /**
     * 扫描设备，默认扫描10s
     *
     * @param bleScanListener 蓝牙扫描回调
     */
    public void startScan(final boolean enable, final IBleScanListener bleScanListener) {
        startScan(SCAN_TIME, enable, bleScanListener);
    }

    /**
     * 连接设备
     *
     * @param connectionTimeOut 指定连接超时
     * @param address           设备mac地址
     * @param connectCallback   连接回调
     */
    private void connect(final int connectionTimeOut, final String address, IConnectListener connectCallback) {

        if (mBleAdapter == null || address == null) {
            Log.e(TAG, "No device found at this address：" + address);
            return;
        }

        BluetoothDevice remoteDevice = mBleAdapter.getRemoteDevice(address);
        if (remoteDevice == null) {
            Log.e(TAG, "Device not found.  Unable to connect.");
            return;
        }
        this.connectCallback = connectCallback;
        mBleGatt = remoteDevice.connectGatt(mContext, false, new BleGattCallback());
        Log.e(TAG, "connecting mac-address:" + address);
        delayConnectResponse(connectionTimeOut);
    }

    /**
     * 连接设备
     *
     * @param address         设备mac地址
     * @param connectCallback 连接回调
     */
    public void connect(final String address, IConnectListener connectCallback) {
        connect(CONNECTION_TIME_OUT, address, connectCallback);
    }

    /**
     * 发送数据
     *
     * @param value         指令
     * @param writeCallback 发送回调
     */
    public void write(String value, IWriteListener writeCallback) {
        this.writeCallback = writeCallback;
        if (isBlueEnable()) {
            writeCallback.onFail(IWriteListener.FAILED_BLUETOOTH_DISABLE);
            Log.e(TAG, "FAILED_BLUETOOTH_DISABLE");
            return;
        }

        if (mBleGattCharacteristic == null) {
            mBleGattCharacteristic = getBluetoothGattCharacteristic(UUID_SERVICE, UUID_WRITE);
        }

        if (null == mBleGattCharacteristic) {
            writeCallback.onFail(IWriteListener.FAILED_INVALID_CHARACTER);
            Log.e(TAG, "FAILED_INVALID_CHARACTER");
            return;
        }

        //设置数组进去
        mBleGattCharacteristic.setValue(AESUtil.INSTANCE.defaultEncrypt(HexUtil.hexStringToBytes(value)));

        //发送
        boolean success = mBleGatt.writeCharacteristic(mBleGattCharacteristic);
        Log.e(TAG, "send status: " + success + " data：" + value);
    }

    /**
     * 设置读取数据的监听
     *
     * @param requestKey         key
     * @param onReceiverCallback 通知监听回调
     */
    public void registerReceiverListener(String requestKey, IReceiverListener onReceiverCallback) {
        mReceiverRequestQueue.set(requestKey, onReceiverCallback);
    }

    /**
     * 移除读取数据的监听
     *
     * @param requestKey key
     */
    public void unRegisterReceiverListener(String requestKey) {
        mReceiverRequestQueue.remove(requestKey);
    }

    /**
     * 手动断开Ble连接
     */
    public void closeBleConnect() {
        disConnection();
        isMyBreak = true;
        mBleGattCharacteristic = null;
        mBleManager = null;
    }

    /**
     * 当前蓝牙是否打开
     */
    private boolean isBlueEnable() {
        if (null != mBleAdapter) {
            return !mBleAdapter.isEnabled();
        }
        return true;
    }

    /**
     * 重置数据
     */
    private void reset() {
        isConnectOk = false;
        servicesMap.clear();
    }

    /**
     * 超时断开
     *
     * @param connectionTimeOut 连接超时时间
     */
    private void delayConnectResponse(int connectionTimeOut) {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isConnectOk && !isMyBreak) {
                    Log.e(TAG, "connect timeout");
                    disConnection();
                    reConnect();
                } else {
                    isMyBreak = false;
                }
            }
        }, connectionTimeOut <= 0 ? CONNECTION_TIME_OUT : connectionTimeOut);
    }

    /**
     * 断开连接
     */
    private void disConnection() {
        if (null == mBleAdapter || null == mBleGatt) {
            Log.e(TAG, "disconnection error maybe no init");
            return;
        }
        mBleGatt.disconnect();
        reset();
    }

    /**
     * 蓝牙GATT连接及操作事件回调
     */
    private class BleGattCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if (newState == BluetoothProfile.STATE_CONNECTED) { //连接成功
                isMyBreak = false;
                isConnectOk = true;
                mBleGatt.discoverServices();
                connSuccess();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {   //断开连接
                if (!isMyBreak) {
                    reConnect();
                }
                reset();
            }
        }

        //发现新服务
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (null != mBleGatt && status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> services = mBleGatt.getServices();
                for (int i = 0; i < services.size(); i++) {
                    HashMap<String, BluetoothGattCharacteristic> charMap = new HashMap<>();
                    BluetoothGattService bluetoothGattService = services.get(i);
                    String serviceUuid = bluetoothGattService.getUuid().toString();
                    List<BluetoothGattCharacteristic> characteristics = bluetoothGattService.getCharacteristics();
                    for (int j = 0; j < characteristics.size(); j++) {
                        charMap.put(characteristics.get(j).getUuid().toString(), characteristics.get(j));
                    }
                    servicesMap.put(serviceUuid, charMap);
                }

                final BluetoothGattCharacteristic NotificationCharacteristic = getBluetoothGattCharacteristic(UUID_SERVICE, UUID_NOTIFY);
                if (NotificationCharacteristic == null) {
                    Log.e(TAG, "NotificationCharacteristic is null");
                    return;
                }

                boolean notification = enableNotification(true, NotificationCharacteristic);
                Log.e(TAG, "onServicesDiscovered is = " + notification);
            }
        }

        //读数据
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.e("BleController", "onCharacteristicRead status = " + status);
            Log.e("BleController", "onCharacteristicRead = " + Arrays.toString(characteristic.getValue()));
        }

        //写数据
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (null != writeCallback) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            writeCallback.onSuccess();
                        }
                    });
                    Log.e(TAG, "Send data success!");
                } else {
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            writeCallback.onFail(IWriteListener.FAILED_OPERATION);
                        }
                    });
                    Log.e(TAG, "Send data failed!");
                }
            }
        }

        //通知数据，硬件返回数据时在此回调
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            if (null != mReceiverRequestQueue) {
                HashMap<String, IReceiverListener> map = mReceiverRequestQueue.getMap();
                final byte[] rec = characteristic.getValue();
                for (String key : mReceiverRequestQueue.getMap().keySet()) {
                    final IReceiverListener onReceiverCallback = map.get(key);
                    if (onReceiverCallback != null) {
                        runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                onReceiverCallback.onReceiver(rec);
                            }
                        });
                        break;
                    }
                }
            }
        }
    }

    /**
     * 设置通知
     *
     * @param enable         是否打开
     * @param characteristic 通知特征
     * @return 是否成功开启
     */
    private boolean enableNotification(boolean enable, BluetoothGattCharacteristic characteristic) {

        if (mBleGatt == null || characteristic == null)
            return false;

        if (!mBleGatt.setCharacteristicNotification(characteristic, enable))
            return false;

        BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(UUID.fromString(BLUETOOTH_NOTIFY_D));

        if (clientConfig == null)
            return false;

        if (enable) {
            clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            clientConfig.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        return mBleGatt.writeDescriptor(clientConfig);
    }

    public BluetoothGattService getService(UUID uuid) {
        if (mBleAdapter == null || mBleGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
            return null;
        }
        return mBleGatt.getService(uuid);
    }

    /**
     * 根据服务UUID和特征UUID,获取一个特征
     *
     * @param serviceUUID   服务UUID
     * @param characterUUID 特征UUID
     */
    private BluetoothGattCharacteristic getBluetoothGattCharacteristic(String serviceUUID, String characterUUID) {

        if (isBlueEnable()) {
            throw new IllegalArgumentException(" Bluetooth is no enable please call BluetoothAdapter.enable()");
        }

        if (null == mBleGatt) {
            Log.e(TAG, "mBluetoothGatt is null");
            return null;
        }

        //找服务
        Map<String, BluetoothGattCharacteristic> bluetoothGattCharacteristicMap = servicesMap.get(serviceUUID);
        if (null == bluetoothGattCharacteristicMap) {
            Log.e(TAG, "Not found the serviceUUID!");
            return null;
        }

        //找特征
        Set<Map.Entry<String, BluetoothGattCharacteristic>> entries = bluetoothGattCharacteristicMap.entrySet();
        BluetoothGattCharacteristic gattCharacteristic = null;
        for (Map.Entry<String, BluetoothGattCharacteristic> entry : entries) {
            if (characterUUID.equals(entry.getKey())) {
                gattCharacteristic = entry.getValue();
                break;
            }
        }
        return gattCharacteristic;
    }

    /**
     * 主线中运行
     */
    private void runOnMainThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            if (mHandler != null) {
                mHandler.post(runnable);
            }
        }
    }

    // TODO 此方法断开连接或连接失败时会被调用。可在此处理自动重连,内部代码可自行修改，如发送广播
    private void reConnect() {
        if (connectCallback != null) {
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    connectCallback.onConnFailed();
                }
            });
        }
        Log.e(TAG, "Ble disconnect or connect failed!");
    }

    // TODO 此方法Notify成功时会被调用。可在通知界面连接成功,内部代码可自行修改，如发送广播
    private void connSuccess() {
        if (connectCallback != null) {
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    connectCallback.onConnSuccess();
                }
            });
        }
        Log.e(TAG, "Ble connect success!");
    }
}
