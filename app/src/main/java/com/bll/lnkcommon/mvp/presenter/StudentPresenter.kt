package com.bll.lnkcommon.mvp.presenter

import com.bll.lnkcommon.mvp.model.SchoolBean
import com.bll.lnkcommon.mvp.model.Score
import com.bll.lnkcommon.mvp.model.StudentBean
import com.bll.lnkcommon.mvp.model.TeacherHomeworkList
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.net.*


class StudentPresenter(view: IContractView.IStudentView) : BasePresenter<IContractView.IStudentView>(view) {

    fun getStudents() {
        val grade = RetrofitManager.service.onStudentList()
        doRequest(grade, object : Callback<MutableList<StudentBean>>(view) {
            override fun failed(tBaseResult: BaseResult<MutableList<StudentBean>>): Boolean {
                return false
            }
            override fun success(tBaseResult: BaseResult<MutableList<StudentBean>>) {
                if (!tBaseResult.data.isNullOrEmpty())
                    view.onListStudents(tBaseResult.data)
            }
        }, false)
    }

}