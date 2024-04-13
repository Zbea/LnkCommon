package com.bll.lnkcommon

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import org.greenrobot.eventbus.EventBus

class MyBroadcastReceiver : BroadcastReceiver() {

    @SuppressLint("InvalidWakeLockTag")
    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action){
            "android.intent.action.PACKAGE_ADDED"->{
                EventBus.getDefault().post(Constants.APP_INSTALL_EVENT)
            }
            "android.intent.action.PACKAGE_REMOVED"->{
                EventBus.getDefault().post(Constants.APP_UNINSTALL_EVENT)
            }
            Constants.ACTION_UPLOAD_REFRESH->{
                Log.d("debug","每天自动上传")
                EventBus.getDefault().postSticky(Constants.AUTO_UPLOAD_DAY_EVENT)
//                //屏幕唤醒
//                val pm =  context.getSystemService(Context.POWER_SERVICE) as PowerManager
//                val wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_DIM_WAKE_LOCK,"MyBroadcastReceiver")
//                wl.acquire()
//
//                //屏幕解锁
//                val  km = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
//                val  kl = km.newKeyguardLock("BootBroadcastReceiver")
//                kl.disableKeyguard()
            }
            Constants.ACTION_UPLOAD_YEAR->{
                Log.d("debug","每年自动上传")
                EventBus.getDefault().postSticky(Constants.AUTO_UPLOAD_YEAR_EVENT)
            }
            Constants.ACTION_DAY_REFRESH->{
                Log.d("debug","每天刷新")
                EventBus.getDefault().postSticky(Constants.AUTO_REFRESH_EVENT)
            }
            Constants.DATA_BROADCAST_EVENT->{
                Log.d("debug","一键下载")
                EventBus.getDefault().postSticky(Constants.SETTING_DATA_EVENT)
            }
        }
    }
}