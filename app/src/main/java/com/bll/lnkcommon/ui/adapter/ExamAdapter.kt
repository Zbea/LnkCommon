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
            setText(R.id.tv_type,"(${item.subject})")
            setText(R.id.tv_content,item.examName)
            setText(R.id.tv_date,"考试时间："+DateUtils.longToStringWeek(item.expTime))

            setGone(R.id.tv_image_content,!item.examUrl.isNullOrEmpty())
            setGone(R.id.tv_image_commit,!item.studentUrl.isNullOrEmpty())
            setGone(R.id.tv_image_correct,!item.teacherUrl.isNullOrEmpty())

            addOnClickListener(R.id.iv_rank,R.id.tv_image_content,R.id.tv_image_commit,R.id.tv_image_correct)
        }
    }

}
