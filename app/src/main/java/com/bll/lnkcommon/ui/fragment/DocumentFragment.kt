package com.bll.lnkcommon.ui.fragment

import PopupClick
import android.media.MediaScannerConnection
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.dialog.DocumentDetailsDialog
import com.bll.lnkcommon.dialog.InputContentDialog
import com.bll.lnkcommon.dialog.ItemSelectorDialog
import com.bll.lnkcommon.dialog.LongClickManageDialog
import com.bll.lnkcommon.manager.ItemTypeDaoManager
import com.bll.lnkcommon.mvp.model.ItemList
import com.bll.lnkcommon.mvp.model.ItemTypeBean
import com.bll.lnkcommon.mvp.model.PopupBean
import com.bll.lnkcommon.ui.adapter.DocumentAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.widget.SpaceGridItemDeco
import kotlinx.android.synthetic.main.ac_list_tab.rv_list
import kotlinx.android.synthetic.main.common_fragment_title.iv_manager
import java.io.File


class DocumentFragment : BaseFragment() {
    private var popupBeans = mutableListOf<PopupBean>()
    private var tabPos = 0
    private var mAdapter: DocumentAdapter? = null
    private var longBeans = mutableListOf<ItemList>()
    private var position=0

    override fun getLayoutId(): Int {
        return R.layout.fragment_list_tab
    }

    override fun initView() {
        setTitle(DataBeanManager.mainListTitle[2])
        showView(iv_manager)
        pageSize = 25

        popupBeans.add(PopupBean(0, getString(R.string.type_create_str), false))
        popupBeans.add(PopupBean(1, getString(R.string.type_delete_str), false))
        popupBeans.add(PopupBean(2, "文档明细", false))

        iv_manager?.setOnClickListener {
            PopupClick(requireActivity(), popupBeans, iv_manager, 5).builder().setOnSelectListener { item ->
                when (item.id) {
                    0 -> {
                        InputContentDialog(requireActivity(), getString(R.string.type_create_str)).builder().setOnDialogClickListener {
                            if (ItemTypeDaoManager.getInstance().isExist(it, 6)) {
                                showToast(R.string.existed)
                                return@setOnDialogClickListener
                            }
                            val path = FileAddress().getPathDocument(it)
                            MethodManager.createFileScan(requireActivity(),path)

                            val itemTypeBean = ItemTypeBean()
                            itemTypeBean.type = 6
                            itemTypeBean.date = System.currentTimeMillis()
                            itemTypeBean.title = it
                            itemTypeBean.path = path
                            ItemTypeDaoManager.getInstance().insertOrReplace(itemTypeBean)
                            mTabTypeAdapter?.addData(itemTypeBean)
                        }
                    }
                    1 -> {
                        if (tabPos == 0) {
                            showToast("默认分类，无法删除")
                            return@setOnSelectListener
                        }
                        if (FileUtils.isExistContent(itemTabTypes[tabPos].path)) {
                            showToast("分类存在内容，无法删除")
                            return@setOnSelectListener
                        }
                        CommonDialog(requireActivity()).setContent(R.string.tips_is_delete).builder().setDialogClickListener(object : CommonDialog.OnDialogClickListener {
                            override fun ok() {
                                val itemTypeBean=itemTabTypes[tabPos]
                                ItemTypeDaoManager.getInstance().deleteBean(itemTypeBean)
                                FileUtils.delete(itemTypeBean.path)
                                MediaScannerConnection.scanFile(requireActivity(), arrayOf(itemTypeBean.path),null, null)
                                mTabTypeAdapter?.remove(tabPos)
                                tabPos = 0
                                itemTabTypes[0].isCheck = true
                                pageIndex = 1
                                fetchData()
                            }
                        })
                    }
                    2 -> {
                        DocumentDetailsDialog(requireActivity()).builder()
                    }
                }
            }
        }

        initRecycleView()
        initTab()
    }

    override fun lazyLoad() {
        for (item in itemTabTypes){
            val path=item.path
            MethodManager.createFileScan(requireActivity(),path)
        }
        fetchData()
    }


    private fun initTab() {
        pageIndex = 1
        itemTabTypes = ItemTypeDaoManager.getInstance().queryAll(6)
        itemTabTypes.add(0, MethodManager.getDefaultItemTypeDocument())
        itemTabTypes = MethodManager.setItemTypeBeanCheck(itemTabTypes, tabPos)
        mTabTypeAdapter?.setNewData(itemTabTypes)
    }

    override fun onTabClickListener(view: View, position: Int) {
        tabPos = position
        pageIndex = 1
        fetchData()
    }

    private fun initRecycleView() {
        val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(
            DP2PX.dip2px(requireActivity(), 20f), DP2PX.dip2px(requireActivity(), 20f),
            DP2PX.dip2px(requireActivity(), 20f), 0
        )
        layoutParams.weight = 1f
        layoutParams.weight = 1f
        rv_list.layoutParams = layoutParams

        rv_list.layoutManager = GridLayoutManager(requireActivity(), 5)//创建布局管理
        mAdapter = DocumentAdapter(R.layout.item_document, null).apply {
            rv_list.adapter = this
            bindToRecyclerView(rv_list)
            rv_list?.addItemDecoration(SpaceGridItemDeco(3, 20))
            setEmptyView(R.layout.common_empty)
            setOnItemClickListener { adapter, view, position ->
                val file = mAdapter?.data?.get(position)
                MethodManager.gotoDocument(requireActivity(), file!!)
            }
            setOnItemLongClickListener { adapter, view, position ->
                this@DocumentFragment.position=position
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
                name=getString(R.string.set)
                resId=R.mipmap.icon_setting_set
            })
        }
        else{
            longBeans.add(ItemList().apply {
                name=getString(R.string.shift_out)
                resId=R.mipmap.icon_setting_out
            })
        }
        val file= mAdapter?.data?.get(position)!!
        val fileName = FileUtils.getUrlName(file.path)
        val drawPath = file.parent + "/${fileName}draw/"
        LongClickManageDialog(requireActivity(),file.name,longBeans).builder()
            .setOnDialogClickListener {
                if (it==0){
                    FileUtils.deleteFile(file)
                    FileUtils.deleteFile(File(drawPath))
                    MediaScannerConnection.scanFile(requireActivity(), arrayOf(file.absolutePath),null, null)
                    fetchData()
                }
                else{
                    if (tabPos==0){
                        val types= ItemTypeDaoManager.getInstance().queryAll(6)
                        val lists= mutableListOf<ItemList>()
                        for (ite in types){
                            lists.add(ItemList(types.indexOf(ite),ite.title))
                        }
                        ItemSelectorDialog(requireActivity(),getString(R.string.type_set_str),lists).builder().setOnDialogClickListener{ pos->
                            val newPath=types[pos].path+"/"+file.name
                            FileUtils.moveFile(file.path,newPath)
                            MediaScannerConnection.scanFile(requireActivity(), arrayOf(file.path,newPath),null, null)
                            val newDrawPath=File(newPath).parent+"/${fileName}draw/"
                            FileUtils.moveDirectory(drawPath,newDrawPath)
                            mAdapter?.remove(position)
                        }
                    }
                    else{
                        val path= FileAddress().getPathDocument("默认")
                        val newPath=path+"/"+file.name
                        FileUtils.moveFile(file.path,newPath)
                        MediaScannerConnection.scanFile(requireActivity(), arrayOf(file.path,newPath),null, null)
                        val newDrawPath=File(newPath).parent+"/${fileName}draw/"
                        FileUtils.moveDirectory(drawPath,newDrawPath)
                        mAdapter?.remove(position)
                    }
                }
            }
    }

    override fun fetchData() {
        val path = itemTabTypes[tabPos].path
        val totalNum = FileUtils.getFiles(path).size
        setPageNumber(totalNum)
        val files = FileUtils.getDescFiles(path, pageIndex, pageSize)
        mAdapter?.setNewData(files)
    }


    override fun onRefreshData() {
        lazyLoad()
    }
}