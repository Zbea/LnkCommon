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

//                const val URL_BASE = "https://api2.qinglanmb.com/v1/"
        const val URL_BASE = "http://192.168.101.100:10800/v1/"

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
        val TEXTBOOK_CATALOG_TXT = "catalog.txt" //book文本信息的json文件
        val TEXTBOOK_PICTURE_FILES = "contents" //图片资源的最确路径
        //解压的目录
        val BOOK_PATH =Environment.getExternalStoragePublicDirectory("Books").absolutePath
        val BOOK_DRAW_PATH= Environment.getExternalStoragePublicDirectory("Notes").absolutePath
        val SCREEN_PATH =Environment.getExternalStoragePublicDirectory("Screenshots").absolutePath

        //eventbus通知标志
        const val AUTO_REFRESH_YEAR_EVENT = "AutoRefreshYearEvent"
        const val AUTO_REFRESH_EVENT = "AutoRefreshEvent" //每天刷新
        const val AUTO_UPLOAD_DAY_EVENT = "AutoUploadDayEvent" //每天3点自动上传
        const val AUTO_UPLOAD_YEAR_EVENT = "AutoUploadYearEvent" //每天3点自动上传
        const val DATE_DRAWING_EVENT = "DateDrawingEvent" //日历手写事件
        const val BOOK_EVENT = "BookEvent"
        const val TEXT_BOOK_EVENT = "TextBookEvent"
        const val NOTE_BOOK_MANAGER_EVENT = "NoteBookManagerEvent"
        const val NOTE_EVENT = "NoteEvent"
        const val STUDENT_EVENT="StudentEvent"
        const val HOMEWORK_CORRECT_EVENT="CorrectEvent"
        const val USER_EVENT="UserEvent"
        const val RECORD_EVENT="RecordEvent"
        const val APP_INSTALL_EVENT="AppInstallEvent"
        const val APP_INSTALL_INSERT_EVENT="AppInstallInsertEvent"
        const val APP_UNINSTALL_EVENT="AppUnInstallEvent"
        const val SCREENSHOT_MANAGER_EVENT="ScreenshotManagerEvent"
        const val CALENDER_EVENT = "CalenderEvent"
        const val CALENDER_SET_EVENT = "CalenderSetEvent"
        const val CHECK_PASSWORD_EVENT = "CheckPasswordEvent"
        const val SETTING_DATA_EVENT = "SettingDataEvent" //系统设置 一键下载
        const val REFRESH_STUDENT_PERMISSION_EVENT = "RefreshStudentPermission"

        const val PACKAGE_WX="com.tencent.mm"
        const val PACKAGE_GEOMETRY="com.geometry"
        const val PACKAGE_READER = "com.geniatech.knote.reader"

        const val ACTION_DAY_REFRESH = "com.bll.lnkcommon.refresh"//每天0刷新
        const val ACTION_UPLOAD_REFRESH = "com.bll.lnkcommon.upload"//每天3自动更新
        const val ACTION_UPLOAD_YEAR = "com.bll.lnkcommon.upload.year"//每年12月31 3点自动上传
        //广播
        const val DATA_BROADCAST_EVENT = "com.android.settings.importdatasgl"
        const val LOGIN_BROADCAST_EVENT="com.bll.lnkcommon.account.login"
        const val LOGOUT_BROADCAST_EVENT="com.bll.lnkcommon.account.logout"
}


