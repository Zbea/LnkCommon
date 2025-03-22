package com.bll.lnkcommon

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.bll.lnkcommon.greendao.DaoMaster
import com.bll.lnkcommon.greendao.DaoSession
import com.bll.lnkcommon.greendao.GreenDaoUpgradeHelper
import com.bll.lnkcommon.utils.ActivityManager
import com.bll.lnkcommon.utils.NetworkUtil
import com.bll.lnkcommon.utils.SToast
import com.bll.lnkcommon.utils.SPUtil
import com.liulishuo.filedownloader.FileDownloader
import kotlin.properties.Delegates


class MyApplication : Application(){


    companion object {

        private const val TAG = "MyApplication"

        var mContext: Context by Delegates.notNull()
            private set
        var mDaoSession: DaoSession?=null
        var requestQueue: RequestQueue? = null
    }

    override fun onCreate() {
        super.onCreate()
        mContext = applicationContext

        requestQueue = Volley.newRequestQueue(applicationContext)
        SPUtil.init(this)
        SToast.initToast(this)
        FileDownloader.setup(this)
        NetworkUtil.init(this)
        registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks)
        setDatabase()
    }

    /**
     * 配置greenDao
     */
    private fun setDatabase() {
        // 通过DaoMaster 的内部类 DevOpenHelper，你可以得到一个便利的SQLiteOpenHelper 对象。
        // 可能你已经注意到了，你并不需要去编写「CREATE TABLE」这样的 SQL 语句，因为greenDAO。
        // 注意：默认的DaoMaster.DevOpenHelper 会在数据库升级时，删除所有的表，意味着这将导致数据的丢失。
        // 所以，在正式的项目中，你还应该做一层封装，来实现数据库的安全升级。
        val mHelper = GreenDaoUpgradeHelper(this, "lnkCommon.db" , null)
        val  db = mHelper.writableDatabase
        // 注意：该数据库连接属于DaoMaster，所以多个 Session 指的是相同的数据库连接。
        val mDaoMaster = DaoMaster(db)
        mDaoSession = mDaoMaster.newSession()

    }

    private val mActivityLifecycleCallbacks = object :ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            Log.d(TAG, "onCreated: " + activity.componentName.className)
            ActivityManager.getInstance().addActivity(activity)
        }

        override fun onActivityStarted(activity: Activity) {
            Log.d(TAG, "onStart: " + activity.componentName.className)
        }

        override fun onActivityResumed(activity: Activity) {

        }

        override fun onActivityPaused(activity: Activity) {

        }

        override fun onActivityStopped(activity: Activity) {

        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

        }

        override fun onActivityDestroyed(activity: Activity) {
            Log.d(TAG, "onDestroy: " + activity.componentName.className)
            ActivityManager.getInstance().finishActivity(activity)

        }
    }


}
