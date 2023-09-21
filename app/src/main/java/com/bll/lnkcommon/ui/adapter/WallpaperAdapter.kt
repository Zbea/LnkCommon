package com.bll.lnkcommon.ui.adapter

import android.widget.ImageView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.WallpaperBean
import com.bll.lnkcommon.utils.GlideUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class WallpaperAdapter(layoutResId: Int, data: List<WallpaperBean>?) : BaseQuickAdapter<WallpaperBean, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: WallpaperBean) {
        helper.apply {
            setText(R.id.tv_name,item.title)
            val image=getView<ImageView>(R.id.iv_image)
            GlideUtils.setImageRoundUrl(mContext,item.bodyUrl,image,5)
            if (item.id==null){
                setText(R.id.tv_price,if (item.price==0) "免费" else item.price.toString())
                setGone(R.id.tv_price_title,item.price!=0)
                setText(R.id.btn_download,if (item.buyStatus==1) "下载" else "购买")
                addOnClickListener(R.id.btn_download)
            }
        }

    }

}
