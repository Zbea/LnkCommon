package com.bll.lnkcommon.ui.adapter

import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.CalenderItemBean
import com.bll.lnkcommon.utils.GlideUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class CalenderListAdapter(layoutResId: Int,private var type:Int, data: List<CalenderItemBean>?) : BaseQuickAdapter<CalenderItemBean, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: CalenderItemBean) {
        helper.apply {
            setText(R.id.tv_name,item.title)
            GlideUtils.setImageRoundUrl(mContext,item.imageUrl,getView(R.id.iv_image),10)
            if (type==0){
                setText(R.id.tv_buy,if (item.buyStatus==1) "下载" else "购买")
                addOnClickListener(R.id.tv_buy)
            }
        }
    }

}
