package com.bll.lnkcommon

import com.bll.lnkcommon.Constants.BOOK_DRAW_PATH
import com.bll.lnkcommon.Constants.BOOK_PATH
import com.bll.lnkcommon.Constants.FREE_NOTE_PATH
import com.bll.lnkcommon.Constants.HOMEWORK_PATH
import com.bll.lnkcommon.Constants.IMAGE_PATH
import com.bll.lnkcommon.Constants.NOTE_PATH
import com.bll.lnkcommon.Constants.SCREEN_PATH
import com.bll.lnkcommon.Constants.TEXTBOOK_CATALOG_TXT
import com.bll.lnkcommon.Constants.TEXTBOOK_PATH
import com.bll.lnkcommon.Constants.TEXTBOOK_PICTURE_FILES
import com.bll.lnkcommon.Constants.ZIP_PATH
import com.bll.lnkcommon.mvp.model.User
import com.bll.lnkcommon.utils.SPUtil
import java.io.File

class FileAddress {

    private fun getUserId():Long{
        val mUser=SPUtil.getObj("user", User::class.java)
        return mUser?.accountId ?: 0
    }

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
        return "$TEXTBOOK_PATH/${getUserId()}/$fileName"
    }
    fun getPathTextBookDraw(fileName: String):String{
        return "$TEXTBOOK_PATH/${getUserId()}/${fileName}/draw"
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
        return "$NOTE_PATH/${getUserId()}/$typeStr/$noteBookStr/$date"
    }

    /**
     * 笔记保存地址
     */
    fun getPathNote(typeStr: String?,noteBookStr: String?):String{
        return "$NOTE_PATH/${getUserId()}/$typeStr/$noteBookStr"
    }

    /**
     * 日历保存地址
     */
    fun getPathDate(dateStr:String):String{
        return "$IMAGE_PATH/${getUserId()}/date/$dateStr"
    }

    /**
     * 获取作业批改路径
     */
    fun getPathCorrect(id:Int):String{
        return "$HOMEWORK_PATH/${getUserId()}/$id"
    }

    /**
     * 朗读作业文件夹路径
     */
    fun getPathRecord():String{
        return "$HOMEWORK_PATH/${getUserId()}"
    }

    /**
     * 随笔文件路径
     */
    fun getPathFreeNote(title:String):String{
        return "$FREE_NOTE_PATH/${getUserId()}/$title"
    }

    /**
     * 计划总览路径
     */
    fun getPathPlan(year:Int,month:Int):String{
        return "$IMAGE_PATH/${getUserId()}/month/$year$month"
    }
    /**
     * 计划总览路径
     */
    fun getPathPlan(startTime:String):String{
        return "$IMAGE_PATH/${getUserId()}/week/$startTime"
    }

    /**
     * 日历背景下载地址
     */
    fun getPathCalender(fileName: String):String{
        return "$IMAGE_PATH/${getUserId()}/calender/$fileName"
    }


    /**
     * 日记路径
     */
    fun getPathDiary(time:String):String{
        return "$IMAGE_PATH/${getUserId()}/diary/$time"
    }

    /**
     * 壁纸
     */
    fun getPathImage(typeStr: String,contentId: Int):String{
        return "$IMAGE_PATH/${getUserId()}/$typeStr/$contentId"
    }

    /**
     * 截图
     */
    fun getPathScreen(typeStr: String):String{
        return "$SCREEN_PATH/${getUserId()}/$typeStr"
    }

}