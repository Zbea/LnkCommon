package com.bll.lnkcommon.ui.adapter

import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.JournalList
import com.bll.lnkcommon.utils.DateUtils
import com.bll.lnkcommon.utils.GlideUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class JournalAdapter(layoutResId: Int,  data: List<JournalList.JournalBean>?) : BaseQuickAdapter<JournalList.JournalBean, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: JournalList.JournalBean) {
        helper.apply {
            setText(R.id.tv_title,item.title)
            setText(R.id.tv_date,DateUtils.longToStringDataNoYear(item.date))
            GlideUtils.setImageRoundUrl(mContext,item.bodyUrl,getView(R.id.iv_image),15)
        }
    }

}
