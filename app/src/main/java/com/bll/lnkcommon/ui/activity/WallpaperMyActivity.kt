package com.bll.lnkcommon.ui.activity

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.ImageDialog
import com.bll.lnkcommon.dialog.LongClickManageDialog
import com.bll.lnkcommon.manager.WallpaperDaoManager
import com.bll.lnkcommon.mvp.model.ItemList
import com.bll.lnkcommon.mvp.model.WallpaperBean
import com.bll.lnkcommon.ui.adapter.WallpaperAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.widget.SpaceGridItemDeco1
import com.chad.library.adapter.base.BaseQuickAdapter
import kotlinx.android.synthetic.main.ac_list.*
import java.io.File

class WallpaperMyActivity:BaseActivity(){

    private var items= mutableListOf<WallpaperBean>()
    private var mAdapter:WallpaperAdapter?=null
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
        setPageTitle("我的壁纸")

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
        mAdapter = WallpaperAdapter(R.layout.item_wallpaper_my, 2,null).apply {
            rv_list.adapter = this
            bindToRecyclerView(rv_list)
            setEmptyView(R.layout.common_empty)
            rv_list?.addItemDecoration(SpaceGridItemDeco1(4, DP2PX.dip2px(this@WallpaperMyActivity, 20f)
                , DP2PX.dip2px(this@WallpaperMyActivity, 70f)))
            setOnItemClickListener { adapter, view, position ->
                ImageDialog(this@WallpaperMyActivity,items[position].bodyUrl.split(",")).builder()
            }
            onItemLongClickListener = BaseQuickAdapter.OnItemLongClickListener { adapter, view, position ->
                this@WallpaperMyActivity.position = position
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
                    WallpaperDaoManager.getInstance().deleteBean(item)
                    mAdapter?.remove(position)
                }
                else{
                    if(File(item.path).exists()){
                        android.os.SystemProperties.set("xsys.eink.standby",item.path)
//                android.os.SystemProperties.set("xsys.eink.poweroff",item.path)
                    }
                }
            }
    }

    override fun fetchData() {
        val count=WallpaperDaoManager.getInstance().queryList().size
        items=WallpaperDaoManager.getInstance().queryList(pageSize,pageIndex)
        setPageNumber(count)
        mAdapter?.setNewData(items)
    }

}