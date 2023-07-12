package com.bll.lnkcommon

import com.bll.lnkcommon.Constants.BOOK_DRAW_PATH
import com.bll.lnkcommon.Constants.BOOK_PATH
import com.bll.lnkcommon.Constants.DATE_PATH
import com.bll.lnkcommon.Constants.NOTE_PATH
import com.bll.lnkcommon.Constants.ZIP_PATH
import com.bll.lnkcommon.mvp.model.User
import com.bll.lnkcommon.utils.SPUtil
import java.io.File

class FileAddress {

    private val mUserId= SPUtil.getObj("user", User::class.java)?.accountId.toString()

    /**
     * 书籍地址
     * /storage/emulated/0/Books
     */
    fun getPathBook(fileName: String):String{
        return "$BOOK_PATH/$fileName"
    }
    /**
     * 书籍手写地址
     * /storage/emulated/0/Notes
     */
    fun getPathBookDraw(fileName: String):String{
        return "$BOOK_DRAW_PATH/$fileName"
    }

    /**
     * zip保存地址
     * ///storage/emulated/0/Android/data/yourPackageName/files/Zip/fileName.zip
     */
    fun getPathZip(fileName:String):String{
        return ZIP_PATH+File.separator + fileName + ".zip"
    }

    /**
     * apk下载地址
     */
    fun getPathApk(fileName: String):String{
        return Constants.APK_PATH+ File.separator + fileName + ".apk"
    }

    /**
     * 笔记保存地址
     */
    fun getPathNote(typeStr: String?,noteBookStr: String?,date:Long):String{
        return "$NOTE_PATH/$mUserId/$typeStr/$noteBookStr/$date"
    }

    /**
     * 笔记保存地址
     */
    fun getPathNote(typeStr: String?,noteBookStr: String?):String{
        return "$NOTE_PATH/$mUserId/$typeStr/$noteBookStr"
    }

    /**
     * 日历保存地址
     */
    fun getPathDate(dateStr:String):String{
        return "$DATE_PATH/$mUserId/$dateStr"
    }

}