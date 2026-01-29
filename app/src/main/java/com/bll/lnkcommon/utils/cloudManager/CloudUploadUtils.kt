package com.bll.lnkcommon.utils.cloudManager

import VolleyCallback
import VolleyHttpManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.mvp.model.CloudListBean
import com.bll.lnkcommon.utils.FileUploadManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resumeWithException

object CloudUploadUtils {

    val limitedDispatcher = Dispatchers.IO.limitedParallelism(5)

     suspend fun uploadZipFile(filePath: String, title: String, token: String): String {
        return suspendCancellableCoroutine { continuation ->
            FileUploadManager(token).apply {
                setCallBack(object : FileUploadManager.UploadCallBack {
                    override fun onUploadSuccess(url: String) {
                        continuation.resume(url) {}
                    }
                    override fun onUploadFail() {
                        continuation.resumeWithException(Throwable("文件上传失败：$filePath"))
                    }
                })
                startZipUpload(filePath, title)
            }
        }
    }

    /**
     * 提交到云书库接口
     */
     suspend fun submitToCloudLibrary(cloudItem: CloudListBean): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val url = Constants.URL_BASE + "cloud/data/insert"
            val jsonBody = JSONObject().apply {
                val jsonArray = JSONArray()
                jsonArray.put(JSONObject(Gson().toJson(cloudItem)))
                put("listModel", jsonArray)
            }
            VolleyHttpManager.post(
                url = url,
                jsonBody = jsonBody,
                clazz = JSONObject::class.java,
                callback = object : VolleyCallback<JSONObject> {
                    override fun onSuccess(data: JSONObject) {
                        val code = data.optInt("code")
                        continuation.resume(code == 0) {}
                    }
                    override fun onError(errorMsg: String, errorCode: Int) {
                        continuation.resumeWithException(Exception(errorMsg))
                    }
                }
            )
        }
    }

}