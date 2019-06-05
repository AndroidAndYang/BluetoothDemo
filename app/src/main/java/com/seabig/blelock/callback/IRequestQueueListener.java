package com.seabig.blelock.callback;

/**
 * @author: YJZ
 * @date: 2019/6/5 9:58
 * @Des:
 **/
public interface IRequestQueueListener<T> {

    void set(String key, T t);

    T get(String key);
}
