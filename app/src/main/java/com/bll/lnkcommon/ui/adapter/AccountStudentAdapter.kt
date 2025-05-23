package com.bll.lnkcommon.ui.adapter

import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.StudentBean
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class AccountStudentAdapter(layoutResId: Int,data: List<StudentBean>?) : BaseQuickAdapter<StudentBean, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: StudentBean) {
        helper.apply {
            setText(R.id.tv_student_id,item.account)
            setText(R.id.tv_student_name,item.nickname)
            setText(R.id.tv_student_school,item.schoolName)
            addOnClickListener(R.id.tv_student_cancel,R.id.tv_set)
        }
    }

}
