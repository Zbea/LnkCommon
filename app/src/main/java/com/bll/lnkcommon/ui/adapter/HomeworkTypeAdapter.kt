package com.bll.lnkcommon.ui.adapter

import android.widget.ImageView
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.HomeworkTypeList
import com.bll.lnkcommon.utils.GlideUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class HomeworkTypeAdapter(layoutResId: Int, data: List<HomeworkTypeList.HomeworkTypeBean>?) : BaseQuickAdapter<HomeworkTypeList.HomeworkTypeBean, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: HomeworkTypeList.HomeworkTypeBean) {
        helper.apply {
            setText(R.id.tv_name,item.name)
            if (DataBeanManager.courses.size>0)
                setText(R.id.tv_course,DataBeanManager.courses[item.subject-1].desc)
            if (item.type==1){
                helper.setImageResource(R.id.iv_image,DataBeanManager.homeworkCoverId())
            }
            else{
                GlideUtils.setImageRoundUrl(mContext,item.imageUrl,helper.getView(R.id.iv_image),10)
            }
        }
    }

}
