package com.bll.lnkcommon.ui.adapter

import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.StudentBean
import com.bll.lnkcommon.mvp.model.TeacherHomeworkList.TeacherHomeworkBean
import com.bll.lnkcommon.utils.DateUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class TeacherHomeworkAdapter(layoutResId: Int, data: List<TeacherHomeworkBean>?,val type:Int) : BaseQuickAdapter<TeacherHomeworkBean, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: TeacherHomeworkBean) {
        helper.apply {
            setText(R.id.tv_status,when (item.status){ 1-> "通知" 2-> "提交" else ->"批改"})
            setText(R.id.tv_type,"(${item.subject}  ${item.homeworkName})")
            setText(R.id.tv_content,item.title)
            setText(R.id.tv_date,if (item.status==1)"" else "学生提交时间："+DateUtils.longToStringWeek(item.time))
            setText(R.id.tv_commitDate,if (item.submitTime==0L)"" else DateUtils.longToStringWeek(item.submitTime)+"提交")

            setGone(R.id.tv_image_content,!item.homeworkContent.isNullOrEmpty())
            when(item.status){
                1->{
                    setGone(R.id.tv_image_commit,false)
                    setGone(R.id.tv_image_correct,false)
                }
                2->{
                    setGone(R.id.tv_image_commit,true)
                    setGone(R.id.tv_image_correct,false)
                }
                3->{
                    setGone(R.id.tv_image_commit,true)
                    setGone(R.id.tv_image_correct,true)
                }
            }

            setVisible(R.id.iv_rank,type==2)
            setGone(R.id.tv_status,type==1)

            addOnClickListener(R.id.iv_delete,R.id.iv_rank,R.id.tv_image_content,R.id.tv_image_commit,R.id.tv_image_correct)
        }
    }

}
