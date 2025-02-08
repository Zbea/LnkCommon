package com.bll.lnkcommon.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.AreaBean
import com.bll.lnkcommon.mvp.model.PopupBean
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

/**
 * 单选弹框
 */
class PopupCityList(val context:Context,val view: View, val width:Int) {

    private var mPopupWindow:PopupWindow?=null
    private var provinces= mutableListOf<AreaBean>()
    private var provincePops= mutableListOf<PopupBean>()
    private var cityPops= mutableListOf<PopupBean>()

    fun builder(): PopupCityList{
        val popView = LayoutInflater.from(context).inflate(R.layout.popup_list_city, null, false)
        mPopupWindow = PopupWindow(context).apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            // 设置PopupWindow的内容view
            contentView=popView
            isFocusable=true // 设置PopupWindow可获得焦点
            isTouchable=true // 设置PopupWindow可触摸
            isOutsideTouchable=true // 设置非PopupWindow区域可触摸
            width=this@PopupCityList.width*2
            isClippingEnabled = false
        }

        provinces = MethodManager.getProvinces(context)

        for (i in provinces.indices){
            provincePops.add(PopupBean(i,provinces[i].value,i==0))
        }
        val citys=provinces[0].children
        for (i in citys.indices){
            cityPops.add(PopupBean(i,citys[i].value,false))
        }

        val rvList=popView.findViewById<RecyclerView>(R.id.rv_list)
        rvList.layoutManager = LinearLayoutManager(context)//创建布局管理
        val mAdapter = MAdapter(R.layout.item_popwindow_list, provincePops)
        rvList.adapter = mAdapter
        mAdapter.bindToRecyclerView(rvList)

        val rvListCity=popView.findViewById<RecyclerView>(R.id.rv_list_city)
        rvListCity.layoutManager = LinearLayoutManager(context)//创建布局管理
        val mAdapterCity = MAdapter(R.layout.item_popwindow_list, cityPops)
        rvListCity.adapter = mAdapterCity
        mAdapterCity.bindToRecyclerView(rvListCity)


        mAdapter.setOnItemClickListener { adapter, view, position ->
            for (item in provincePops) {
                item.isCheck=false
            }
            provincePops[position].isCheck=true
            mAdapter.notifyDataSetChanged()

            cityPops.clear()
            val citys=provinces[position].children
            for (i in citys.indices){
                cityPops.add(PopupBean(i,citys[i].value,false))
            }
            mAdapterCity.setNewData(cityPops)
        }

        mAdapterCity.setOnItemClickListener { adapter, view, position ->
            for (item in cityPops) {
                item.isCheck=false
            }
            cityPops[position].isCheck=true
            mAdapterCity.notifyDataSetChanged()
            if (onSelectListener!=null)
                onSelectListener?.onSelect(cityPops[position])
            dismiss()
        }

        show()
        return this
    }

    fun dismiss() {
        if (mPopupWindow != null) {
            mPopupWindow?.dismiss()
        }
    }

    fun show() {
        if (mPopupWindow != null) {
            mPopupWindow?.showAsDropDown(view, 0, 0,Gravity.START)
        }
    }

   private var onSelectListener:OnSelectListener?=null

    fun setOnSelectListener(onSelectListener:OnSelectListener)
    {
        this.onSelectListener=onSelectListener
    }

    fun interface OnSelectListener{
        fun onSelect(item: PopupBean)
    }


    private class MAdapter(layoutResId: Int, data: List<PopupBean>?) : BaseQuickAdapter<PopupBean, BaseViewHolder>(layoutResId, data) {

        override fun convert(helper: BaseViewHolder, item: PopupBean) {
            helper.setText(R.id.tv_name,item.name)
            helper.setVisible(R.id.iv_check,item.isCheck)
        }

    }

}