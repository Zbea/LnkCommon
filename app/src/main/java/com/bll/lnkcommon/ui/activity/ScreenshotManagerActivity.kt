package com.bll.lnkcommon.ui.activity

import PopupClick
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.*
import com.bll.lnkcommon.manager.ItemTypeDaoManager
import com.bll.lnkcommon.mvp.model.ItemList
import com.bll.lnkcommon.mvp.model.ItemTypeBean
import com.bll.lnkcommon.mvp.model.PopupBean
import com.bll.lnkcommon.ui.adapter.ScreenshotAdapter
import com.bll.lnkcommon.ui.adapter.TabTypeAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.widget.FlowLayoutManager
import com.bll.lnkcommon.widget.SpaceGridItemDeco1
import com.chad.library.adapter.base.BaseQuickAdapter
import kotlinx.android.synthetic.main.ac_list_type.*
import kotlinx.android.synthetic.main.common_page_number.*
import kotlinx.android.synthetic.main.common_title.*
import java.io.File
import kotlin.math.ceil

class ScreenshotManagerActivity:BaseActivity() {

    private var screenTypes= mutableListOf<ItemTypeBean>()
    private var listMap=HashMap<Int,MutableList<File>>()
    private var popupBeans = mutableListOf<PopupBean>()
    private var longBeans = mutableListOf<ItemList>()
    private var tabPos=0
    private var mAdapter:ScreenshotAdapter?=null
    private var pos=0


    override fun layoutId(): Int {
        return R.layout.ac_list_type
    }

    override fun initData() {
        pageSize = 12
        popupBeans.add(PopupBean(0, "创建分类", false))
        popupBeans.add(PopupBean(1, "删除分类", false))
    }

    override fun initView() {
        setPageTitle("图库列表")
        showView(iv_manager)

        iv_manager.setOnClickListener {
            PopupClick(this, popupBeans, iv_manager, 5).builder().setOnSelectListener { item ->
                when (item.id) {
                    0 -> {
                        InputContentDialog(this,"创建分类").builder().setOnDialogClickListener{
                            if (ItemTypeDaoManager.getInstance().isExist(it,3)){
                                //创建文件夹
                                showToast("已存在")
                                return@setOnDialogClickListener
                            }
                            val path=FileAddress().getPathScreen(it)
                            if (!File(path).exists()){
                                File(path).parentFile?.mkdir()
                                File(path).mkdirs()
                            }
                            val bean= ItemTypeBean()
                            bean.type=3
                            bean.title=it
                            bean.path=path
                            bean.date=System.currentTimeMillis()
                            ItemTypeDaoManager.getInstance().insertOrReplace(bean)
                            mTabTypeAdapter?.addData(screenTypes.size-1,bean)
                        }
                    }
                    1 -> {
                        val types= ItemTypeDaoManager.getInstance().queryAll(3)
                        val lists= mutableListOf<ItemList>()
                        for (ite in types){
                            lists.add(ItemList(types.indexOf(ite),ite.title))
                        }
                        ItemSelectorDialog(this,"删除分类",lists).builder().setOnDialogClickListener{
                            val screenTypeBean=types[it]
                            if (FileUtils.getFiles(screenTypeBean.path).size>0){
                                showToast("分类存在截图，无法删除")
                                return@setOnDialogClickListener
                            }
                            FileUtils.deleteFile(File(screenTypeBean.path))
                            ItemTypeDaoManager.getInstance().deleteBean(screenTypeBean)
                            var index=0
                            for (i in screenTypes.indices){
                                if (screenTypes[i].title == screenTypeBean.title){
                                    index=i
                                }
                            }
                            mTabTypeAdapter?.remove(index)
                            if (index==tabPos){
                                screenTypes[0].isCheck=true
                                tabPos=0
                                mTabTypeAdapter?.notifyItemChanged(0)
                                getFetchFiles()
                            }
                        }
                    }
                }
            }
        }

        initRecycleView()
        initTab()
    }

    private fun initTab() {
        screenTypes=ItemTypeDaoManager.getInstance().queryAll(3)
        screenTypes.add(ItemTypeBean().apply {
            path=FileAddress().getPathScreen("未分类")
            title="未分类"
        })
        screenTypes[tabPos].isCheck=true
        mTabTypeAdapter?.setNewData(screenTypes)
        getFetchFiles()
    }

    override fun onTabClickListener(view: View, position: Int) {
        tabPos=position
        getFetchFiles()
    }

    private fun initRecycleView(){
        val layoutParams= LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(
            DP2PX.dip2px(this,28f), DP2PX.dip2px(this,40f),
            DP2PX.dip2px(this,28f),0)
        layoutParams.weight=1f
        rv_list.layoutParams= layoutParams

        rv_list.layoutManager = GridLayoutManager(this, 4)//创建布局管理
        mAdapter = ScreenshotAdapter(R.layout.item_screenshot, null).apply {
            rv_list.adapter = this
            bindToRecyclerView(rv_list)
            setEmptyView(R.layout.common_empty)
            rv_list?.addItemDecoration(SpaceGridItemDeco1(4, DP2PX.dip2px(this@ScreenshotManagerActivity, 22f), DP2PX.dip2px(this@ScreenshotManagerActivity, 60f)))
            setOnItemClickListener { adapter, view, position ->
                val file=mAdapter?.getItem(position)
                ImageDialog(this@ScreenshotManagerActivity, arrayListOf(file?.path!!)).builder()
            }
            onItemLongClickListener = BaseQuickAdapter.OnItemLongClickListener { adapter, view, position ->
                pos=position
                onLongClick()
                true
            }
        }

    }

    private fun onLongClick() {
        longBeans.clear()
        longBeans.add(ItemList().apply {
            name=getString(R.string.delete)
            resId=R.mipmap.icon_setting_delete
        })
        if (tabPos==0){
            longBeans.add(ItemList().apply {
                name="分类"
                resId=R.mipmap.icon_setting_set
            })
        }
        else{
            longBeans.add(ItemList().apply {
                name=getString(R.string.shift_out)
                resId=R.mipmap.icon_setting_out
            })
        }
        val file= mAdapter?.data?.get(pos)!!
        LongClickManageDialog(this, file.name,longBeans).builder()
            .setOnDialogClickListener {
                if (it==0){
                    mAdapter?.remove(pos)
                    FileUtils.deleteFile(file)
                }
                else{
                    if (tabPos==0){
                        val types= ItemTypeDaoManager.getInstance().queryAll(3)
                        val lists= mutableListOf<ItemList>()
                        for (ite in types){
                            lists.add(ItemList(types.indexOf(ite),ite.title))
                        }
                        ItemSelectorDialog(this,"设置分类",lists).builder().setOnDialogClickListener{
                            FileUtils.copyFile(file.path,types[it].path+"/"+file.name)
                            FileUtils.deleteFile(file)
                            mAdapter?.remove(pos)
                        }
                    }
                    else{
                        val path=FileAddress().getPathScreen("未分类")
                        FileUtils.copyFile(file.path,path+"/"+file.name)
                        FileUtils.deleteFile(file)
                        mAdapter?.remove(pos)
                    }
                }
            }
    }

    /**
     * 获取本地文件
     */
    private fun getFetchFiles(){
        listMap.clear()
        mAdapter?.setNewData(null)
        pageIndex=1
        val path=screenTypes[tabPos].path
        val files= FileUtils.getFiles(path)
        setPageNumber(files.size)

        val pageTotal=files.size //全部数量
        val count = ceil(pageTotal.toDouble()/pageSize).toInt()//总共页码
        var toIndex=pageSize
        for(i in 0 until count){
            val index=i*pageSize
            if(index+pageSize>pageTotal){        //作用为toIndex最后没有12条数据则剩余几条newList中就装几条
                toIndex=pageTotal-index
            }
            val newList = files.subList(index,index+toIndex)
            listMap[i+1]=newList
        }

        if (files.size>0)
            fetchData()
    }

    override fun fetchData() {
        mAdapter?.setNewData(listMap[pageIndex]!!)
        tv_page_current.text=pageIndex.toString()
    }
}