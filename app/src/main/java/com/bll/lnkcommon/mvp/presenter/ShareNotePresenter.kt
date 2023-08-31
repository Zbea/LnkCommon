package com.bll.lnkcommon.mvp.presenter

import com.bll.lnkcommon.mvp.model.HomeworkCorrectList
import com.bll.lnkcommon.mvp.model.ShareNoteList
import com.bll.lnkcommon.mvp.model.TeacherHomeworkList
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.net.*

class ShareNotePresenter(view: IContractView.IShareNoteView):BasePresenter<IContractView.IShareNoteView>(view) {

    fun getToken(){
        val token = RetrofitManager.service.getQiniuToken()
        doRequest(token, object : Callback<String>(view) {
            override fun failed(tBaseResult: BaseResult<String>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<String>) {
                if (tBaseResult.data!=null)
                    view.onToken(tBaseResult.data)
            }
        }, true)
    }


    fun getShareNotes(map: HashMap<String,Any>,isShow:Boolean) {
        val grade = RetrofitManager.service.getShareList(map)
        doRequest(grade, object : Callback<ShareNoteList>(view) {
            override fun failed(tBaseResult: BaseResult<ShareNoteList>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<ShareNoteList>) {
                if (tBaseResult.data!=null)
                    view.onList(tBaseResult.data)
            }
        }, isShow)
    }

    fun deleteShareNote(map: HashMap<String,Any>) {
        val body=RequestUtils.getBody(map)
        val grade = RetrofitManager.service.deleteShare(body)
        doRequest(grade, object : Callback<Any>(view) {
            override fun failed(tBaseResult: BaseResult<Any>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<Any>) {
                view.onDeleteSuccess()
            }
        }, true)
    }

    fun commitShare(map:HashMap<String,Any>){
        val body= RequestUtils.getBody(map)
        val commit = RetrofitManager.service.shareFreeNote(body)
        doRequest(commit, object : Callback<Any>(view) {
            override fun failed(tBaseResult: BaseResult<Any>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<Any>) {
                view.onShare()
            }
        }, true)
    }

}