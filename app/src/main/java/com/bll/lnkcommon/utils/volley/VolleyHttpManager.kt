
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkError
import com.android.volley.NoConnectionError
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bll.lnkcommon.MethodManager
import com.google.android.exoplayer2.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParseException
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.StandardCharsets

/**
 * Volley网络请求工具类（单例模式）
 * 封装通用GET/POST请求，统一处理回调、解析、线程切换
 */
object VolleyHttpManager {
    private const val VOLLEY_LOG_TAG = "VolleyHttpDebug" // 日志标签
    private const val LOG_DIVIDER = "==============================" // 日志分隔符，便于区分不同请求

    // 全局请求队列
    private lateinit var requestQueue: RequestQueue
    // Gson实例
    private val gson by lazy { Gson() }
    // 主线程Handler
    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }
    // 公共请求头
    private val commonHeaders by lazy { HashMap<String, String>() }

    /**
     * 初始化Volley工具类
     * @param context
     */
    fun init(context: Context) {
        if (!::requestQueue.isInitialized) {
            requestQueue = Volley.newRequestQueue(context.applicationContext)
            initCommonHeaders()
        }
    }

    /**
     * 初始化公共请求头（可根据业务需求扩展）
     */
    private fun initCommonHeaders() {
        commonHeaders["Content-Type"] = "application/json; charset=utf-8"
        commonHeaders["Accept"] = "application/json"
        commonHeaders["Authorization"] = MethodManager.getToken()
    }

    /**
     * 刷新公共请求头中的Token
     * @param token 新的Token
     */
    fun refreshAuthorizationToken(token: String) {
        commonHeaders["Authorization"] = token
    }

    /**
     * 通用GET请求（
     * @param url 请求URL
     * @param clazz 响应数据的Class类型（用于Gson解析）
     * @param callback 回调接口
     * @param needCommonHeaders 是否需要携带公共请求头
     */
    fun <T> get(url: String, clazz: Class<T>, callback: VolleyCallback<T>, needCommonHeaders: Boolean = true) {
        val stringRequest = object : StringRequest(
            Method.GET, url, Response.Listener { response ->
                printSuccessResponseLog(Method.GET, url, response)
                handleSuccessResponse(response, clazz, callback)
            },
            Response.ErrorListener { error ->
                handleErrorResponse(error, callback)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = if (needCommonHeaders) {
                    HashMap(commonHeaders)
                } else {
                    HashMap()
                }
                printRequestLog(Method.GET, url, headers, null)
                return headers
            }

            // 配置超时时间
            override fun getRetryPolicy(): RetryPolicy {
                return DefaultRetryPolicy(
                    5000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES, // 默认重试次数
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
            }
        }

        // 添加请求到队列
        addRequestToQueue(stringRequest, url)
    }

    /**
     * 通用POST请求
     * @param url 请求URL
     * @param jsonBody 请求体（JSONObject）
     * @param clazz 响应数据的Class类型
     * @param callback 回调接口
     * @param needCommonHeaders 是否需要携带公共请求头
     */
    fun <T> post(url: String, jsonBody: JSONObject?, clazz: Class<T>, callback: VolleyCallback<T>, needCommonHeaders: Boolean = true) {
        // 创建JsonObjectRequest（POST方式）
        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST, url, jsonBody ?: JSONObject(),
            Response.Listener { response ->
                printSuccessResponseLog(Request.Method.POST, url, response.toString())
                handleSuccessResponse(response.toString(), clazz, callback)
            },
            Response.ErrorListener { error ->
                handleErrorResponse(error, callback)
            }
        ) {
            // 配置请求头
            override fun getHeaders(): MutableMap<String, String> {
                val headers = if (needCommonHeaders) {
                    HashMap(commonHeaders)
                } else {
                    HashMap()
                }
                printRequestLog(Request.Method.POST, url, headers, jsonBody)
                return headers
            }

            // 配置超时时间
            override fun getRetryPolicy(): RetryPolicy {
                return DefaultRetryPolicy(
                    5000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
            }
        }

        // 添加请求到队列
        addRequestToQueue(jsonObjectRequest, url)
    }

    private fun <T> handleSuccessResponse(response: String, clazz: Class<T>, callback: VolleyCallback<T>) {
        mainHandler.post { // 切换到UI线程执行回调
            try {
                val data = if (clazz == JSONObject::class.java) {
                    JSONObject(response) as T
                } else {
                    gson.fromJson(response, clazz)
                }
                callback.onSuccess(data)
            } catch (e: Exception) {
                // 捕获解析异常
                val errorMsg = when (e) {
                    is JsonParseException, is JSONException -> "数据解析失败"
                    else -> "未知解析错误"
                }
                Log.e(VOLLEY_LOG_TAG, "$LOG_DIVIDER\n解析失败：$errorMsg\n响应数据：$response\n$LOG_DIVIDER")
                callback.onError(errorMsg, VolleyErrorConst.ERROR_PARSE)
            } finally {
                callback.onComplete()
            }
        }
    }

    private fun <T> handleErrorResponse(error: VolleyError, callback: VolleyCallback<T>) {
        mainHandler.post {
            val (errorMsg, errorCode) = when (error) {
                is NoConnectionError -> {
                    val causeMsg = error.cause?.message ?: "请检查网络连接"
                    "网络连接失败：$causeMsg" to VolleyErrorConst.ERROR_NETWORK
                }
                is TimeoutError -> {
                    val causeMsg = error.cause?.message ?: "请求超时"
                    "请求超时：$causeMsg，请稍后重试" to VolleyErrorConst.ERROR_TIMEOUT
                }
                is ParseError -> {
                    "数据解析失败" to VolleyErrorConst.ERROR_PARSE
                }
                is ServerError -> {
                    val errorBody = error.networkResponse?.data?.toString(StandardCharsets.UTF_8)
                    val statusCode = error.networkResponse?.statusCode ?: -1
                    Log.e(VOLLEY_LOG_TAG, "$LOG_DIVIDER\n服务端错误：\n状态码：$statusCode\n错误信息：$errorBody\n$LOG_DIVIDER")
                    "服务端错误：${errorBody ?: "未知错误"}" to statusCode
                }
                is AuthFailureError -> {
                    Log.e(VOLLEY_LOG_TAG, "$LOG_DIVIDER\n认证失败：${error.message ?: "Token过期或无效"}\n$LOG_DIVIDER")
                    "认证失败，请重新登录" to -2
                }
                is NetworkError -> {
                    "网络异常，请检查网络状态" to VolleyErrorConst.ERROR_NETWORK
                }
                else -> {
                    val errorMessage = error.message ?: "未知请求错误"
                    Log.e(VOLLEY_LOG_TAG, "$LOG_DIVIDER\n未知错误：\n错误信息：$errorMessage\n异常详情：${error.stackTraceToString()}\n$LOG_DIVIDER")
                    "请求失败：$errorMessage" to VolleyErrorConst.ERROR_UNKNOWN
                }
            }
            // 回调错误信息和完成事件
            callback.onError(errorMsg, errorCode)
            callback.onComplete()
        }
    }

    private fun addRequestToQueue(request: Request<*>, tag: String) {
        request.tag = tag
        if (::requestQueue.isInitialized) {
            requestQueue.add(request)
        } else {
            throw IllegalStateException("VolleyHttpManager 未初始化，请先调用 init() 方法")
        }
    }

    /**
     * 取消指定标签的所有请求（例如Activity销毁时取消未完成的请求）
     * @param tag 请求标签
     */
    fun cancelRequestsByTag(tag: String) {
        if (::requestQueue.isInitialized) {
            requestQueue.cancelAll(tag)
        }
    }

    /**
     * 取消所有请求
     */
    fun cancelAllRequests() {
        if (::requestQueue.isInitialized) {
            requestQueue.cancelAll { true }
        }
    }

    /**
     * 打印完整请求信息
     * @param method 请求方式（GET/POST）
     * @param url 请求URL
     * @param headers 请求头
     * @param requestBody 请求体（仅POST有，GET为null）
     */
    private fun printRequestLog(method: Int, url: String, headers: Map<String, String>, requestBody: JSONObject?) {
        val methodStr = if (method == Request.Method.GET) "GET" else "POST"
        val logBuilder = StringBuilder()
            .append(LOG_DIVIDER)
            .append("\n【请求开始】")
            .append("\n请求方式：$methodStr")
            .append("\n请求URL：$url")
            .append("\n请求头：")
        // 拼接请求头
        headers.forEach { (key, value) ->
            logBuilder.append("\n  $key: $value")
        }
        // 拼接请求体（仅POST）
        if (requestBody != null) {
            logBuilder.append("\n请求体：${requestBody.toString(4)}") // toString(4) 格式化JSON，带缩进，更易读
        }
        logBuilder.append("\n$LOG_DIVIDER")

        // 打印请求日志（使用d级别，调试时可见）
        Log.d(VOLLEY_LOG_TAG, logBuilder.toString())
    }

    /**
     * 打印成功响应信息
     * @param method 请求方式（GET/POST）
     * @param url 请求URL
     * @param response 响应数据
     */
    private fun printSuccessResponseLog(method: Int, url: String, response: String) {
        val methodStr = if (method == Request.Method.GET) "GET" else "POST"
        val logContent = StringBuilder()
            .append(LOG_DIVIDER)
            .append("\n【响应成功】")
            .append("\n请求方式：$methodStr")
            .append("\n请求URL：$url")
            .append("\n响应数据：${formatJson(response)}") // 格式化JSON响应，更易读
            .append("\n$LOG_DIVIDER")

        // 打印成功响应日志（使用d级别）
        Log.d(VOLLEY_LOG_TAG, logContent.toString())
    }

    /**
     * 格式化JSON字符串，带缩进，提升可读性
     * @param jsonStr 原始JSON字符串
     * @return 格式化后的JSON字符串
     */
    private fun formatJson(jsonStr: String): String {
        return try {
            when {
                jsonStr.startsWith("{") -> JSONObject(jsonStr).toString(4)
                jsonStr.startsWith("[") -> JSONArray(jsonStr).toString(4)
                else -> jsonStr
            }
        } catch (e: Exception) {
            // 非JSON格式，直接返回原字符串
            jsonStr
        }
    }
}