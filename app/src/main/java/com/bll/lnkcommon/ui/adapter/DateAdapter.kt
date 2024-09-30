package com.bll.lnkcommon.ui.adapter

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Typeface.BOLD
import android.graphics.Typeface.defaultFromStyle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.Date
import com.bll.lnkcommon.utils.DateUtils
import com.bll.lnkcommon.utils.GlideUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import java.io.File

class DateAdapter(layoutResId: Int, data: List<Date>?) :
    BaseQuickAdapter<Date, BaseViewHolder>(layoutResId, data) {

    @SuppressLint("WrongConstant")
    override fun convert(helper: BaseViewHolder, item: Date) {
        val tvDay = helper.getView<TextView>(R.id.tv_day)
        val tvLunar=helper.getView<TextView>(R.id.tv_lunar)
        val ivImage=helper.getView<ImageView>(R.id.iv_image)
        val rlImage=helper.getView<RelativeLayout>(R.id.rl_image)
        tvDay.text = if (item.day == 0) "" else item.day.toString()
        if (item.isNow)
            tvDay.typeface = defaultFromStyle(BOLD)

        val str = if (!item.solar.solar24Term.isNullOrEmpty()) {
            item.solar.solar24Term
        } else {
            if (!item.solar.solarFestivalName.isNullOrEmpty()) {
                item.solar.solarFestivalName
            } else {
                if (!item.lunar.lunarFestivalName.isNullOrEmpty()) {
                    item.lunar.lunarFestivalName
                } else {
                    item.lunar.getChinaDayString(item.lunar.lunarDay)
                }
            }
        }
        tvLunar.text=str

        if (item.year!=0){
            val path= FileAddress().getPathDate(DateUtils.longToStringCalender(item.time))+"/draw.png"
            if (File(path).exists()){
                GlideUtils.setImageNoCacheUrl(mContext,path,ivImage)
                rlImage.visibility= View.VISIBLE
            }
            else{
                rlImage.visibility= View.GONE
            }
        }
        else{
            rlImage.visibility= View.GONE
        }
    }


}
