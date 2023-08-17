package com.bll.lnkcommon.ui.fragment

import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.manager.AppDaoManager
import com.bll.lnkcommon.mvp.model.AppBean
import com.bll.lnkcommon.ui.activity.AppCenterActivity
import com.bll.lnkcommon.ui.adapter.AppListAdapter
import com.bll.lnkcommon.utils.AppUtils
import com.bll.lnkcommon.utils.BitmapUtils
import com.bll.lnkcommon.widget.SpaceGridItemDeco
import kotlinx.android.synthetic.main.fragment_app.*

class AppFragment:BaseFragment() {

    private var apps= mutableListOf<AppBean>()
    private var menuApps= mutableListOf<AppBean>()
    private var mAdapter: AppListAdapter?=null
    private var mMenuAdapter: AppListAdapter?=null

    override fun getLayoutId(): Int {
        return R.layout.fragment_app
    }

    override fun initView() {
        setTitle(DataBeanManager.mainListTitle[3])
        initRecyclerView()
        initMenuRecyclerView()

        tv_add.setOnClickListener {
            val list= mutableListOf<AppBean>()
            for (item in apps){
                if (item.isCheck){
                    item.userId=getUser()?.accountId!!
                    list.add(item)
                }
            }
            AppDaoManager.getInstance().insertOrReplaces(list)
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
        rv_list.addItemDecoration(SpaceGridItemDeco(5,70))
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
                if (isSelector5()){
                    item.isCheck=!item.isCheck
                    mAdapter?.notifyItemChanged(position)
                }
                else{
                    showToast("至多选择5个应用")
                }
            }
        }
        mAdapter?.setOnItemLongClickListener { adapter, view, position ->
            if (position>2){
                CommonDialog(requireActivity()).setContent("卸载应用？").builder().setDialogClickListener(object :
                    CommonDialog.OnDialogClickListener {
                    override fun cancel() {
                    }
                    override fun ok() {
                        AppUtils.uninstallAPK(requireActivity(),apps[position].packageName)
                    }
                })
            }
            true
        }
    }

    private fun initMenuRecyclerView(){
        rv_list_tool.layoutManager = GridLayoutManager(activity,5)//创建布局管理
        mMenuAdapter = AppListAdapter(R.layout.item_app_list, 0,null)
        rv_list_tool.adapter = mMenuAdapter
        mMenuAdapter?.bindToRecyclerView(rv_list_tool)
        mMenuAdapter?.setOnItemChildClickListener { adapter, view, position ->
            val item=menuApps[position]
            if (view.id==R.id.cb_check){
                item.isCheck=!item.isCheck
                mMenuAdapter?.notifyItemChanged(position)
            }
        }
    }

    private fun isSelector5():Boolean{
        val list= mutableListOf<AppBean>()
        for (item in apps){
            if (item.isCheck)
                list.add(item)
        }
        return list.size<=5
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
        apps.addAll(AppUtils.scanLocalInstallAppList(activity))
        mAdapter?.setNewData(apps)
    }

    private fun initMenuData(){
        ll_menu.visibility=if (isLoginState()) View.VISIBLE else View.INVISIBLE
        if (isLoginState()){
            menuApps=AppDaoManager.getInstance().queryAll()
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
            Constants.APP_EVENT->{
                initData()
            }
        }
    }

    override fun onRefreshData() {
        super.onRefreshData()
        initData()
    }

}