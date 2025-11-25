package com.bll.lnkcommon.ui.fragment.cloud

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseCloudFragment
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.manager.ItemTypeDaoManager
import com.bll.lnkcommon.manager.NoteContentDaoManager
import com.bll.lnkcommon.manager.NoteDaoManager
import com.bll.lnkcommon.mvp.model.CloudList
import com.bll.lnkcommon.mvp.model.ItemTypeBean
import com.bll.lnkcommon.mvp.model.Note
import com.bll.lnkcommon.mvp.model.NoteContent
import com.bll.lnkcommon.ui.adapter.cloud.CloudNoteAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.DownloadManager
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.utils.zip.IZipCallback
import com.bll.lnkcommon.utils.zip.ZipUtils
import com.bll.lnkcommon.widget.SpaceItemDeco
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.liulishuo.filedownloader.BaseDownloadTask
import kotlinx.android.synthetic.main.fragment_list_content.rv_list
import org.greenrobot.eventbus.EventBus
import java.io.File

class CloudNoteFragment: BaseCloudFragment() {

    private var mAdapter: CloudNoteAdapter?=null
    private var notes= mutableListOf<Note>()
    private var position=0

    override fun getLayoutId(): Int {
        return R.layout.fragment_list_content
    }

    override fun initView() {
        pageSize=14
        initRecyclerView()
    }

    override fun lazyLoad() {
        fetchData()
    }

    private fun initRecyclerView() {
        val layoutParams= LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(DP2PX.dip2px(activity,30f), DP2PX.dip2px(activity,20f), DP2PX.dip2px(activity,30f),0)
        layoutParams.weight=1f
        rv_list.layoutParams= layoutParams
        mAdapter = CloudNoteAdapter(R.layout.item_cloud_diary, null).apply {
            rv_list.layoutManager = LinearLayoutManager(activity)//创建布局管理
            rv_list.adapter = this
            bindToRecyclerView(rv_list)
            setOnItemClickListener { adapter, view, position ->
                this@CloudNoteFragment.position=position
                CommonDialog(requireActivity()).setContent("确定下载？").builder()
                    .setDialogClickListener(object : CommonDialog.OnDialogClickListener {
                        override fun cancel() {
                        }
                        override fun ok() {
                            downloadItem()
                        }
                    })
            }
            setOnItemChildClickListener { adapter, view, position ->
                this@CloudNoteFragment.position=position
                if (view.id==R.id.iv_delete){
                    CommonDialog(requireActivity()).setContent("确定删除？").builder()
                        .setDialogClickListener(object : CommonDialog.OnDialogClickListener {
                            override fun cancel() {
                            }
                            override fun ok() {
                                deleteItem()
                            }
                        })
                }
            }
        }
        rv_list.addItemDecoration(SpaceItemDeco(30))
    }

    private fun downloadItem(){
        val note=notes[position]
        if (NoteDaoManager.getInstance().isExistCloud(note.typeStr,note.title,note.date)){
            showToast("已下载")
        }
        else{
            downloadNote(note)
        }
    }

    private fun deleteItem(){
        val ids= mutableListOf<Int>()
        ids.add(notes[position].cloudId)
        mCloudPresenter.deleteCloud(ids)
    }

    /**
     * 下载笔记
     */
    private fun downloadNote(item: Note){
        showLoading()
        val zipPath = FileAddress().getPathZip(FileUtils.getUrlName(item.downloadUrl))
        val fileTargetPath=FileAddress().getPathNote(item.typeStr,item.title)
        mDownloadManager?.startSingle(item.downloadUrl,zipPath, object : DownloadManager.SingleCallback {
            override fun onProgress(task: BaseDownloadTask, soFar: Long, total: Long) {

            }
            override fun onCompleted(task: BaseDownloadTask) {
                ZipUtils.unzip(zipPath, fileTargetPath, object : IZipCallback {
                    override fun onFinish() {
                        if (item.typeStr!="我的密本"&&!ItemTypeDaoManager.getInstance().isExist(item.typeStr,1)){
                            val noteType = ItemTypeBean().apply {
                                title = item.typeStr
                                type=1
                                date=System.currentTimeMillis()
                            }
                            ItemTypeDaoManager.getInstance().insertOrReplace(noteType)
                        }
                        //添加笔记
                        item.id=null//设置数据库id为null用于重新加入
                        item.date=System.currentTimeMillis()
                        NoteDaoManager.getInstance().insertOrReplace(item)

                        val noteContents=Gson().fromJson(item.contentJson, object : TypeToken<List<NoteContent>>() {}.type) as MutableList<NoteContent>
                        for (contentBean in noteContents){
                            contentBean.id=null
                            NoteContentDaoManager.getInstance().insertOrReplaceNote(contentBean)
                        }

                        //删掉本地zip文件
                        FileUtils.deleteFile(File(zipPath))
                        EventBus.getDefault().post(Constants.NOTE_TYPE_REFRESH_EVENT)
                        deleteItem()
                        showToast("下载成功")
                        hideLoading()
                    }
                    override fun onProgress(percentDone: Int) {
                    }
                    override fun onError(msg: String?) {
                        showToast(msg!!)
                        hideLoading()
                    }
                    override fun onStart() {
                    }
                })
            }
            override fun onFailed(task: BaseDownloadTask?, error: String) {
                hideLoading()
                showToast("下载失败")
            }
        })
    }

    override fun fetchData() {
        val map = HashMap<String, Any>()
        map["page"]=pageIndex
        map["size"] = pageSize
        map["type"] = 3
        mCloudPresenter.getList(map)
    }

    override fun onCloudType(types: MutableList<String>) {
    }

    override fun onCloudList(cloudList: CloudList) {
        setPageNumber(cloudList.total)
        notes.clear()
        for (item in cloudList.list){
            if (item.listJson.isNotEmpty()){
                val note= Gson().fromJson(item.listJson, Note::class.java)
                note.cloudId=item.id
                note.downloadUrl=item.downloadUrl
                note.contentJson=item.contentJson
                notes.add(note)
            }
        }
        mAdapter?.setNewData(notes)
    }

    override fun onCloudDelete() {
        mAdapter?.remove(position)
        onRefreshList(notes)
    }
}