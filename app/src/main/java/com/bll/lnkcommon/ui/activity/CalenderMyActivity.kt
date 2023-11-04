package com.bll.lnkcommon.ui.activity

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.CalenderDetailsDialog
import com.bll.lnkcommon.dialog.ImageDialog
import com.bll.lnkcommon.dialog.LongClickManageDialog
import com.bll.lnkcommon.manager.CalenderDaoManager
import com.bll.lnkcommon.mvp.model.CalenderItemBean
import com.bll.lnkcommon.mvp.model.ItemList
import com.bll.lnkcommon.ui.adapter.CalenderListAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.DateUtils
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.widget.SpaceGridItemDeco1
import com.chad.library.adapter.base.BaseQuickAdapter
import kotlinx.android.synthetic.main.ac_list.*
import org.greenrobot.eventbus.EventBus
import java.io.File

class CalenderMyActivity:BaseActivity(){

    private var items= mutableListOf<CalenderItemBean>()
    private var mAdapter:CalenderListAdapter?=null
    private var longBeans = mutableListOf<ItemList>()
    private var position=0

    override fun layoutId(): Int {
        return R.layout.ac_list
    }

    override fun initData() {
        pageSize=12

        longBeans.add(ItemList().apply {
            name="删除"
            resId=R.mipmap.icon_setting_delete
        })
        longBeans.add(ItemList().apply {
            name="设置"
            resId=R.mipmap.icon_setting_set
        })
    }
    override fun initView() {
        setPageTitle("我的台历")

        initRecycleView()
        fetchData()
    }

    private fun initRecycleView(){

        val layoutParams= LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(
            DP2PX.dip2px(this,28f), DP2PX.dip2px(this,60f),
            DP2PX.dip2px(this,28f),0)
        layoutParams.weight=1f
        rv_list.layoutParams= layoutParams

        rv_list.layoutManager = GridLayoutManager(this, 4)//创建布局管理
        mAdapter = CalenderListAdapter(R.layout.item_calendar, null).apply {
            rv_list.adapter = this
            bindToRecyclerView(rv_list)
            setEmptyView(R.layout.common_empty)
            rv_list?.addItemDecoration(SpaceGridItemDeco1(4, DP2PX.dip2px(this@CalenderMyActivity, 20f)
                , DP2PX.dip2px(this@CalenderMyActivity, 60f)))
            setOnItemClickListener { adapter, view, position ->
                val item =items[position]
                item.loadSate=2
                CalenderDetailsDialog(this@CalenderMyActivity,item).builder()
            }
            setOnItemChildClickListener { adapter, view, position ->
                val item=items[position]
                if (view.id==R.id.tv_preview){
                    val urls=item.previewUrl.split(",")
                    ImageDialog(this@CalenderMyActivity,urls).builder()
                }
            }
            onItemLongClickListener = BaseQuickAdapter.OnItemLongClickListener { adapter, view, position ->
                this@CalenderMyActivity.position = position
                onLongClick()
                true
            }
        }
    }

    private fun onLongClick() {
        val item=items[position]
        LongClickManageDialog(this, item.title,longBeans).builder()
            .setOnDialogClickListener {
                if (it==0){
                    FileUtils.deleteFile(File(item.path))
                    CalenderDaoManager.getInstance().deleteBean(item)
                    mAdapter?.remove(position)
                }
                else{
                    CalenderDaoManager.getInstance().setSetFalse()
                    item.isSet=true
                    CalenderDaoManager.getInstance().insertOrReplace(item)
                }
                EventBus.getDefault().post(Constants.CALENDER_SET_EVENT)
            }
    }

    override fun fetchData() {
        val count=CalenderDaoManager.getInstance().queryList(DateUtils.getYear()).size
        items=CalenderDaoManager.getInstance().queryList(DateUtils.getYear(),pageIndex,pageSize)
        setPageNumber(count)
        mAdapter?.setNewData(items)
    }

    override fun onEventBusMessage(msgFlag: String) {
        if (msgFlag==Constants.CALENDER_EVENT){
            pageIndex=1
            fetchData()
        }
    }

}