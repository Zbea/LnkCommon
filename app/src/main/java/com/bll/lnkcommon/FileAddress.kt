package com.bll.lnkcommon

import com.bll.lnkcommon.Constants.BOOK_DRAW_PATH
import com.bll.lnkcommon.Constants.BOOK_PATH
import com.bll.lnkcommon.Constants.DATE_PATH
import com.bll.lnkcommon.Constants.HOMEWORK_PATH
import com.bll.lnkcommon.Constants.NOTE_PATH
import com.bll.lnkcommon.Constants.TEXTBOOK_CATALOG_TXT
import com.bll.lnkcommon.Constants.TEXTBOOK_PATH
import com.bll.lnkcommon.Constants.TEXTBOOK_PICTURE_FILES
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

    fun getPathTextBook(fileName: String):String{
        return "$TEXTBOOK_PATH/$mUserId/$fileName"
    }
    fun getPathTextBookDraw(fileName: String):String{
        return "$TEXTBOOK_PATH/$mUserId/${fileName}/draw"
    }
    /**
     * 书籍目录地址
     */
    fun getPathTextBookCatalog(path:String):String{
        return path + File.separator + TEXTBOOK_CATALOG_TXT
    }
    /**
     * 书籍图片地址
     */
    fun getPathTextBookPicture(path:String):String{
        return path + File.separator + TEXTBOOK_PICTURE_FILES
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

    /**
     * 获取作业批改路径
     */
    fun getPathCorrect(id:Int):String{
        return "$HOMEWORK_PATH/$mUserId/$id"
    }

}