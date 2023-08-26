package com.bll.lnkcommon.ui.fragment

import android.content.Intent
import android.view.View
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
import com.bll.lnkcommon.ui.activity.AppCenterActivity
import com.bll.lnkcommon.ui.adapter.AppListAdapter
import com.bll.lnkcommon.utils.AppUtils
import com.bll.lnkcommon.utils.BitmapUtils
import com.bll.lnkcommon.widget.SpaceGridItemDeco
import kotlinx.android.synthetic.main.fragment_app.*
import kotlinx.android.synthetic.main.fragment_app.rv_list

class AppFragment:BaseFragment() {

    private var apps= mutableListOf<AppBean>()
    private var menuApps= mutableListOf<AppBean>()
    private var mAdapter: AppListAdapter?=null
    private var mMenuAdapter: AppListAdapter?=null
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

        setTitle(DataBeanManager.mainListTitle[3])
        initRecyclerView()
        initMenuRecyclerView()

        tv_add.setOnClickListener {
            for (item in apps){
                if (item.isCheck){
                    item.userId=getUser()?.accountId!!
                    if (!AppDaoManager.getInstance().isExist(item.packageName,0)){
                        AppDaoManager.getInstance().insertOrReplace(item)
                    }
                }
            }
            for (item in apps){
                item.isCheck=false
            }
            mAdapter?.notifyDataSetChanged()
            initMenuData()
        }
        tv_out.setOnClickListener {
            val list= mutableListOf<AppBean>()
            for (item in menuApps){
                if (item.isCheck){
                    list.add(item)
                }
            }
            AppDaoManager.getInstance().deletes(list)
            initMenuData()
        }
    }

    override fun lazyLoad() {
        initMenuData()
        initData()
    }


    private fun initRecyclerView(){
        rv_list.layoutManager = GridLayoutManager(activity,5)//创建布局管理
        mAdapter = AppListAdapter(R.layout.item_app_list, 0,null)
        rv_list.adapter = mAdapter
        mAdapter?.bindToRecyclerView(rv_list)
        rv_list.addItemDecoration(SpaceGridItemDeco(5,50))
        mAdapter?.setOnItemClickListener { adapter, view, position ->
            if (position==0){
                customStartActivity(Intent(requireActivity(), AppCenterActivity::class.java))
            }
            else{
                val packageName= apps[position].packageName
                AppUtils.startAPP(activity,packageName)
            }
        }
        mAdapter?.setOnItemChildClickListener { adapter, view, position ->
            val item=apps[position]
            if (view.id==R.id.cb_check){
                item.isCheck=!item.isCheck
                mAdapter?.notifyItemChanged(position)
            }
        }
        mAdapter?.setOnItemLongClickListener { adapter, view, position ->
            this.position=position
            if (position>2){
                LongClickManageDialog(requireActivity(),apps[position].appName,longBeans).builder().setOnDialogClickListener{
                    if (it==0){
                        AppMenuDialog(requireActivity(),apps[position]).builder()
                    }
                    else{
                        AppUtils.uninstallAPK(requireActivity(),apps[position].packageName)
                    }
                }
            }
            true
        }
    }

    private fun initMenuRecyclerView(){
        rv_list_tool.layoutManager = GridLayoutManager(activity,5)//创建布局管理
        mMenuAdapter = AppListAdapter(R.layout.item_app_list, 0,null)
        rv_list_tool.adapter = mMenuAdapter
        mMenuAdapter?.bindToRecyclerView(rv_list_tool)
        rv_list_tool.addItemDecoration(SpaceGridItemDeco(5,40))
        mMenuAdapter?.setOnItemChildClickListener { adapter, view, position ->
            val item=menuApps[position]
            if (view.id==R.id.cb_check){
                item.isCheck=!item.isCheck
                mMenuAdapter?.notifyItemChanged(position)
            }
        }
    }

    private fun initData() {
        apps.clear()
        apps.add(AppBean().apply {
            appName = "应用中心"
            imageByte = BitmapUtils.drawableToByte(requireActivity().getDrawable(R.mipmap.icon_app_center))
            isBase = true
        })
        apps.add(AppBean().apply {
            appName="百度搜索"
            imageByte = BitmapUtils.drawableToByte(requireActivity().getDrawable(R.mipmap.icon_app_bd))
            packageName="com.baidu.searchbox"
            isBase=true
        })
        apps.add(AppBean().apply {
            appName="应用宝"
            imageByte = BitmapUtils.drawableToByte(requireActivity().getDrawable(R.mipmap.icon_app_yyb))
            packageName="com.tencent.android.qqdownloader"
            isBase=true
        })
        apps.add(AppBean().apply {
            appName="几何绘图"
            imageByte = BitmapUtils.drawableToByte(requireActivity().getDrawable(R.mipmap.icon_app_geometry))
            packageName=Constants.PACKAGE_GEOMETRY
            isBase=false
        })
        apps.addAll(AppUtils.scanLocalInstallAppList(activity))
        mAdapter?.setNewData(apps)
    }

    private fun initMenuData(){
        ll_menu.visibility=if (isLoginState()) View.VISIBLE else View.INVISIBLE
        if (isLoginState()){
            menuApps=AppDaoManager.getInstance().queryTool()
            mMenuAdapter?.setNewData(menuApps)
        }
        else{
            menuApps.clear()
            mMenuAdapter?.setNewData(menuApps)
        }

    }

    override fun onEventBusMessage(msgFlag: String) {
        when(msgFlag){
            Constants.USER_EVENT->{
                initMenuData()
            }
            Constants.APP_INSTALL_EVENT->{
                initData()
                initMenuData()
            }
            Constants.APP_UNINSTALL_EVENT->{
                if (isLoginState()){
                    AppDaoManager.getInstance().delete(apps[position].packageName)
                }
                initData()
                initMenuData()
            }
        }
    }

}