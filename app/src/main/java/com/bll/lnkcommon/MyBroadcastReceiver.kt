package com.bll.lnkcommon

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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
        }
    }
}