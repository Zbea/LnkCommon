package com.bll.lnkcommon.ui.fragment.resource

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.MyApplication
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.dialog.ImageDialog
import com.bll.lnkcommon.manager.WallpaperDaoManager
import com.bll.lnkcommon.mvp.model.WallpaperBean
import com.bll.lnkcommon.mvp.model.WallpaperList
import com.bll.lnkcommon.mvp.presenter.WallpaperPresenter
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.ui.adapter.WallpaperAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.FileMultitaskDownManager
import com.bll.lnkcommon.utils.NetworkUtil
import com.bll.lnkcommon.widget.SpaceGridItemDeco
import com.bll.lnkcommon.widget.SpaceGridItemDeco1
import com.liulishuo.filedownloader.BaseDownloadTask
import kotlinx.android.synthetic.main.fragment_list_content.*

class WallpaperDownloadFragment : BaseFragment(), IContractView.IWallpaperView{

    private var presenter= WallpaperPresenter(this)
    private var items= mutableListOf<WallpaperBean>()
    private var mAdapter: WallpaperAdapter?=null
    private var supply=1
    private var position=0

    override fun onList(bean: WallpaperList) {
        setPageNumber(bean.total)
        items=bean.list
        mAdapter?.setNewData(items)
    }

    override fun buySuccess() {
        items[position].buyStatus=1
        mAdapter?.notifyDataSetChanged()
    }


    override fun getLayoutId(): Int {
        return R.layout.fragment_list_content
    }

    override fun initView() {
        pageSize=12
        initRecyclerView()
    }

    override fun lazyLoad() {
        if (NetworkUtil.isNetworkConnected()) {
            fetchData()
        }
    }

    private fun initRecyclerView(){
        val layoutParams= LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(
            DP2PX.dip2px(requireActivity(),30f),
            DP2PX.dip2px(requireActivity(),40f),
            DP2PX.dip2px(requireActivity(),30f),0)
        layoutParams.weight=1f
        rv_list.layoutParams= layoutParams

        rv_list.layoutManager = GridLayoutManager(requireActivity(),4)//创建布局管理
        mAdapter = WallpaperAdapter(R.layout.item_wallpaper, items)
        rv_list.adapter = mAdapter
        rv_list.addItemDecoration(SpaceGridItemDeco(4,30))
        mAdapter?.bindToRecyclerView(rv_list)
        mAdapter?.setEmptyView(R.layout.common_empty)
        mAdapter?.setOnItemClickListener { adapter, view, position ->
            ImageDialog(requireActivity(), arrayListOf(items[position].bodyUrl) ).builder()
        }
        mAdapter?.setOnItemChildClickListener{ adapter, view, position ->
            this.position=position
            val item=items[position]
            if (view.id==R.id.btn_download){
                if (item.buyStatus==1){
                    val paintingBean= WallpaperDaoManager.getInstance().queryBean(item.contentId)
                    if (paintingBean==null){
                        onDownload()
                    }
                    else{
                        showToast("已下载")
                    }
                }
                else{
                    val map = HashMap<String, Any>()
                    map["type"] = 5
                    map["bookId"] = item.contentId
                    presenter.onBuy(map)
                }
            }
        }
    }


    /**
     * 下载
     */
    private fun onDownload(){
        val item=items[position]
        showLoading()
        val pathStr= FileAddress().getPathImage("wallpaper",item.contentId)
        val images = mutableListOf(item.bodyUrl)
        val savePaths= arrayListOf("$pathStr/1.png")
        FileMultitaskDownManager.with(requireActivity()).create(images).setPath(savePaths).startMultiTaskDownLoad(
            object : FileMultitaskDownManager.MultiTaskCallBack {
                override fun progress(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int, ) {
                }
                override fun completed(task: BaseDownloadTask?) {
                    hideLoading()
                    item.path=savePaths[0]
                    item.date=System.currentTimeMillis()
                    WallpaperDaoManager.getInstance().insertOrReplace(item)
                    showToast("下载完成")
                }
                override fun paused(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                }
                override fun error(task: BaseDownloadTask?, e: Throwable?) {
                    hideLoading()
                    showToast("下载失败")
                }
            })
    }

    /**
     * 改变供应商
     */
    fun changeSupply(supply:Int){
        this.supply=supply
        fetchData()
    }


    override fun fetchData() {
        val map = HashMap<String, Any>()
        map["page"] = pageIndex
        map["size"] = pageSize
        map["supply"]=supply
        map["type"]=1
        map["imgType"]=1
        presenter.getList(map)
    }

}