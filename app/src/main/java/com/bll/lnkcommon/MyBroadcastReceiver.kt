package com.bll.lnkcommon

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.greenrobot.eventbus.EventBus

class MyBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action){
            "android.intent.action.PACKAGE_ADDED"->{
                EventBus.getDefault().post(Constants.APP_INSTALL_EVENT)
            }
            "android.intent.action.PACKAGE_REMOVED"->{
                EventBus.getDefault().post(Constants.APP_UNINSTALL_EVENT)
            }
            Constants.ACTION_UPLOAD_1MONTH->{
                Log.d("debug","每年更新")
                EventBus.getDefault().postSticky(Constants.AUTO_UPLOAD_1MONTH_EVENT)
            }
            Constants.ACTION_UPLOAD->{
                Log.d("debug","每天更新")
                EventBus.getDefault().postSticky(Constants.AUTO_UPLOAD_EVENT)
            }
        }
    }
}