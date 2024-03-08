package com.bll.lnkcommon.ui.adapter

import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.PermissionTimeBean
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class PermissionTimeAdapter(layoutResId: Int, data: List<PermissionTimeBean>?) : BaseQuickAdapter<PermissionTimeBean, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: PermissionTimeBean) {
        helper.apply {
            setText(R.id.tv_time,item.timeStr)
            var weekStr=""
            for (i in item.weeks){
               weekStr=weekStr+DataBeanManager.getWeekStr(i)+"  "
            }
            setText(R.id.tv_week,weekStr)
            addOnClickListener(R.id.iv_delete)
        }
    }

}
