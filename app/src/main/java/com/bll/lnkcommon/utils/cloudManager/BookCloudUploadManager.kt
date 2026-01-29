package com.bll.lnkcommon.utils.cloudManager

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.manager.BookDaoManager
import com.bll.lnkcommon.mvp.model.CloudListBean
import com.bll.lnkcommon.mvp.model.book.Book
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.utils.MD5Utils
import com.bll.lnkcommon.utils.cloudManager.CloudUploadUtils.limitedDispatcher
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

class BookCloudUploadManager(private val lifecycleOwner: LifecycleOwner) {

    fun upload(token: String) {
        lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            val cloudList = withContext(Dispatchers.IO) {
                queryAndBuildCloudList()
            }

            if (cloudList.isNotEmpty()) {
                val uploadTasks = cloudList.map { cloudItem ->
                    launch(limitedDispatcher) {
                        try {
                            if (FileUtils.isExistContent(cloudItem.path)){
                                cloudItem.downloadUrl = CloudUploadUtils.uploadZipFile(cloudItem.path, cloudItem.title, token)
                            }
                            val isSuccess = CloudUploadUtils.submitToCloudLibrary(cloudItem)
                            if (isSuccess) {
                                val book=Gson().fromJson(cloudItem.listJson, Book::class.java)
                                MethodManager.deleteBook(book)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                uploadTasks.joinAll()
            }

            EventBus.getDefault().post(Constants.BOOK_EVENT)
        }
    }

    private suspend fun queryAndBuildCloudList(): MutableList<CloudListBean>{
        return withContext(Dispatchers.IO) {
            val cloudList = mutableListOf<CloudListBean>()
            val books= BookDaoManager.getInstance().queryBookByHalfYear()
            for (book in books){
                cloudList.add(CloudListBean().apply {
                    type=1
                    subTypeStr= DataBeanManager.getBookStoreTypeStr(book.type)
                    date=System.currentTimeMillis()
                    listJson= Gson().toJson(book)
                    zipUrl=book.downloadUrl
                    bookId=book.bookId
                    this.path=book.bookDrawPath
                    title=MD5Utils.digest(book.bookId.toString())
                })
            }

            cloudList
        }
    }

}