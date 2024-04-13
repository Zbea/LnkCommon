package com.bll.lnkcommon.ui.activity

import PopupClick
import android.content.Intent
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.*
import com.bll.lnkcommon.manager.ItemTypeDaoManager
import com.bll.lnkcommon.mvp.model.ItemList
import com.bll.lnkcommon.mvp.model.ItemTypeBean
import com.bll.lnkcommon.mvp.model.PopupBean
import com.bll.lnkcommon.ui.adapter.ScreenshotAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.widget.SpaceGridItemDeco1
import com.chad.library.adapter.base.BaseQuickAdapter
import kotlinx.android.synthetic.main.ac_book_type_list.*
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
        return R.layout.ac_book_type_list
    }

    override fun initData() {
        pageSize = 12
        popupBeans.add(PopupBean(0, "创建分类", false))
        popupBeans.add(PopupBean(1, "删除分类", false))

        longBeans.add(ItemList().apply {
            name="删除"
            resId=R.mipmap.icon_setting_delete
        })
        longBeans.add(ItemList().apply {
            name="移出"
            resId=R.mipmap.icon_setting_out
        })

    }

    override fun initView() {
        setPageTitle("截图管理")
        setSettingText("截图列表")
        showView(tv_province)
        tv_province.text="分类管理"


        tv_province.setOnClickListener {
            setTopSelectView()
        }
        tv_setting.setOnClickListener {
            customStartActivity(Intent(this, ScreenshotListActivity::class.java))
        }

        initRecycleView()
        initTab()

    }

    //顶部弹出选择
    private fun setTopSelectView() {
        PopupClick(this, popupBeans, tv_province, 5).builder().setOnSelectListener { item ->
            when (item.id) {
                0 -> {
                    InputContentDialog(this,"创建分类").builder().setOnDialogClickListener{
                        if (ItemTypeDaoManager.getInstance().isExist(it,3)){
                            showToast("已存在")
                            return@setOnDialogClickListener
                        }
                        val path=FileAddress().getPathScreen(it)
                        //创建文件夹
                        if (!File(path).exists()){
                            File(path).parentFile.mkdir()
                            File(path).mkdirs()
                        }
                        val bean= ItemTypeBean()
                        bean.type=3
                        bean.title=it
                        bean.path=path
                        bean.date=System.currentTimeMillis()
                        ItemTypeDaoManager.getInstance().insertOrReplace(bean)

                        rg_group.addView(getRadioButton(screenTypes.size, it,screenTypes.size==0))
                        screenTypes.add(bean)
                        //更新tab
                        if (screenTypes.isEmpty()){
                            getFetchFiles()
                        }
                    }
                }
                1 -> {
                    val lists= mutableListOf<ItemList>()
                    for (ite in screenTypes){
                        lists.add(ItemList(screenTypes.indexOf(ite),ite.title))
                    }
                    ItemSelectorDialog(this,"删除分类",lists).builder().setOnDialogClickListener{
                        val screenTypeBean=screenTypes[it]
                        if (FileUtils.getFiles(screenTypeBean.path).size>0){
                            showToast("分类存在截图，无法删除")
                            return@setOnDialogClickListener
                        }
                        FileUtils.deleteFile(File(screenTypeBean.path))
                        ItemTypeDaoManager.getInstance().deleteBean(screenTypeBean)
                        rg_group.removeViewAt(it)
                        if (tabPos==it){
                            if (screenTypes.size>0){
                                rg_group.check(0)
                            }
                            else{
                                screenTypes.clear()
                                mAdapter?.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initTab() {
        screenTypes=ItemTypeDaoManager.getInstance().queryAll(3)
        rg_group.removeAllViews()
        if (screenTypes.isEmpty()){
            return
        }
        for (i in screenTypes.indices) {
            rg_group.addView(getRadioButton(i, screenTypes[i].title, i==0))
        }
        rg_group.setOnCheckedChangeListener { radioGroup, id ->
            pageIndex = 1
            tabPos=id
            getFetchFiles()
        }
        getFetchFiles()
    }

    private fun initRecycleView(){

        val layoutParams= LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(
            DP2PX.dip2px(this,28f), DP2PX.dip2px(this,60f),
            DP2PX.dip2px(this,28f),0)
        layoutParams.weight=1f
        rv_list.layoutParams= layoutParams

        rv_list.layoutManager = GridLayoutManager(this, 4)//创建布局管理
        mAdapter = ScreenshotAdapter(R.layout.item_screenshot, null).apply {
            rv_list.adapter = this
            bindToRecyclerView(rv_list)
            setEmptyView(R.layout.common_empty)
            rv_list?.addItemDecoration(SpaceGridItemDeco1(4, DP2PX.dip2px(this@ScreenshotManagerActivity, 22f), DP2PX.dip2px(this@ScreenshotManagerActivity, 70f)))
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
        val file= mAdapter?.data?.get(pos)!!
        LongClickManageDialog(this, file.name,longBeans).builder()
            .setOnDialogClickListener {
                if (it==0){
                    mAdapter?.remove(pos)
                    FileUtils.deleteFile(file)
                }
                else{
                    val path=FileAddress().getPathScreen("未分类")
                    FileUtils.copyFile(file.path,path+"/"+file.name)
                    FileUtils.deleteFile(file)
                    mAdapter?.remove(pos)
                }
            }
    }

    /**
     * 获取本地文件
     */
    private fun getFetchFiles(){
        listMap.clear()
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

    override fun onEventBusMessage(msgFlag: String) {
        if (msgFlag==Constants.SCREENSHOT_MANAGER_EVENT){
            getFetchFiles()
        }
    }
}