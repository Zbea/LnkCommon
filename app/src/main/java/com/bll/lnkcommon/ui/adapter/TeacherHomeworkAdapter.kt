package com.bll.lnkcommon.ui.adapter

import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.StudentBean
import com.bll.lnkcommon.mvp.model.TeacherHomeworkList.TeacherHomeworkBean
import com.bll.lnkcommon.utils.DateUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class TeacherHomeworkAdapter(layoutResId: Int, data: List<TeacherHomeworkBean>?) : BaseQuickAdapter<TeacherHomeworkBean, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: TeacherHomeworkBean) {
        helper.apply {
            setText(R.id.tv_status,when (item.status){ 1-> "通知" 2-> "提交" else ->"批改"})
            setText(R.id.tv_type,"(${item.course}  ${item.typeStr})")
            setText(R.id.tv_content,item.content)
            setText(R.id.tv_date,if (item.date==0L)"" else "提交时间:"+DateUtils.longToStringWeek(item.date))
            setText(R.id.tv_commitDate,if (item.commitDate==0L)"" else DateUtils.longToStringWeek(item.commitDate)+"提交")

            if (item.type==1){
                setGone(R.id.tv_image_content,true)
            }
            else{
                setGone(R.id.tv_image_content,false)
            }
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
        }
    }

}
