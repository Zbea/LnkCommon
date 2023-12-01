package com.bll.lnkcommon.ui.adapter

import android.widget.ImageView
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.HomeworkTypeList
import com.bll.lnkcommon.utils.GlideUtils
import com.bll.lnkcommon.utils.ToolUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class HomeworkTypeAdapter(layoutResId: Int, data: List<HomeworkTypeList.HomeworkTypeBean>?) : BaseQuickAdapter<HomeworkTypeList.HomeworkTypeBean, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: HomeworkTypeList.HomeworkTypeBean) {
        helper.apply {
            if (item.type==1){
                setText(R.id.tv_name,item.name)
                if (DataBeanManager.courses.size>0)
                    setText(R.id.tv_course,"(${DataBeanManager.courses[item.subject-1].desc})")
                setImageResource(R.id.iv_image,R.mipmap.icon_homework_cover_1)
                setBackgroundRes(R.id.rl_bg,R.color.color_transparent)
            }
            else{
                GlideUtils.setImageRoundUrl(mContext, item.imageUrl, helper.getView(R.id.iv_image), 10)
                setBackgroundRes(R.id.rl_bg,R.drawable.bg_black_stroke_5dp_corner)
            }
        }
    }

}
