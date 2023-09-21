package com.bll.lnkcommon.ui.fragment

import android.content.Intent
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.dialog.AppMenuDialog
import com.bll.lnkcommon.dialog.LongClickManageDialog
import com.bll.lnkcommon.manager.AppDaoManager
import com.bll.lnkcommon.mvp.model.AppBean
import com.bll.lnkcommon.mvp.model.ItemList
import com.bll.lnkcommon.ui.activity.*
import com.bll.lnkcommon.ui.activity.book.BookStoreTypeActivity
import com.bll.lnkcommon.ui.adapter.AppListAdapter
import com.bll.lnkcommon.utils.AppUtils
import com.bll.lnkcommon.utils.BitmapUtils
import com.bll.lnkcommon.widget.SpaceGridItemDeco
import kotlinx.android.synthetic.main.common_title.tv_setting
import kotlinx.android.synthetic.main.fragment_app.rv_list

class AppFragment:BaseFragment() {

    private var apps= mutableListOf<AppBean>()
    private var mAdapter: AppListAdapter?=null
    private var position=0
    private var longBeans = mutableListOf<ItemList>()

    override fun getLayoutId(): Int {
        return R.layout.fragment_app
    }

    override fun initView() {
        longBeans.add(ItemList().apply {
            name="菜单"
            resId=R.mipmap.icon_setting_menu
        })
        longBeans.add(ItemList().apply {
            name="卸载"
            resId=R.mipmap.icon_setting_uninstall
        })

        setTitle(DataBeanManager.mainListTitle[4])
        showView(tv_setting)
        tv_setting.text="工具设置"

        tv_setting.setOnClickListener {
            customStartActivity(Intent(requireActivity(),if (isLoginState())AppToolActivity::class.java
            else AccountLoginActivity::class.java))
        }

        initRecyclerView()

    }

    override fun lazyLoad() {
        initData()
    }


    private fun initRecyclerView(){
        rv_list.layoutManager = GridLayoutManager(activity,5)//创建布局管理
        mAdapter = AppListAdapter(R.layout.item_app_list, 0,null)
        rv_list.adapter = mAdapter
        mAdapter?.bindToRecyclerView(rv_list)
        rv_list.addItemDecoration(SpaceGridItemDeco(5,50))
        mAdapter?.setOnItemClickListener { adapter, view, position ->
            when (position) {
                0 -> {
                    if (isLoginState()){
                        customStartActivity(Intent(requireActivity(), AppCenterActivity::class.java))
                    }
                }
                1 -> {
                    if (isLoginState()){
                        customStartActivity(Intent(requireActivity(), CalenderListActivity::class.java))
                    }
                }
                2->{
                    if (isLoginState()){
                        customStartActivity(Intent(requireActivity(), WallpaperListActivity::class.java))
                    }
                }
                3->{
                    if (isLoginState()){
                        customStartActivity(Intent(requireActivity(), BookStoreTypeActivity::class.java))
                    }
                }
                else -> {
                    val packageName= apps[position].packageName
                    AppUtils.startAPP(activity,packageName)
                }
            }
        }
        mAdapter?.setOnItemLongClickListener { adapter, view, position ->
            this.position=position
            if (position>3){
                LongClickManageDialog(requireActivity(),apps[position].appName,longBeans).builder().setOnDialogClickListener{
                    when (it) {
                        0 -> {
                            AppMenuDialog(requireActivity(),apps[position]).builder()
                        }
                        else -> {
                            AppUtils.uninstallAPK(requireActivity(),apps[position].packageName)
                        }
                    }
                }
            }
            true
        }
    }


    private fun initData() {
        apps.clear()
        if (isLoginState()){
            apps.add(AppBean().apply {
                appName = "应用中心"
                imageByte = BitmapUtils.drawableToByte(requireActivity().getDrawable(R.mipmap.icon_app_center))
            })
            apps.add(AppBean().apply {
                appName="新年台历"
                imageByte = BitmapUtils.drawableToByte(requireActivity().getDrawable(R.mipmap.icon_app_center))
            })
            apps.add(AppBean().apply {
                appName="壁纸"
                imageByte = BitmapUtils.drawableToByte(requireActivity().getDrawable(R.mipmap.icon_app_center))
            })
            apps.add(AppBean().apply {
                appName="书库"
                imageByte = BitmapUtils.drawableToByte(requireActivity().getDrawable(R.mipmap.icon_app_center))
            })
            apps.addAll(AppDaoManager.getInstance().queryAPP())
        }
        mAdapter?.setNewData(apps)
    }


    override fun onEventBusMessage(msgFlag: String) {
        when(msgFlag){
            Constants.USER_EVENT->{
                initData()
            }
            Constants.APP_INSTALL_INSERT_EVENT->{
                initData()
            }
            Constants.APP_UNINSTALL_EVENT->{
                AppDaoManager.getInstance().delete(apps[position])
                mAdapter?.remove(position)
            }
        }
    }

}