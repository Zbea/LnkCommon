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

//                const val URL_BASE = "https://api2.qinglanmb.com/v1/"
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
        val FREE_NOTE_PATH = MyApplication.mContext.getExternalFilesDir("FreeNote")?.path
        val IMAGE_PATH = MyApplication.mContext.getExternalFilesDir("Image")?.path
        //断点记录文件保存的文件夹
        val RECORDER_PATH= MyApplication.mContext.getExternalFilesDir("Recorder")!!.path
        val HOMEWORK_PATH = MyApplication.mContext.getExternalFilesDir("Homework")?.path

        val TEXTBOOK_PATH = MyApplication.mContext.getExternalFilesDir("TextBookFile")!!.path
        val TEXTBOOK_CATALOG_TXT = "catalog.txt" //book文本信息的json文件
        val TEXTBOOK_PICTURE_FILES = "contents" //图片资源的最确路径


        //eventbus通知标志
        const val DATE_EVENT = "DateEvent"
        const val BOOK_EVENT = "BookEvent"
        const val TEXT_BOOK_EVENT = "TextBookEvent"
        const val NOTE_BOOK_MANAGER_EVENT = "NoteBookManagerEvent"
        const val NOTE_EVENT = "NoteEvent"
        const val STUDENT_EVENT="StudentEvent"
        const val HOMEWORK_CORRECT_EVENT="CorrectEvent"
        const val USER_EVENT="UserEvent"
        const val RECORD_EVENT="RecordEvent"
        const val APP_INSTALL_EVENT="AppInstallEvent"
        const val APP_UNINSTALL_EVENT="AppUnInstallEvent"
        const val CALENDER_EVENT = "CalenderEvent"

        const val PACKAGE_WX="com.tencent.mm"
        const val PACKAGE_GEOMETRY="com.geometry"
}


