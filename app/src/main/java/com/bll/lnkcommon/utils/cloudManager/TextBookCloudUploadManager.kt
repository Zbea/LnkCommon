package com.bll.lnkcommon.utils.cloudManager

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.manager.TextbookGreenDaoManager
import com.bll.lnkcommon.mvp.model.CloudListBean
import com.bll.lnkcommon.mvp.model.book.TextbookBean
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.utils.MD5Utils
import com.bll.lnkcommon.utils.cloudManager.CloudUploadUtils.limitedDispatcher
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import java.io.File

class TextBookCloudUploadManager(private val lifecycleOwner: LifecycleOwner) {

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
                                val book=Gson().fromJson(cloudItem.listJson, TextbookBean::class.java)
                                MethodManager.deleteTextbook(book)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                uploadTasks.joinAll()
            }

            EventBus.getDefault().post(Constants.TEXT_BOOK_EVENT)
        }
    }

    private suspend fun queryAndBuildCloudList(): MutableList<CloudListBean>{
        return withContext(Dispatchers.IO) {
            val cloudList = mutableListOf<CloudListBean>()
            val books= TextbookGreenDaoManager.getInstance().queryTextBookByHalfYear()
            for (book in books){
                cloudList.add(CloudListBean().apply {
                    type=2
                    subTypeStr= DataBeanManager.textBookTypes[book.category].title
                    date=System.currentTimeMillis()
                    listJson= Gson().toJson(book)
                    zipUrl=book.downloadUrl
                    bookId=book.bookId
                    bookTypeId = book.category
                    this.path=File(book.bookDrawPath).parent
                    title=MD5Utils.digest(book.bookId.toString())
                })
            }

            cloudList
        }
    }

}