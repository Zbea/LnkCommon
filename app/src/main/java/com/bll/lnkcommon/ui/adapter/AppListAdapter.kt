package com.bll.lnkcommon.ui.adapter

import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.AppBean
import com.bll.lnkcommon.utils.BitmapUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class AppListAdapter(layoutResId: Int, val type:Int,data: List<AppBean>?) : BaseQuickAdapter<AppBean, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: AppBean) {
        helper.apply {
            setText(R.id.tv_name,item.appName)
            setImageDrawable(R.id.iv_image, BitmapUtils.byteToDrawable(item.imageByte))
            if (type==0){
                setGone(R.id.cb_check,!item.isBase)
                setChecked(R.id.cb_check,item.isCheck)
                addOnClickListener(R.id.cb_check)
            }

        }
    }

}
