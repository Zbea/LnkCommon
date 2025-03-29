package com.bll.lnkcommon.ui.adapter

import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.CalenderItemBean
import com.bll.lnkcommon.mvp.model.CloudListBean
import com.bll.lnkcommon.mvp.model.WallpaperBean
import com.bll.lnkcommon.utils.GlideUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class CalenderMyAdapter(layoutResId: Int, data: List<CalenderItemBean>?) : BaseQuickAdapter<CalenderItemBean, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: CalenderItemBean) {
        helper.apply {
            setText(R.id.cb_check,"  "+item.title)
            setChecked(R.id.cb_check,item.isCheck)
            GlideUtils.setImageRoundUrl(mContext,item.imageUrl,getView(R.id.iv_image),8)
            addOnClickListener(R.id.cb_check)
        }
    }

}
