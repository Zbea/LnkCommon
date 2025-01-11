package com.bll.lnkcommon.ui.adapter

import android.widget.ImageView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.WallpaperBean
import com.bll.lnkcommon.utils.GlideUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class WallpaperAdapter(layoutResId: Int, private val type:Int,data: List<WallpaperBean>?) : BaseQuickAdapter<WallpaperBean, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: WallpaperBean) {
        helper.apply {
            setText(R.id.tv_name,item.title)
            GlideUtils.setImageRoundUrl(mContext,item.bodyUrl,getView(R.id.iv_image),10)
            if (type==1){
                setText(R.id.tv_price,if (item.price==0) "免费" else item.price.toString()+"青豆")
                setText(R.id.btn_download,if (item.buyStatus==1) "下载" else "购买")
                addOnClickListener(R.id.btn_download)
            }
        }

    }

}
