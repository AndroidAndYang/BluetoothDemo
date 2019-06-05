package com.seabig.blelock.base

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast

/**
 * @author: YJZ
 * @date: 2019/6/4 15:25
 * @Des: baseActivity
 */
abstract class BaseActivity : AppCompatActivity() {

    private val tag = this.javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(initLayoutId())
        onSettingUpView()
    }

    abstract fun onSettingUpView()

    abstract fun initLayoutId(): Int

    fun e(msg: String) {
        Log.e(tag, msg)
    }

    fun showToast(content: String) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
    }
}
