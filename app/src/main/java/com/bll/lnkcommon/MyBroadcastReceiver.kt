package com.bll.lnkcommon

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.PowerManager
import android.util.Log
import com.bll.lnkcommon.utils.NetworkUtil
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
            Constants.ACTION_DAY_REFRESH->{
                Log.d("debug","每天刷新")
                EventBus.getDefault().postSticky(Constants.AUTO_REFRESH_EVENT)
            }
            Constants.DATA_UPLOAD_BROADCAST_EVENT->{
                Log.d("debug","上传")
                EventBus.getDefault().postSticky(Constants.SETTING_DATA_UPLOAD_EVENT)
            }
            //监听网络变化
            ConnectivityManager.CONNECTIVITY_ACTION->{
                val isNet=intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)
                Log.d("debug", "监听网络变化$isNet")
                if (isNet)
                    EventBus.getDefault().post(Constants.NETWORK_CONNECTION_COMPLETE_EVENT )
            }
            //wifi监听
            WifiManager.NETWORK_STATE_CHANGED_ACTION->{
                val info: NetworkInfo? = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)
                if (info!!.state.equals(NetworkInfo.State.CONNECTED)) {
                    val isNet = NetworkInfo.State.CONNECTED == info.state && info.isAvailable
                    Log.d("debug", "wifi监听网络变化$isNet")
                    if (isNet)
                        EventBus.getDefault().post(Constants.NETWORK_CONNECTION_COMPLETE_EVENT )
                }
            }
            Constants.NET_REFRESH->{
                if (NetworkUtil.isNetworkAvailable(context)){
                    EventBus.getDefault().post(Constants.NETWORK_CONNECTION_COMPLETE_EVENT )
                }
            }
        }
    }
}