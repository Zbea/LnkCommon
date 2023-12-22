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
            if (type==1){
                setGone(R.id.cb_check,true)
                setImageResource(R.id.cb_check,if (item.isCheck) R.mipmap.icon_check_select else R.mipmap.icon_check_nor)
                addOnClickListener(R.id.ll_name)
            }
        }
    }

}
