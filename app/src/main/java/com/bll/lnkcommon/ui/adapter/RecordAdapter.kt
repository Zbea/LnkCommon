package com.bll.lnkcommon.ui.adapter

import com.bll.lnkcommon.mvp.model.RecordBean
import com.bll.lnkcommon.R
import com.bll.lnkcommon.utils.DateUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class RecordAdapter(layoutResId: Int, data: MutableList<RecordBean>?) : BaseQuickAdapter<RecordBean, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: RecordBean) {
        helper.apply {
            setText(R.id.tv_title,item.title)
            setImageResource(R.id.iv_record,if (item.state==0) R.mipmap.icon_record_play else R.mipmap.icon_record_pause)
            addOnClickListener(R.id.iv_record,R.id.iv_setting)
        }
    }
}
