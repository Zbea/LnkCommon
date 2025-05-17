package com.bll.lnkcommon.ui.adapter

import android.widget.ImageView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.utils.GlideUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import java.io.File

class DocumentAdapter(layoutResId: Int, data: List<File>?) : BaseQuickAdapter<File, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, file: File) {
        helper.setText(R.id.tv_name,file.name)
        val ivImage=helper.getView<ImageView>(R.id.iv_image)
        if (FileUtils.getUrlFormat(file.path).equals(".png")|| FileUtils.getUrlFormat(file.path).equals(".jpg")|| FileUtils.getUrlFormat(file.path).equals(".jpeg")){
            GlideUtils.setImageRoundUrl(mContext,file.path,ivImage,8)
            ivImage.setBackgroundResource(R.drawable.bg_black_stroke_5dp_corner)
        }
        else{
            ivImage.setImageResource(R.mipmap.icon_document_bg)
            ivImage.setBackgroundResource(R.color.color_transparent)
        }
    }

}
