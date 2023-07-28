package com.bll.lnkcommon.ui.adapter

import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.HomeworkCorrectList.CorrectBean
import com.bll.lnkcommon.utils.DateUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class HomeworkCorrectAdapter(layoutResId: Int, data: List<CorrectBean>?) : BaseQuickAdapter<CorrectBean, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: CorrectBean) {
        helper.apply {
            if (DataBeanManager.courses.size>0)
                setText(R.id.tv_type,"(${DataBeanManager.courses[item.subject-1].desc}  ${item.homeworkName})")
            setText(R.id.tv_content,item.content)
            setText(R.id.tv_date,if (item.submitTime==0L)"" else "学生提交时间："+DateUtils.longToStringWeek(item.submitTime))
            setText(R.id.tv_date_commit,if (item.endTime==0L)"" else "要求时间："+DateUtils.longToStringWeek(item.endTime))
            setText(R.id.tv_date_create,"布置时间："+DateUtils.longToStringWeek(item.time))

            setGone(R.id.tv_send,item.status==3)

            addOnClickListener(R.id.iv_delete,R.id.tv_send)
        }
    }

}
