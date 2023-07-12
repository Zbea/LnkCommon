package com.bll.lnkcommon.net


import com.bll.lnkcommon.MyApplication
import com.bll.lnkcommon.R
import com.bll.lnkcommon.utils.SToast
import io.reactivex.Observer
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable

abstract class Callback<T>(private var IBaseView: IBaseView) : Observer<BaseResult<T>> {


    override fun onSubscribe(@NonNull d: Disposable) {
        IBaseView.addSubscription(d)
    }

    override fun onNext(@NonNull tBaseResult: BaseResult<T>) {
        if (!tBaseResult.error.isNullOrEmpty()) {
            IBaseView.fail(tBaseResult.error)
            return
        }
        if (tBaseResult.code == 0) {
            success(tBaseResult)
        } else {
            when (tBaseResult.code) {
                -10 -> {
                    IBaseView.login()
                }
                else -> {
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

        val code = ExceptionHandle.handleException(e).code
        if (code == ExceptionHandle.ERROR.UNKONW_HOST_EXCEPTION) {
            SToast.showText(MyApplication.mContext.getString(R.string.net_work_error))
        } else if (code == ExceptionHandle.ERROR.NETWORD_ERROR || code == ExceptionHandle.ERROR.SERVER_ADDRESS_ERROR) {
            SToast.showText(MyApplication.mContext.getString(R.string.connect_server_timeout))
        } else if (code == ExceptionHandle.ERROR.PARSE_ERROR) {
            SToast.showText(MyApplication.mContext.getString(R.string.parse_data_error))
        } else if (code == ExceptionHandle.ERROR.HTTP_ERROR) {
            SToast.showText(MyApplication.mContext.getString(R.string.connect_error))
        }else if(code==401)
        {
            IBaseView.login()
        }
        else {
            SToast.showText(MyApplication.mContext.getString(R.string.on_server_error))
        }
        IBaseView.hideLoading()
    }

    abstract fun failed(tBaseResult: BaseResult<T>): Boolean

    abstract fun success(tBaseResult: BaseResult<T>)
}