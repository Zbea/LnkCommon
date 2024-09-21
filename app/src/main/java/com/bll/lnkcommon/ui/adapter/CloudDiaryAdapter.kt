package com.bll.lnkcommon.ui.adapter

import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.CloudListBean
import com.bll.lnkcommon.mvp.model.DiaryBean
import com.bll.lnkcommon.mvp.model.Note
import com.bll.lnkcommon.utils.DateUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class CloudDiaryAdapter(layoutResId: Int, data: List<CloudListBean>?) : BaseQuickAdapter<CloudListBean, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: CloudListBean) {
        helper.setText(R.id.tv_title,item.subTypeStr)
        helper.setText(R.id.tv_date, DateUtils.longToStringDataNoHour(item.date))
        helper.addOnClickListener(R.id.iv_delete)
    }

}
