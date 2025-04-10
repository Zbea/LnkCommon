package com.bll.lnkcommon.net


import com.bll.lnkcommon.utils.SToast
import io.reactivex.Observer
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable

abstract class Callback<T> : Observer<BaseResult<T>> {

    private var IBaseView: IBaseView
    private var isShowToast=true

    constructor(IBaseView: IBaseView) {
        this.IBaseView = IBaseView
    }
    constructor(IBaseView: IBaseView, isShowToast:Boolean) {
        this.IBaseView = IBaseView
        this.isShowToast=isShowToast
    }

    override fun onSubscribe(@NonNull d: Disposable) {
        IBaseView.addSubscription(d)
    }

    override fun onNext(@NonNull tBaseResult: BaseResult<T>) {
        if (tBaseResult.code == 0) {
            success(tBaseResult)
        } else {
            when (tBaseResult.code) {
                -10 -> {
                    IBaseView.login()
                }
                else -> {
                    if (isShowToast)
                        IBaseView.fail(tBaseResult.msg)
                    failed(tBaseResult)
                }
            }
        }
    }

    override fun onComplete() {
        IBaseView.hideLoading()
    }

    override fun onError(@NonNull e: Throwable) {
        e.printStackTrace()
        if (isShowToast){
            when (ExceptionHandle.handleException(e).code) {
                ExceptionHandle.ERROR.NETWORD_ERROR-> {
                    SToast.showText("网络连接失败")
                }
                ExceptionHandle.ERROR.SERVER_TIMEOUT_ERROR -> {
                    SToast.showText("请求超时")
                }
                ExceptionHandle.ERROR.PARSE_ERROR -> {
                    SToast.showText("数据解析错误")
                }
                ExceptionHandle.ERROR.HTTP_ERROR -> {
                    SToast.showText("服务器连接失败")
                }
                else -> {
                    SToast.showText("服务器开小差，请重试")
                }
            }
        }
        IBaseView.hideLoading()
    }

    abstract fun failed(tBaseResult: BaseResult<T>): Boolean

    abstract fun success(tBaseResult: BaseResult<T>)
}