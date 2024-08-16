package com.bll.lnkcommon.ui.adapter

import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.ExamList
import com.bll.lnkcommon.utils.DateUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class ExamAdapter(layoutResId: Int, data: List<ExamList.ExamBean>?) : BaseQuickAdapter<ExamList.ExamBean, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: ExamList.ExamBean) {
        helper.apply {
            setText(R.id.tv_status,"批改")
            setText(R.id.tv_type,"(${DataBeanManager.getCourseStr(item.subject)})")
            setText(R.id.tv_content,item.examName)
            setText(R.id.tv_date,"考试时间："+DateUtils.longToStringWeek(item.expTime))
            setGone(R.id.tv_answer,!item.answerUrl.isNullOrEmpty())
            addOnClickListener(R.id.iv_delete,R.id.tv_rank,R.id.tv_answer)
        }
    }

}
