package com.bll.lnkcommon

import android.os.Environment

//  ┏┓　　　┏┓
//┏┛┻━━━┛┻┓
//┃　　　　　　　┃
//┃　　　━　　　┃
//┃　┳┛　┗┳　┃
//┃　　　　　　　┃
//┃　　　┻　　　┃
//┃　　　　　　　┃
//┗━┓　　　┏━┛
//    ┃　　　┃   神兽保佑
//    ┃　　　┃   代码无BUG！
//    ┃　　　┗━━━┓
//    ┃　　　　　　　┣┓
//    ┃　　　　　　　┏┛
//    ┗┓┓┏━┳┓┏┛
//      ┃┫┫　┃┫┫
//      ┗┻┛　┗┻┛
/**
 * desc: 常量  分辨率为 1404x1872，屏幕尺寸为 10.3
 */
object Constants {

        const val halfYear=180*24*60*60*1000
        const val dayLong=24*60*60*1000
        const val weekTime=7*24*60*60*1000
        const val STATUS_BAR_SHOW=2147483647//永不消失

//                const val URL_BASE = "https://api2.qinglanmb.com/v1/"
        const val URL_BASE = "http://192.168.101.100:10800/v1/"
//        const val  RELEASE_BASE_URL = "http://www.htfyun.com.cn:8080/"
//        const val RELEASE_BASE_URL = "http://sys.qinglanmb.com:8080/"
        const val RELEASE_BASE_URL = "https://api2.qinglanmb.com/v1/"
        const val UPDATE_URL="http://cdn.qinglanmb.com/"

        ///storage/emulated/0/Android/data/yourPackageName/files/Zip
        val ZIP_PATH = MyApplication.mContext.getExternalFilesDir("Zip")?.path
        ///storage/emulated/0/Android/data/yourPackageName/files/APK
        val APK_PATH = MyApplication.mContext.getExternalFilesDir("APK")?.path
        //笔记保存目录
        val NOTE_PATH = MyApplication.mContext.getExternalFilesDir("Note")?.path
        val FREE_NOTE_PATH = MyApplication.mContext.getExternalFilesDir("FreeNote")?.path
        val IMAGE_PATH = MyApplication.mContext.getExternalFilesDir("Image")?.path
        //断点记录文件保存的文件夹
        val RECORDER_PATH= MyApplication.mContext.getExternalFilesDir("Recorder")!!.path
        val HOMEWORK_PATH = MyApplication.mContext.getExternalFilesDir("Homework")?.path
        val TEXTBOOK_PATH = MyApplication.mContext.getExternalFilesDir("TextBookFile")!!.path
        //解压的目录
        val BOOK_PATH =Environment.getExternalStoragePublicDirectory("Books").absolutePath
        val SCREEN_PATH =Environment.getExternalStoragePublicDirectory("Screenshots").absolutePath
        val DOCUMENT_PATH =Environment.getExternalStoragePublicDirectory("Documents").absolutePath

        //eventbus通知标志
        const val AUTO_REFRESH_EVENT = "AutoRefreshEvent" //每天刷新
        const val DATE_DRAWING_EVENT = "DateDrawingEvent" //日历手写事件
        const val BOOK_EVENT = "BookEvent"
        const val TEXT_BOOK_EVENT = "TextBookEvent"
        const val NOTE_TYPE_REFRESH_EVENT = "NoteTypeRefreshEvent"
        const val NOTE_EVENT = "NoteEvent"
        const val STUDENT_EVENT="StudentEvent"
        const val HOMEWORK_CORRECT_EVENT="CorrectEvent"
        const val USER_EVENT="UserEvent"
        const val APP_INSTALL_EVENT="AppInstallEvent"
        const val APP_INSTALL_INSERT_EVENT="AppInstallInsertEvent"
        const val APP_UNINSTALL_EVENT="AppUnInstallEvent"
        const val CALENDER_EVENT = "CalenderEvent"
        const val CALENDER_SET_EVENT = "CalenderSetEvent"
        const val SETTING_DATA_UPLOAD_EVENT = "SettingDataUploadEvent" //系统设置 一键下载
        const val REFRESH_STUDENT_PERMISSION_EVENT = "RefreshStudentPermission"
        const val SCREENSHOT_MANAGER_EVENT="ScreenshotManagerEvent"//截图管理刷新

        const val PACKAGE_GEOMETRY="com.geometry"
        const val PACKAGE_READER = "com.geniatech.knote.reader"
        const val PACKAGE_SYSTEM_UPDATE = "com.htfyun.firmwareupdate"

        const val ACTION_DAY_REFRESH = "com.bll.lnkcommon.refresh"//每天0刷新
        const val NET_REFRESH="com.htfyun.blackwhitebar.refresh"
        //广播
        const val DATA_UPLOAD_BROADCAST_EVENT = "com.htfyun.blackwhitebar.uploaddata"
        const val LOGIN_BROADCAST_EVENT="com.bll.lnkcommon.account.login"
        const val LOGOUT_BROADCAST_EVENT="com.bll.lnkcommon.account.logout"
        const val NETWORK_CONNECTION_COMPLETE_EVENT = "NetworkConnectionCompleteEvent"//网络连接成功

        //OTA SN前缀
        const val PERSIST_OTA_SN_PREFIX = "persist.ota.sn.prefix"
        const val SN = "SN"
        const val KEY = "Key"
        const val VERSION_NO = "VersionNO"
        const val SP_DIARY_BG_SET ="dirayBgRes"//日记

}


