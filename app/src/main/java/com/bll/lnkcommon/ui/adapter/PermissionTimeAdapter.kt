package com.bll.lnkcommon.ui.adapter

import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.PermissionTimeBean
import com.bll.lnkcommon.utils.DateUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class PermissionTimeAdapter(layoutResId: Int, data: List<PermissionTimeBean>?) : BaseQuickAdapter<PermissionTimeBean, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: PermissionTimeBean) {
        helper.apply {
            setText(R.id.tv_time,DateUtils.longToHour(DateUtils.getStartOfDayInMillis()+item.startTime)
            +"~"+DateUtils.longToHour(DateUtils.getStartOfDayInMillis()+item.endTime)
            )
            var weekStr=""
            for (i in item.weeks.split(",")){
               weekStr=weekStr+DataBeanManager.getWeekStr(i.toInt())+"  "
            }
            setText(R.id.tv_week, "$weekStr 不能查看")
            addOnClickListener(R.id.iv_delete)
        }
    }

}
