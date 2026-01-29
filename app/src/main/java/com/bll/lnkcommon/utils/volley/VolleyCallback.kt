/**
 * Volley通用请求回调接口
 * @param T 响应数据泛型类型
 */
interface VolleyCallback<T> {
    /**
     * 请求成功回调
     * @param data 解析后的响应数据
     */
    fun onSuccess(data: T)

    /**
     * 请求失败回调
     * @param errorMsg 错误信息
     * @param errorCode 错误码
     */
    fun onError(errorMsg: String, errorCode: Int = -1)

    /**
     * 请求完成回调
     */
    fun onComplete() {}
}

// 错误码常量定义
object VolleyErrorConst {
    const val ERROR_NETWORK = 1001 // 网络异常
    const val ERROR_TIMEOUT = 1002 // 超时异常
    const val ERROR_PARSE = 1003 // 数据解析异常
    const val ERROR_UNKNOWN = 1004 // 未知异常
}