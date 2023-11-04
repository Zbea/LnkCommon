package com.bll.lnkcommon.ui.fragment

import PopupClick
import android.content.Intent
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.Constants.CHECK_PASSWORD_EVENT
import com.bll.lnkcommon.Constants.NOTE_BOOK_MANAGER_EVENT
import com.bll.lnkcommon.Constants.NOTE_EVENT
import com.bll.lnkcommon.Constants.USER_EVENT
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.dialog.PrivacyPasswordDialog
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.dialog.InputContentDialog
import com.bll.lnkcommon.dialog.NoteModuleAddDialog
import com.bll.lnkcommon.manager.ItemTypeDaoManager
import com.bll.lnkcommon.manager.NoteContentDaoManager
import com.bll.lnkcommon.manager.NoteDaoManager
import com.bll.lnkcommon.mvp.model.CloudListBean
import com.bll.lnkcommon.mvp.model.ItemTypeBean
import com.bll.lnkcommon.mvp.model.Note
import com.bll.lnkcommon.mvp.model.PopupBean
import com.bll.lnkcommon.ui.activity.AccountLoginActivity
import com.bll.lnkcommon.ui.activity.NotebookManagerActivity
import com.bll.lnkcommon.ui.adapter.NoteAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.DateUtils
import com.bll.lnkcommon.utils.FileUploadManager
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.utils.ToolUtils
import com.google.gson.Gson
import kotlinx.android.synthetic.main.common_radiogroup.*
import kotlinx.android.synthetic.main.common_title.*
import kotlinx.android.synthetic.main.fragment_note.*
import org.greenrobot.eventbus.EventBus
import java.io.File

class NoteFragment:BaseFragment() {

    private var popupBeans = mutableListOf<PopupBean>()
    private var notebooks = mutableListOf<ItemTypeBean>()
    private var notes = mutableListOf<Note>()
    private var mAdapter: NoteAdapter? = null
    private var position = 0 //当前笔记标记
    private var positionType = 0//当前笔记本标记
    private var typeStr=""

    override fun getLayoutId(): Int {
        return R.layout.fragment_note
    }
    override fun initView() {
        pageSize=10

        popupBeans.add(PopupBean(0, getString(R.string.notebook_manager), true))
        popupBeans.add(PopupBean(1, getString(R.string.notebook_create), false))
        popupBeans.add(PopupBean(2, getString(R.string.note_create), false))

        setTitle(DataBeanManager.mainListTitle[3])
        showView(iv_manager)

        iv_manager?.setOnClickListener {
            if (isLoginState()){
                setTopSelectView()
            }
            else{
                customStartActivity(Intent(requireActivity(),AccountLoginActivity::class.java))
            }
        }

        initRecyclerView()
        findTabs()
    }

    override fun lazyLoad() {
    }

    private fun initRecyclerView() {
        rv_list.layoutManager = LinearLayoutManager(activity)//创建布局管理
        mAdapter = NoteAdapter(R.layout.item_note, null)
        rv_list.adapter = mAdapter
        mAdapter?.bindToRecyclerView(rv_list)
        mAdapter?.setOnItemClickListener { adapter, view, position ->
            val note = notes[position]
            if (positionType==0&&privacyPassword!=null&&privacyPassword?.isSet==true&&!note.isCancelPassword){
                PrivacyPasswordDialog(requireActivity()).builder()?.setOnDialogClickListener{
                    gotoNote(note)
                }
            }
            else{
                gotoNote(note)
            }
        }
        mAdapter?.setOnItemChildClickListener { adapter, view, position ->
            this.position = position
            val note=notes[position]
            when(view.id){
                R.id.iv_delete->{
                    CommonDialog(requireActivity()).setContent("确定要删除主题？").builder()
                        .setDialogClickListener(object : CommonDialog.OnDialogClickListener {
                            override fun cancel() {
                            }
                            override fun ok() {
                                mAdapter?.remove(position)
                                //删除笔记本
                                NoteDaoManager.getInstance().deleteBean(note)
                                //删除笔记本中的所有笔记
                                NoteContentDaoManager.getInstance().deleteType(note.typeStr, note.title)
                                val path= FileAddress().getPathNote(note.typeStr,note.title)
                                FileUtils.deleteFile(File(path))
                            }
                        })
                }
                R.id.iv_edit->{
                    InputContentDialog(requireContext(), note.title).builder()
                        .setOnDialogClickListener { string ->
                            if (NoteDaoManager.getInstance().isExist(typeStr,string)){
                                showToast("已存在")
                                return@setOnDialogClickListener
                            }
                            //修改内容分类
                            NoteContentDaoManager.getInstance().editNotes(note.typeStr,note.title,string)
                            note.title = string
                            mAdapter?.notifyItemChanged(position)
                            NoteDaoManager.getInstance().insertOrReplace(note)
                        }
                }
                R.id.iv_password->{
                    PrivacyPasswordDialog(requireActivity()).builder()?.setOnDialogClickListener{
                        note.isCancelPassword=!note.isCancelPassword
                        mAdapter?.notifyItemChanged(position)
                        NoteDaoManager.getInstance().insertOrReplace(note)
                    }
                }
            }
        }
    }

    //顶部弹出选择
    private fun setTopSelectView() {
        PopupClick(requireActivity(), popupBeans, iv_manager, 5).builder().setOnSelectListener { item ->
            when (item.id) {
                0 -> customStartActivity(Intent(activity, NotebookManagerActivity::class.java))
                1 -> addNoteBookType()
                else -> {
                    NoteModuleAddDialog(requireContext(),1).builder()
                        ?.setOnDialogClickListener { moduleBean ->
                            createNote(ToolUtils.getImageResStr(activity, moduleBean.resContentId))
                        }
                }
            }
        }
    }

    /**
     * tab数据设置
     */
    private fun findTabs() {
        notebooks.clear()
        notebooks.add(ItemTypeBean().apply {
            title = getString(R.string.note_tab_diary)
        })
        if (isLoginState()){
            notebooks.addAll(ItemTypeDaoManager.getInstance().queryAll(1))
            if (positionType>=notebooks.size){
                positionType=0
            }
            typeStr = notebooks[positionType].title
            initTab()
            fetchData()
        }
        else{
            initTab()
            notes.clear()
            mAdapter?.setNewData(notes)
        }

    }

    //设置头部索引
    private fun initTab() {
        rg_group.removeAllViews()
        for (i in notebooks.indices) {
            rg_group.addView(getRadioButton(i,positionType, notebooks[i].title, notebooks.size - 1))
        }
        rg_group.setOnCheckedChangeListener { radioGroup, id ->
            positionType=id
            typeStr=notebooks[positionType].title
            pageIndex=1
            fetchData()
        }
    }

    private fun getRadioButton(i:Int,check:Int,str:String,max:Int): RadioButton {
        val radioButton =
            layoutInflater.inflate(R.layout.common_radiobutton, null) as RadioButton
        radioButton.text = str
        radioButton.id = i
        radioButton.isChecked = i == check
        val layoutParams = RadioGroup.LayoutParams(
            RadioGroup.LayoutParams.WRAP_CONTENT,
            DP2PX.dip2px(activity, 45f))

        layoutParams.marginEnd = if (i == max) 0 else DP2PX.dip2px(activity, 44f)
        radioButton.layoutParams = layoutParams

        return radioButton
    }

    //新建笔记分类
    private fun addNoteBookType() {
        InputContentDialog(requireContext(),  "请输入笔记本").builder()
            .setOnDialogClickListener { string ->
                if (ItemTypeDaoManager.getInstance().isExist(string,1)){
                    showToast("已存在")
                }
                else{
                    val noteBook = ItemTypeBean()
                    noteBook.type=1
                    noteBook.title = string
                    noteBook.date=System.currentTimeMillis()
                    notebooks.add(noteBook)
                    ItemTypeDaoManager.getInstance().insertOrReplace(noteBook)
                    initTab()
                }
            }
    }

    //新建笔记
    private fun createNote(resId:String) {
        val note = Note()
        InputContentDialog(requireContext(),  "请输入主题").builder()
            .setOnDialogClickListener { string ->
                if (NoteDaoManager.getInstance().isExist(typeStr,string)){
                    showToast("已存在")
                    return@setOnDialogClickListener
                }
                note.title = string
                note.date = System.currentTimeMillis()
                note.typeStr = typeStr
                note.contentResId = resId
                pageIndex=1
                NoteDaoManager.getInstance().insertOrReplace(note)
                EventBus.getDefault().post(NOTE_EVENT)
            }
    }

    override fun onEventBusMessage(msgFlag: String) {
        when(msgFlag){
            USER_EVENT->{
                privacyPassword=getCheckPasswordObj()
                positionType=0
                findTabs()
            }
            NOTE_BOOK_MANAGER_EVENT->{
                findTabs()
            }
            NOTE_EVENT->{
                fetchData()
            }
            CHECK_PASSWORD_EVENT->{
                privacyPassword=getCheckPasswordObj()
                fetchData()
            }
        }
    }

    override fun fetchData() {
        if (isLoginState())
        {
            notes = NoteDaoManager.getInstance().queryAll(typeStr, pageIndex, pageSize)
            val total = NoteDaoManager.getInstance().queryAll(typeStr)
            setPageNumber(total.size)
            for (item in notes){
                item.isSet = positionType==0&&privacyPassword!=null&&privacyPassword?.isSet==true
            }
            mAdapter?.setNewData(notes)
        }
    }

    /**
     * 上传
     */
    fun upload(token:String){
        cloudList.clear()
        val nullItems= mutableListOf<Note>()
        for (noteType in notebooks){
            //查找到这个分类的所有内容，然后遍历上传所有内容
            val notes= NoteDaoManager.getInstance().queryAll(noteType.title)
            for (item in notes){
                val path=FileAddress().getPathNote(noteType.title,item.title)
                val fileName=item.title
                //获取笔记所有内容
                val noteContents = NoteContentDaoManager.getInstance().queryAll(item.typeStr,item.title)
                //如果此笔记还没有开始书写，则不用上传源文件
                if (noteContents.size>0){
                    FileUploadManager(token).apply {
                        startUpload(path,fileName)
                        setCallBack{
                            cloudList.add(CloudListBean().apply {
                                type=3
                                subType=-1
                                subTypeStr=item.typeStr
                                year=DateUtils.getYear()
                                date=System.currentTimeMillis()
                                listJson= Gson().toJson(item)
                                contentJson= Gson().toJson(noteContents)
                                downloadUrl=it
                            })
                            //当加入上传的内容等于全部需要上传时候，则上传
                            if (cloudList.size== NoteDaoManager.getInstance().queryAll().size-nullItems.size)
                                mCloudUploadPresenter.upload(cloudList)
                        }
                    }
                }
                else{
                    //没有内容不上传
                    nullItems.add(item)
                }
            }
        }
    }

    override fun uploadSuccess(cloudIds: MutableList<Int>?) {
        super.uploadSuccess(cloudIds)
        for (i in notebooks.indices){
            val notes= NoteDaoManager.getInstance().queryAll(notebooks[i].title)
            //删除该笔记分类中的所有笔记本及其内容
            for (note in notes){
                NoteDaoManager.getInstance().deleteBean(note)
                NoteContentDaoManager.getInstance().deleteType(note.typeStr,note.title)
                val path= FileAddress().getPathNote(note.typeStr,note.title)
                FileUtils.deleteFile(File(path))
            }
        }
        ItemTypeDaoManager.getInstance().clear(1)
        EventBus.getDefault().post(NOTE_BOOK_MANAGER_EVENT)
    }

}