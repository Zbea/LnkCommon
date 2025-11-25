package com.bll.lnkcommon.ui.adapter.teaching

import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.teaching.ExamList
import com.bll.lnkcommon.utils.DateUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class StudentExamAdapter(layoutResId: Int, data: List<ExamList.ExamBean>?) : BaseQuickAdapter<ExamList.ExamBean, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: ExamList.ExamBean) {
        helper.apply {
            setText(R.id.tv_status,DataBeanManager.getCourseStr(item.subject))
            setGone(R.id.tv_type,false)
            setText(R.id.tv_content,item.examName)
            setText(R.id.tv_commitTime,"提交时间："+DateUtils.longToStringWeek(item.expTime))
            setText(R.id.tv_correctTime,"批改时间："+DateUtils.longToStringWeek(DateUtils.dateStrToLong(item.createTime)))
            setText(R.id.tv_startTime,if(DateUtils.dateStrToLong(item.startTime)<=0L)"" else "布置时间："+DateUtils.longToStringWeek(DateUtils.dateStrToLong(item.startTime)))
            addOnClickListener(R.id.iv_delete,R.id.iv_rank)
        }
    }

}
