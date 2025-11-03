package com.bll.lnkcommon.utils

import android.telecom.Log
import android.text.TextUtils
import com.bll.lnkcommon.Constants
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadLargeFileListener
import com.liulishuo.filedownloader.FileDownloader
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

class DownloadManager() {
    private val taskList = CopyOnWriteArrayList<BaseDownloadTask>()

    // 单任务下载回调
    interface SingleCallback {
        fun onProgress(task: BaseDownloadTask, soFar: Long, total: Long)
        fun onCompleted(task: BaseDownloadTask)
        fun onPaused(task: BaseDownloadTask, soFar: Long, total: Long) {}
        fun onFailed(task: BaseDownloadTask?, error: String)
    }

    // 多任务下载回调
    interface BatchCallback {
        fun onSingleProgress(task: BaseDownloadTask, url: String, soFar: Long, total: Long) {}
        fun onSingleCompleted(task: BaseDownloadTask, url: String, savePath: String) {}
        fun onBatchCompleted()
        fun onBatchFailed(error: String)
    }

    /**
     * 启动单任务下载
     * @param url
     * @param savePath
     * @param isLargeFile
     */
    fun startSingle(url: String, savePath: String, callback: SingleCallback) {

        if (TextUtils.isEmpty(url)) {
            callback.onFailed(null, "URL不能为空")
            return
        }
        if (TextUtils.isEmpty(savePath)) {
            callback.onFailed(null, "保存地址不能为空")
            return
        }
        val isLargeFile= isLargeFileByUrl(url)
        // 创建任务
        val task = createTask(url, savePath, isLargeFile)
        task.setListener(object : FileDownloadLargeFileListener() {
            override fun pending(task: BaseDownloadTask, soFarBytes: Long, totalBytes: Long) {
            }
            override fun progress(task: BaseDownloadTask, soFarBytes: Long, totalBytes: Long) {
                callback.onProgress(task, soFarBytes, totalBytes)
            }
            override fun completed(task: BaseDownloadTask) {
                callback.onCompleted(task)
                removeTask(task.id) // 完成后移除任务
            }
            override fun paused(task: BaseDownloadTask, soFarBytes: Long, totalBytes: Long) {
                callback.onPaused(task, soFarBytes, totalBytes)
            }
            override fun error(task: BaseDownloadTask, e: Throwable) {
                Log.d(Constants.DEBUG,e.message.toString())
                callback.onFailed(task, e.message.toString())
                FileUtils.delete(task.path)
                removeTask(task.id) // 失败后移除任务
            }
            override fun warn(task: BaseDownloadTask) {
            }
        })

        task.start()
        taskList.add(task)
    }

    /**
     * 启动多任务下载
     * @param urlList 多个下载URL
     * @param savePathList 多个保存路径
     */
    fun startBatch(urlList: List<String>, savePathList: List<String>, callback: BatchCallback) {

        if (urlList.isEmpty()) {
            callback.onBatchFailed("URL不能为空")
            return
        }
        if (savePathList.isEmpty()) {
            callback.onBatchFailed("保存地址不能为空")
            return
        }
        if (urlList.size != savePathList.size) {
            callback.onBatchFailed("URL列表与保存地址长度不一致")
            throw IllegalArgumentException()
        }
        val activeCount = AtomicInteger(0)
        activeCount.addAndGet(urlList.size)

        val currentTask= mutableListOf<BaseDownloadTask>()
        // 逐个启动子任务
        urlList.forEachIndexed { index, url ->
            val savePath = (savePathList[index])
            val isLarge = isLargeFileByUrl(url)
            val task = createTask(url, savePath, isLarge)
            task.setListener(object : FileDownloadLargeFileListener() {
                override fun progress(task: BaseDownloadTask, soFarBytes: Long, totalBytes: Long) {
                    callback.onSingleProgress(task, url, soFarBytes, totalBytes)
                }
                override fun completed(task: BaseDownloadTask) {
                    callback.onSingleCompleted(task, url, savePath)
                    if (activeCount.decrementAndGet() == 0) {
                        callback.onBatchCompleted()
                        currentTask.forEach { task ->
                            removeTask(task.id)
                        }
                        currentTask.clear()
                    }
                }
                override fun error(task: BaseDownloadTask, e: Throwable) {
                    Log.d(Constants.DEBUG,e.message.toString())
                    callback.onBatchFailed(e.message.toString())
                    currentTask.forEach { task ->
                        FileDownloader.getImpl().pause(task.id)
                        // 清理未完成的临时文件
                        FileUtils.delete(task.path)
                        removeTask(task.id)
                    }
                    currentTask.clear()
                }
                override fun pending(task: BaseDownloadTask, soFarBytes: Long, totalBytes: Long) {}
                override fun paused(task: BaseDownloadTask, soFarBytes: Long, totalBytes: Long) {}
                override fun warn(task: BaseDownloadTask) {}
            })
            task.start()
            currentTask.add(task)
            taskList.add(task)
        }
    }

    /** 创建下载任务（区分大文件） */
    private fun createTask(url: String, savePath: String, isLargeFile: Boolean): BaseDownloadTask {
        return FileDownloader.getImpl().create(url).apply {
            path = savePath
            autoRetryTimes = 2
            isForceReDownload = !isLargeFile//大文件支持断点续传
            addHeader("Accept-Encoding", "identity")
            addHeader("Authorization", SPUtil.getString("token"))
        }
    }

    /** 自动判断是否为大文件（根据常见大文件后缀） */
    private fun isLargeFileByUrl(url: String): Boolean {
        val largeSuffixes = listOf(".zip", ".rar", ".apk", ".mp4", ".iso", ".tar", ".gz",".pdf",".epub",".mobi",".ppt",".pptx")
        return largeSuffixes.any { url.endsWith(it, ignoreCase = true) }
    }

    /** 移除已完成/失败的任务 */
    private fun removeTask(taskId: Int) {
        taskList.removeAll { it.id == taskId }
    }

    fun pauseAll(){
        taskList.forEach { task ->
            FileDownloader.getImpl().pause(task.id)
        }
        taskList.clear()
    }

    fun isRunning():Boolean{
        return taskList.isNotEmpty()
    }

}
