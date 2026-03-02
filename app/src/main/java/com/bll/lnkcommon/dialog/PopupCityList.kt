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
import com.bll.lnkcommon.base.BasePopupWindow
import com.bll.lnkcommon.mvp.model.AreaBean
import com.bll.lnkcommon.mvp.model.PopupBean
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder


class PopupCityList(
    context: Context,
    anchorView: View,
    width: Int
) : BasePopupWindow(context, anchorView, width * 2) {

    private var provinces = mutableListOf<AreaBean>()
    private var provincePops = mutableListOf<PopupBean>()
    private var cityPops = mutableListOf<PopupBean>()
    private lateinit var provinceAdapter: BaseQuickAdapter<PopupBean, BaseViewHolder>
    private lateinit var cityAdapter: BaseQuickAdapter<PopupBean, BaseViewHolder>

    override fun getLayoutResId(): Int = R.layout.popup_list_city

    override fun initView() {

        initProvinceData()

        provinceAdapter = initRecyclerView(
            rvId = R.id.rv_list,
            data = provincePops,
            adapterBuilder = { layoutResId, data ->
                createCityAdapter(data)
            }
        )

        cityAdapter = initRecyclerView(
            rvId = R.id.rv_list_city,
            data = cityPops,
            adapterBuilder = { layoutResId, data ->
                createCityAdapter(data)
            }
        )

        provinceAdapter.setOnItemClickListener { _, _, position ->
            provincePops.forEach { it.isCheck = false }
            provincePops[position].isCheck = true
            provinceAdapter.notifyDataSetChanged()

            cityPops.clear()
            val citys = provinces[position].children
            citys.forEachIndexed { index, areaBean ->
                cityPops.add(PopupBean(index, areaBean.value, false))
            }
            cityAdapter.setNewData(cityPops)
        }

        cityAdapter.setOnItemClickListener { _, _, position ->
            cityPops.forEach { it.isCheck = false }
            cityPops[position].isCheck = true
            cityAdapter.notifyDataSetChanged()
            notifySelect(cityPops[position])
        }
    }

    /**
     * 初始化省份数据（业务特有逻辑）
     */
    private fun initProvinceData() {
        provinces = MethodManager.getProvinces(context)
        // 初始化省份列表
        provinces.forEachIndexed { index, areaBean ->
            provincePops.add(PopupBean(index, areaBean.value, index == 0))
        }
        // 初始化默认城市列表
        val defaultCitys = provinces[0].children
        defaultCitys.forEachIndexed { index, areaBean ->
            cityPops.add(PopupBean(index, areaBean.value, false))
        }
    }

    /**
     * 创建城市/省份通用Adapter
     */
    private fun createCityAdapter(data: List<PopupBean>?): BaseQuickAdapter<PopupBean, BaseViewHolder> {
        return object : BaseQuickAdapter<PopupBean, BaseViewHolder>(R.layout.item_popwindow_list, data) {
            override fun convert(helper: BaseViewHolder, item: PopupBean) {
                helper.setText(R.id.tv_name, item.name)
                helper.setVisible(R.id.iv_check, item.isCheck)
            }
        }
    }
}