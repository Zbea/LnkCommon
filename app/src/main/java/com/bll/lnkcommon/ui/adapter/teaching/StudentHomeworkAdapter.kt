package com.bll.lnkcommon.ui.adapter.teaching

import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.teaching.TeacherHomeworkList.TeacherHomeworkBean
import com.bll.lnkcommon.utils.DateUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class StudentHomeworkAdapter(layoutResId: Int, data: List<TeacherHomeworkBean>?, val type:Int) : BaseQuickAdapter<TeacherHomeworkBean, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: TeacherHomeworkBean) {
        helper.apply {
            setText(R.id.tv_status,"${item.subject}  ${if(type==1) when (item.status){ 1-> "通知" 2-> "提交" else ->"批改"} else ""}")
            setText(R.id.tv_type,item.typeName)
            setText(R.id.tv_content,item.title)
            val commitTimeStr=if (item.submitTime==0L)"" else "提交截止："+DateUtils.longToStringWeek(item.submitTime)
            setText(R.id.tv_commitTime,if (item.status==1)commitTimeStr else "学生提交："+DateUtils.longToStringWeek(item.time))
            setText(R.id.tv_startTime, "布置时间："+DateUtils.longToStringWeek(DateUtils.dateStrToLong(item.createTime)))
            setText(R.id.tv_correctTime, if(DateUtils.dateStrToLong(item.correctTime)<=0L)"" else "批改时间："+DateUtils.longToStringWeek(DateUtils.dateStrToLong(item.correctTime)))
            setGone(R.id.iv_rank,item.type==2)

            addOnClickListener(R.id.iv_delete,R.id.iv_rank)
        }
    }

}
