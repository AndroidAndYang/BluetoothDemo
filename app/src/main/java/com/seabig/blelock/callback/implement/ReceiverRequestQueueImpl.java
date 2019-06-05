package com.seabig.blelock.callback.implement;

import android.util.Log;

import com.seabig.blelock.callback.IReceiverListener;
import com.seabig.blelock.callback.IRequestQueueListener;

import java.util.HashMap;

/**
 * @author: YJZ
 * @date: 2019/6/5 9:57
 * @Des: 接收通知数据请求队列
 **/
public class ReceiverRequestQueueImpl implements IRequestQueueListener<IReceiverListener> {

    private static final String TAG = ReceiverRequestQueueImpl.class.getSimpleName();

    private HashMap<String, IReceiverListener> map = new HashMap<>();

    @Override
    public void set(String key, IReceiverListener iReceiverListener) {
        if (!map.containsKey(key))
            map.put(key, iReceiverListener);
    }

    @Override
    public IReceiverListener get(String key) {
        return map.get(key);
    }

    public HashMap<String, IReceiverListener> getMap() {
        return map;
    }

    public boolean remove(String key) {
        Log.e(TAG, "ReceiverRequestQueue before:" + map.size());
        IReceiverListener iReceiverListener = map.remove(key);
        Log.e(TAG, "ReceiverRequestQueue after:" + map.size());
        return iReceiverListener == null;
    }
}
