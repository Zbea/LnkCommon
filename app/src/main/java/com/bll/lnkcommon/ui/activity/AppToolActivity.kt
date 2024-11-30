package com.bll.lnkcommon.ui.activity

import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.dialog.LongClickManageDialog
import com.bll.lnkcommon.manager.AppDaoManager
import com.bll.lnkcommon.mvp.model.AppBean
import com.bll.lnkcommon.mvp.model.ItemList
import com.bll.lnkcommon.ui.adapter.AppListAdapter
import com.bll.lnkcommon.utils.AppUtils
import com.bll.lnkcommon.utils.BitmapUtils
import com.bll.lnkcommon.widget.SpaceGridItemDeco
import kotlinx.android.synthetic.main.ac_app_tool.*

class AppToolActivity:BaseActivity() {

    private var apps= mutableListOf<AppBean>()
    private var menuApps= mutableListOf<AppBean>()
    private var mAdapter: AppListAdapter?=null
    private var mMenuAdapter: AppListAdapter?=null

    override fun layoutId(): Int {
        return R.layout.ac_app_tool
    }

    override fun initData() {
    }

    override fun initView() {
        setPageTitle("我的工具")
        initRecyclerView()
        initMenuRecyclerView()

        tv_add.setOnClickListener {
            for (item in apps){
                if (item.isCheck){
                    if (!AppDaoManager.getInstance().isExist(item.packageName,2)){
                        item.type=2
                        AppDaoManager.getInstance().insertOrReplace(item)
                    }
                }
            }
            setData()
            setMenuData()
        }
        tv_out.setOnClickListener {
            for (item in menuApps){
                if (item.isCheck){
                    item.type=0
                    AppDaoManager.getInstance().insertOrReplace(item)
                }
            }
            setData()
            setMenuData()
        }

        setData()
        setMenuData()
    }

    private fun initRecyclerView(){
        rv_list.layoutManager = GridLayoutManager(this,6)//创建布局管理
        mAdapter = AppListAdapter(R.layout.item_app_list, 1,null)
        rv_list.adapter = mAdapter
        mAdapter?.bindToRecyclerView(rv_list)
        rv_list.addItemDecoration(SpaceGridItemDeco(6,50))
        mAdapter?.setOnItemClickListener { adapter, view, position ->
            val packageName= apps[position].packageName
            if (packageName!=Constants.PACKAGE_GEOMETRY){
                AppUtils.startAPP(this,packageName)
            }
        }
        mAdapter?.setOnItemChildClickListener { adapter, view, position ->
            val item=apps[position]
            if (view.id==R.id.ll_name){
                item.isCheck=!item.isCheck
                mAdapter?.notifyItemChanged(position)
            }
        }
        mAdapter?.setOnItemLongClickListener { adapter, view, position ->
            val item=apps[position]
            val packageName= item.packageName
            if (packageName!=Constants.PACKAGE_GEOMETRY){
                CommonDialog(this).setContent("确认卸载该应用？").builder().setDialogClickListener(object :
                    CommonDialog.OnDialogClickListener {
                    override fun cancel() {
                    }
                    override fun ok() {
                        AppUtils.uninstallAPK(this@AppToolActivity,apps[position].packageName)
                        AppDaoManager.getInstance().delete(item)
                        return
                    }
                })
            }
            true
        }
    }

    private fun initMenuRecyclerView(){
        rv_list_tool.layoutManager = GridLayoutManager(this,6)//创建布局管理
        mMenuAdapter = AppListAdapter(R.layout.item_app_list, 1,null)
        rv_list_tool.adapter = mMenuAdapter
        mMenuAdapter?.bindToRecyclerView(rv_list_tool)
        rv_list_tool.addItemDecoration(SpaceGridItemDeco(6,30))
        mMenuAdapter?.setOnItemChildClickListener { adapter, view, position ->
            val item=menuApps[position]
            if (view.id==R.id.ll_name){
                item.isCheck=!item.isCheck
                mMenuAdapter?.notifyItemChanged(position)
            }
        }
    }

    private fun setData(){
        apps.clear()
        if (!AppDaoManager.getInstance().isExist(Constants.PACKAGE_GEOMETRY)){
            AppDaoManager.getInstance().insertOrReplace(AppBean().apply {
                appName="几何绘图"
                imageByte = BitmapUtils.drawableToByte(getDrawable(R.mipmap.icon_app_geometry))
                packageName=Constants.PACKAGE_GEOMETRY
                type=0
                subType=1
            })
        }
        apps=AppDaoManager.getInstance().queryAPPTool()
        for (item in apps){
            item.isCheck=false
        }
        mAdapter?.setNewData(apps)
    }

    private fun setMenuData(){
        menuApps=AppDaoManager.getInstance().queryTool()
        for (item in menuApps){
            item.isCheck=false
        }
        mMenuAdapter?.setNewData(menuApps)
    }

    override fun onEventBusMessage(msgFlag: String) {
        when(msgFlag){
            Constants.APP_INSTALL_INSERT_EVENT->{
                setData()
            }
            Constants.APP_UNINSTALL_EVENT->{
                setData()
                setMenuData()
            }
        }
    }

}