package com.bll.lnkcommon.net.system


import com.bll.lnkcommon.net.ExceptionHandle
import com.bll.lnkcommon.net.IBaseView
import com.bll.lnkcommon.utils.SToast
import io.reactivex.Observer
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable

abstract class Callback1<T> : Observer<BaseResult1<T>> {

    private var IBaseView: IBaseView
    private var screen=0
    private var isComplete=true

    constructor(IBaseView: IBaseView) {
        this.IBaseView = IBaseView
    }

    override fun onSubscribe(@NonNull d: Disposable) {
        IBaseView.addSubscription(d)
    }

    override fun onNext(@NonNull tBaseResult: BaseResult1<T>) {
        if (!tBaseResult.Error.isNullOrEmpty()) {
            IBaseView.fail(tBaseResult.Error)
            return
        }
        if (tBaseResult.Code == 200) {
            success(tBaseResult)
        } else {
            when (tBaseResult.Code) {
                -10 -> {
                    IBaseView.login()
                }
                else -> {
                    IBaseView.fail(tBaseResult.Error)
                    failed(tBaseResult)
                }
            }
        }
    }

    override fun onComplete() {
        if (isComplete)
            IBaseView.hideLoading()
    }

    override fun onError(@NonNull e: Throwable) {
        e.printStackTrace()

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
        IBaseView.hideLoading()
    }

    abstract fun failed(tBaseResult: BaseResult1<T>): Boolean

    abstract fun success(tBaseResult: BaseResult1<T>)
}