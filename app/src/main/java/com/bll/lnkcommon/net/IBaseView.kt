package com.bll.lnkcommon.net

import io.reactivex.disposables.Disposable


interface IBaseView {
    fun addSubscription(d: Disposable)

    fun login()//登录失效，用于重新登录

    fun hideLoading()

    fun showLoading()

    fun fail(msg:String)

    /**
     * 加载出现错误
     * @param responeThrowable
     */
    fun onFailer(responeThrowable: ExceptionHandle.ResponeThrowable?)
    fun onComplete()//加载完成
}