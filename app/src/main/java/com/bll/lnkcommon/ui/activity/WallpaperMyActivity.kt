package com.bll.lnkcommon.ui.activity

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.dialog.ImageDialog
import com.bll.lnkcommon.dialog.LongClickManageDialog
import com.bll.lnkcommon.manager.WallpaperDaoManager
import com.bll.lnkcommon.mvp.model.ItemList
import com.bll.lnkcommon.mvp.model.WallpaperBean
import com.bll.lnkcommon.ui.adapter.WallpaperAdapter
import com.bll.lnkcommon.ui.adapter.WallpaperMyAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.widget.SpaceGridItemDeco
import com.bll.lnkcommon.widget.SpaceGridItemDeco1
import com.chad.library.adapter.base.BaseQuickAdapter
import kotlinx.android.synthetic.main.ac_list.*
import kotlinx.android.synthetic.main.common_title.tv_setting
import java.io.File

class WallpaperMyActivity:BaseActivity(){

    private var items= mutableListOf<WallpaperBean>()
    private var mAdapter:WallpaperMyAdapter?=null
    private var position=-1

    override fun layoutId(): Int {
        return R.layout.ac_list
    }

    override fun initData() {
        pageSize=12
    }
    override fun initView() {
        setPageTitle("我的壁纸")
        showView(tv_setting)

        tv_setting.text="设为壁纸"
        tv_setting.setOnClickListener {
            if (position>=0){
                val item=items[position]
                if(File(item.path).exists()){
                    android.os.SystemProperties.set("xsys.eink.standby",item.path)
                    showToast("设置成功")
                }
            }
        }

        initRecycleView()
        fetchData()
    }

    private fun initRecycleView(){
        val layoutParams= LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(
            DP2PX.dip2px(this,30f), DP2PX.dip2px(this,60f),
            DP2PX.dip2px(this,30f),0)
        layoutParams.weight=1f
        rv_list.layoutParams= layoutParams

        rv_list.layoutManager = GridLayoutManager(this, 4)//创建布局管理
        mAdapter = WallpaperMyAdapter(R.layout.item_wallpaper_my,null).apply {
            rv_list.adapter = this
            bindToRecyclerView(rv_list)
            setEmptyView(R.layout.common_empty)
            rv_list?.addItemDecoration(SpaceGridItemDeco(4,90))
            setOnItemClickListener { adapter, view, position ->
                ImageDialog(this@WallpaperMyActivity,items[position].bodyUrl.split(",")).builder()
            }
            setOnItemChildClickListener { adapter, view, position ->
                this@WallpaperMyActivity.position=position
                if (view.id==R.id.cb_check){
                    for (item in items){
                        item.isCheck=false
                    }
                    val item=items[position]
                    item.isCheck=true
                    mAdapter?.notifyDataSetChanged()
                }
            }
            onItemLongClickListener = BaseQuickAdapter.OnItemLongClickListener { adapter, view, position ->
                delete(position)
                true
            }
        }
    }

    private fun delete(pos:Int){
        CommonDialog(this).setContent("确定删除？").builder().setDialogClickListener(object :
            CommonDialog.OnDialogClickListener {
            override fun cancel() {
            }
            override fun ok() {
                val item=items[pos]
                FileUtils.deleteFile(File(item.path))
                WallpaperDaoManager.getInstance().deleteBean(item)
                mAdapter?.remove(pos)
            }
        })
    }

    override fun fetchData() {
        val count=WallpaperDaoManager.getInstance().queryList().size
        items=WallpaperDaoManager.getInstance().queryList(pageSize,pageIndex)
        setPageNumber(count)
        mAdapter?.setNewData(items)
    }

}