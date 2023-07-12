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

//                const val URL_BASE = "https://api2.bailianlong.com/v1/"
        const val URL_BASE = "http://192.168.101.100:10800/v1/"

        ///storage/emulated/0/Android/data/yourPackageName/files/Zip
        val ZIP_PATH = MyApplication.mContext.getExternalFilesDir("Zip")?.path
        ///storage/emulated/0/Android/data/yourPackageName/files/APK
        val APK_PATH = MyApplication.mContext.getExternalFilesDir("APK")?.path
        //解压的目录
        val BOOK_PATH = Environment.getExternalStorageDirectory().absolutePath + "/Books"
        val BOOK_DRAW_PATH= Environment.getExternalStorageDirectory().absolutePath+"/Notes"
        //笔记保存目录
        val NOTE_PATH = MyApplication.mContext.getExternalFilesDir("Note")?.path
        //日历保存
        val DATE_PATH = MyApplication.mContext.getExternalFilesDir("Date")?.path
        val IMAGE_PATH = MyApplication.mContext.getExternalFilesDir("Image")?.path

        //eventbus通知标志
        const val DATE_EVENT = "DateEvent"
        const val BOOK_EVENT = "BookEvent"
        const val NOTE_BOOK_MANAGER_EVENT = "NoteBookManagerEvent"
        const val NOTE_EVENT = "NoteEvent"
        const val APP_EVENT = "APPEvent"


}


