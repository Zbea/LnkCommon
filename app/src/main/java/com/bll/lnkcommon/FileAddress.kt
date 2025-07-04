package com.bll.lnkcommon

import com.bll.lnkcommon.Constants.APK_PATH
import com.bll.lnkcommon.Constants.BOOK_PATH
import com.bll.lnkcommon.Constants.DOCUMENT_PATH
import com.bll.lnkcommon.Constants.FREE_NOTE_PATH
import com.bll.lnkcommon.Constants.HOMEWORK_PATH
import com.bll.lnkcommon.Constants.IMAGE_PATH
import com.bll.lnkcommon.Constants.NOTE_PATH
import com.bll.lnkcommon.Constants.SCREEN_PATH
import com.bll.lnkcommon.Constants.TEXTBOOK_PATH
import com.bll.lnkcommon.Constants.ZIP_PATH
import com.bll.lnkcommon.MethodManager.getAccountId

class FileAddress {

    fun getLauncherPath():String{
        return  getPathApk("lnkCommon")
    }

    /**
     * 书籍目录地址
     */
    fun getPathTextBookCatalog(path:String):String{
        return "$path/catalog.txt"
    }
    /**
     * 书籍图片地址
     */
    fun getPathTextBookPicture(path:String):String{
        return "$path/contents"
    }
    /**
     * 书籍地址
     * /storage/emulated/0/Books
     */
    fun getPathBook(fileName: String):String{
        return "$BOOK_PATH/${getAccountId()}/$fileName"
    }
    /**
     * 书籍手写地址
     * /storage/emulated/0/Notes
     */
    fun getPathBookDraw(fileName: String):String{
        return "$BOOK_PATH/${getAccountId()}/${fileName}draw"
    }

    fun getPathHomeworkBook(fileName: String):String{
        return "$TEXTBOOK_PATH/${getAccountId()}/homeworkBook/$fileName"
    }
    fun getPathHomeworkBookDraw(fileName: String):String{
        return "$TEXTBOOK_PATH/${getAccountId()}/homeworkBook/${fileName}draw"
    }
    fun getPathTextBook(fileName: String):String{
        return "$TEXTBOOK_PATH/${getAccountId()}/textbook/$fileName"
    }
    fun getPathTextBookDraw(fileName: String):String{
        return "$TEXTBOOK_PATH/${getAccountId()}/textbook/${fileName}draw"
    }
    /**
     * zip保存地址
     * ///storage/emulated/0/Android/data/yourPackageName/files/Zip/fileName.zip
     */
    fun getPathZip(fileName:String):String{
        return "$ZIP_PATH/$fileName.zip"
    }
    /**
     * apk下载地址
     */
    fun getPathApk(fileName: String):String{
        return "$APK_PATH/$fileName.apk"
    }

    /**
     * 笔记保存地址
     */
    fun getPathNote(typeStr: String?,noteBookStr: String?,date:Long):String{
        return "$NOTE_PATH/${getAccountId()}/$typeStr/$noteBookStr/$date"
    }

    /**
     * 笔记保存地址
     */
    fun getPathNote(typeStr: String?,noteBookStr: String?):String{
        return "$NOTE_PATH/${getAccountId()}/$typeStr/$noteBookStr"
    }

    /**
     * 日历保存地址
     */
    fun getPathDate(dateStr:String):String{
        return "$IMAGE_PATH/${getAccountId()}/date/$dateStr"
    }

    /**
     * 获取作业批改路径
     */
    fun getPathCorrect(id:Int):String{
        return "$HOMEWORK_PATH/${getAccountId()}/$id"
    }

    /**
     * 朗读作业文件夹路径
     */
    fun getPathRecord():String{
        return "$HOMEWORK_PATH/${getAccountId()}"
    }

    /**
     * 随笔文件路径
     */
    fun getPathFreeNote(title:String):String{
        return "$FREE_NOTE_PATH/${getAccountId()}/$title"
    }

    /**
     * 计划总览路径
     */
    fun getPathPlan(year:Int,month:Int):String{
        return "$IMAGE_PATH/${getAccountId()}/month/$year$month"
    }
    /**
     * 计划总览路径
     */
    fun getPathPlan(startTime:String):String{
        return "$IMAGE_PATH/${getAccountId()}/week/$startTime"
    }

    /**
     * 日历背景下载地址
     */
    fun getPathCalender(fileName: String):String{
        return "$IMAGE_PATH/${getAccountId()}/calender/$fileName"
    }


    /**
     * 日记路径
     */
    fun getPathDiary(time:String):String{
        return "$IMAGE_PATH/${getAccountId()}/diary/$time"
    }

    /**
     * 壁纸
     */
    fun getPathImage(typeStr: String,contentId: Int):String{
        return "$IMAGE_PATH/${getAccountId()}/$typeStr/$contentId"
    }

    /**
     * 截图
     */
    fun getPathScreen(typeStr: String):String{
        return "$SCREEN_PATH/${getAccountId()}/$typeStr"
    }

    /**
     * 文档
     */
    fun getPathDocument(typeStr: String):String{
        return "$DOCUMENT_PATH/$typeStr"
    }

}