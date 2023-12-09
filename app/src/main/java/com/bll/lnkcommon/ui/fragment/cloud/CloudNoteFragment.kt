package com.bll.lnkcommon.ui.fragment.cloud

import android.os.Handler
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseCloudFragment
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.manager.ItemTypeDaoManager
import com.bll.lnkcommon.manager.NoteContentDaoManager
import com.bll.lnkcommon.manager.NoteDaoManager
import com.bll.lnkcommon.mvp.model.CloudList
import com.bll.lnkcommon.mvp.model.ItemTypeBean
import com.bll.lnkcommon.mvp.model.Note
import com.bll.lnkcommon.mvp.model.NoteContent
import com.bll.lnkcommon.ui.adapter.CloudNoteAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.FileDownManager
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.utils.zip.IZipCallback
import com.bll.lnkcommon.utils.zip.ZipUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.liulishuo.filedownloader.BaseDownloadTask
import kotlinx.android.synthetic.main.common_radiogroup.*
import kotlinx.android.synthetic.main.fragment_cloud_list_type.*
import org.greenrobot.eventbus.EventBus
import java.io.File

class CloudNoteFragment: BaseCloudFragment() {

    var noteType=0
    private var noteTypeStr=""
    private var mAdapter:CloudNoteAdapter?=null
    private var notes= mutableListOf<Note>()
    private var position=0

    override fun getLayoutId(): Int {
        return R.layout.fragment_cloud_list_type
    }

    override fun initView() {
        pageSize=10
        initRecyclerView()
    }

    override fun lazyLoad() {
        mCloudPresenter.getType(3)
    }

    private fun initTab(){
        noteTypeStr=types[0]
        for (i in types.indices) {
            rg_group.addView(getRadioButton(i ,types[i],types.size-1))
        }
        rg_group.setOnCheckedChangeListener { radioGroup, id ->
            noteType=id
            noteTypeStr=types[id]
            pageIndex=1
            fetchData()
        }
        fetchData()
    }

    private fun initRecyclerView() {
        val layoutParams= LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(0, DP2PX.dip2px(activity,25f), 0,0)
        layoutParams.weight=1f
        rv_list.layoutParams= layoutParams
        mAdapter = CloudNoteAdapter(R.layout.item_note, null).apply {
            rv_list.layoutManager = LinearLayoutManager(activity)//创建布局管理
            rv_list.adapter = this
            bindToRecyclerView(rv_list)
            setOnItemClickListener { adapter, view, position ->
                val note=notes[position]
                if (NoteDaoManager.getInstance().isExistCloud(note.typeStr,note.title,note.date)){
                    showToast("已下载")
                }
                else{
                    downloadNote(note)
                }
            }
            onItemLongClickListener = BaseQuickAdapter.OnItemLongClickListener { adapter, view, position ->
                this@CloudNoteFragment.position=position
                CommonDialog(requireActivity()).setContent("确定删除").builder()
                    .setDialogClickListener(object : CommonDialog.OnDialogClickListener {
                        override fun cancel() {
                        }
                        override fun ok() {
                            val ids= mutableListOf<Int>()
                            ids.add(notes[position].cloudId)
                            mCloudPresenter.deleteCloud(ids)
                        }
                    })
                true
            }
        }
    }

    /**
     * 下载笔记
     */
    private fun downloadNote(item: Note){
        showLoading()
        val zipPath = FileAddress().getPathZip(File(item.downloadUrl).name)
        val fileTargetPath=FileAddress().getPathNote(item.typeStr,item.title)
        FileDownManager.with(activity).create(item.downloadUrl).setPath(zipPath)
            .startSingleTaskDownLoad(object :
                FileDownManager.SingleTaskCallBack {
                override fun progress(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                }
                override fun paused(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                }
                override fun completed(task: BaseDownloadTask?) {
                    ZipUtils.unzip(zipPath, fileTargetPath, object : IZipCallback {
                        override fun onFinish() {
                            addNote(item)
                            //添加笔记内容
                            val jsonArray= JsonParser().parse(item.contentJson).asJsonArray
                            for (json in jsonArray){
                                val contentBean= Gson().fromJson(json, NoteContent::class.java)
                                contentBean.id=null//设置数据库id为null用于重新加入
                                NoteContentDaoManager.getInstance().insertOrReplaceNote(contentBean)
                            }
                            //删掉本地zip文件
                            FileUtils.deleteFile(File(zipPath))
                            Handler().postDelayed({
                                EventBus.getDefault().post(Constants.NOTE_EVENT)
                                showToast("下载成功")
                                hideLoading()
                            },500)
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
                override fun error(task: BaseDownloadTask?, e: Throwable?) {
                    hideLoading()
                    showToast("下载失败")
                }
            })
    }

    /**
     * 添加笔记（如果下载的笔记分类本地不存在则添加）
     */
    private fun addNote(item: Note){
        item.id=null//设置数据库id为null用于重新加入
        if (!ItemTypeDaoManager.getInstance().isExist(item.typeStr,1)){
            val noteType = ItemTypeBean().apply {
                title = item.typeStr
                type=1
                date=System.currentTimeMillis()
            }
            ItemTypeDaoManager.getInstance().insertOrReplace(noteType)
        }
        //添加笔记
        NoteDaoManager.getInstance().insertOrReplace(item)
    }

    override fun fetchData() {
        val map = HashMap<String, Any>()
        map["page"]=pageIndex
        map["size"] = pageSize
        map["type"] = 3
        map["subTypeStr"] = noteTypeStr
        mCloudPresenter.getList(map)
    }

    override fun onCloudType(types: MutableList<String>) {
        this.types=types
        if (types.size>0)
            initTab()
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
    }
}