package com.bll.lnkcommon.ui.adapter

import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.WallpaperBean
import com.bll.lnkcommon.utils.GlideUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class WallpaperMyAdapter(layoutResId: Int, data: List<WallpaperBean>?) : BaseQuickAdapter<WallpaperBean, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: WallpaperBean) {
        helper.apply {
            setText(R.id.cb_check,"  "+item.title)
            setChecked(R.id.cb_check,item.isCheck)
            GlideUtils.setImageRoundUrl(mContext,item.path,getView(R.id.iv_image),8)
            addOnClickListener(R.id.cb_check)
        }
    }

}
