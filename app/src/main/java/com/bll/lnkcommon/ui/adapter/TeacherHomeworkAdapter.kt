package com.bll.lnkcommon.ui.adapter

import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.TeacherHomeworkList.TeacherHomeworkBean
import com.bll.lnkcommon.utils.DateUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class TeacherHomeworkAdapter(layoutResId: Int, data: List<TeacherHomeworkBean>?,val type:Int) : BaseQuickAdapter<TeacherHomeworkBean, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: TeacherHomeworkBean) {
        helper.apply {
            setText(R.id.tv_status,"${item.subject}  ${if(type==1) when (item.status){ 1-> "通知" 2-> "提交" else ->"批改"} else ""}")
            setText(R.id.tv_type,item.homeworkName)
            setText(R.id.tv_content,item.title+"  "+if (item.submitTime==0L)"" else DateUtils.longToStringWeek(item.submitTime)+"提交")
            setText(R.id.tv_commitTime,if (item.status==1)"" else "学生提交时间："+DateUtils.longToStringWeek(item.time))
            setText(R.id.tv_startTime, "布置时间："+DateUtils.longToStringWeek(DateUtils.dateStrToLong(item.createTime)))
            setText(R.id.tv_correctTime, if(DateUtils.dateStrToLong(item.correctTime)<=0L)"" else "批改下发时间："+DateUtils.longToStringWeek(DateUtils.dateStrToLong(item.correctTime)))
            setGone(R.id.iv_rank,item.type==2)

            addOnClickListener(R.id.iv_delete,R.id.iv_rank)
        }
    }

}
