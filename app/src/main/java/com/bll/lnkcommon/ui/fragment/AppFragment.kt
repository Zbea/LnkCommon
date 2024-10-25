package com.bll.lnkcommon.ui.fragment

import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.manager.AppDaoManager
import com.bll.lnkcommon.mvp.model.AppBean
import com.bll.lnkcommon.ui.adapter.AppListAdapter
import com.bll.lnkcommon.utils.AppUtils
import com.bll.lnkcommon.widget.SpaceGridItemDeco
import kotlinx.android.synthetic.main.fragment_app.*

class AppFragment:BaseFragment() {

    private var apps= mutableListOf<AppBean>()
    private var mAdapter: AppListAdapter?=null
    private var position=0

    override fun getLayoutId(): Int {
        return R.layout.fragment_app
    }

    override fun initView() {
        setTitle(DataBeanManager.mainListTitle[3])

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
            val packageName= apps[position].packageName
            AppUtils.startAPP(activity,packageName)
        }
        mAdapter?.setOnItemLongClickListener { adapter, view, position ->
            this.position=position
            CommonDialog(requireActivity()).setContent("确认卸载应用？").builder().setDialogClickListener(object : CommonDialog.OnDialogClickListener {
                override fun cancel() {
                }
                override fun ok() {
                    AppUtils.uninstallAPK(requireActivity(),apps[position].packageName)
                }
            })
            true
        }
    }


    private fun initData() {
        if (MethodManager.isLogin()){
            apps=AppUtils.scanLocalInstallAppList(requireActivity())
        }
        mAdapter?.setNewData(apps)
    }


    override fun onEventBusMessage(msgFlag: String) {
        when(msgFlag){
            Constants.USER_EVENT->{
                initData()
            }
            Constants.APP_INSTALL_EVENT->{
                initData()
            }
            Constants.APP_UNINSTALL_EVENT->{
                AppDaoManager.getInstance().delete(apps[position].packageName)
                mAdapter?.remove(position)
            }
        }
    }

}