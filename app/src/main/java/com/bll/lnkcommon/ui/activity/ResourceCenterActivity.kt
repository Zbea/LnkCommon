package com.bll.lnkcommon.ui.activity

import android.view.View
import androidx.fragment.app.Fragment
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.PopupRadioList
import com.bll.lnkcommon.mvp.model.ItemTypeBean
import com.bll.lnkcommon.mvp.model.PopupBean
import com.bll.lnkcommon.ui.fragment.resource.AppDownloadFragment
import com.bll.lnkcommon.ui.fragment.resource.CalenderDownloadFragment
import com.bll.lnkcommon.ui.fragment.resource.WallpaperDownloadFragment
import kotlinx.android.synthetic.main.common_title.*

class ResourceCenterActivity: BaseActivity(){
    private var lastFragment: Fragment? = null
    private var appFragment: AppDownloadFragment? = null
    private var bookFragment: AppDownloadFragment? = null
    private var readFragment: AppDownloadFragment? = null
    private var toolFragment: AppDownloadFragment? = null
    private var wallpaperFragment: WallpaperDownloadFragment? = null
    private var calenderFragment: CalenderDownloadFragment? = null

    private var popSupplys= mutableListOf<PopupBean>()

    override fun layoutId(): Int {
        return  R.layout.ac_resource
    }

    override fun initData() {
        popSupplys=DataBeanManager.popupSupplys
    }

    override fun initView() {
        setPageTitle("资源中心")
        showView(tv_supply)

        appFragment=AppDownloadFragment().newInstance(1)
        bookFragment=AppDownloadFragment().newInstance(3)
        readFragment=AppDownloadFragment().newInstance(4)
        toolFragment=AppDownloadFragment().newInstance(2)
        wallpaperFragment = WallpaperDownloadFragment()
        calenderFragment = CalenderDownloadFragment()

        switchFragment(lastFragment, appFragment)
        initTab()

        if (popSupplys.size>0)
            tv_supply.text=popSupplys[0].name
        tv_supply.setOnClickListener {
            PopupRadioList(this,popSupplys,tv_supply,tv_supply.width,5).builder().setOnSelectListener {
                tv_supply.text = it.name
                appFragment?.changeSupply(it.id)
                bookFragment?.changeSupply(it.id)
                readFragment?.changeSupply(it.id)
                toolFragment?.changeSupply(it.id)
                wallpaperFragment?.changeSupply(it.id)
                calenderFragment?.changeSupply(it.id)
            }
        }

    }

    private fun initTab(){
        for (i in DataBeanManager.resources.indices) {
            itemTabTypes.add(ItemTypeBean().apply {
                title=DataBeanManager.resources[i]
                isCheck=i==0
            })
        }
        mTabTypeAdapter?.setNewData(itemTabTypes)
        fetchData()
    }

    override fun onTabClickListener(view: View, position: Int) {
        when(position){
            0->{
                switchFragment(lastFragment, appFragment)
            }
            1->{
                switchFragment(lastFragment, bookFragment)
            }
            2->{
                switchFragment(lastFragment, readFragment)
            }
            3->{
                switchFragment(lastFragment, toolFragment)
            }
            4->{
                switchFragment(lastFragment, wallpaperFragment)
            }
            5->{
                switchFragment(lastFragment, calenderFragment)
            }
        }
    }


    //页码跳转
    private fun switchFragment(from: Fragment?, to: Fragment?) {
        if (from != to) {
            lastFragment = to
            val fm = supportFragmentManager
            val ft = fm.beginTransaction()

            if (!to!!.isAdded) {
                if (from != null) {
                    ft.hide(from)
                }
                ft.add(R.id.frame_layout, to).commit()
            } else {
                if (from != null) {
                    ft.hide(from)
                }
                ft.show(to).commit()
            }
        }
    }

}