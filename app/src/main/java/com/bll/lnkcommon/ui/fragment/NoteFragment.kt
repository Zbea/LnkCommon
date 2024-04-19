package com.bll.lnkcommon.ui.fragment

import PopupClick
import android.content.Intent
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.Constants.NOTE_TYPE_REFRESH_EVENT
import com.bll.lnkcommon.Constants.NOTE_EVENT
import com.bll.lnkcommon.Constants.USER_EVENT
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.dialog.*
import com.bll.lnkcommon.manager.ItemTypeDaoManager
import com.bll.lnkcommon.manager.NoteContentDaoManager
import com.bll.lnkcommon.manager.NoteDaoManager
import com.bll.lnkcommon.mvp.model.CloudListBean
import com.bll.lnkcommon.mvp.model.ItemTypeBean
import com.bll.lnkcommon.mvp.model.Note
import com.bll.lnkcommon.mvp.model.PopupBean
import com.bll.lnkcommon.mvp.model.PrivacyPassword
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
    private var privacyPassword:PrivacyPassword?=null

    override fun getLayoutId(): Int {
        return R.layout.fragment_note
    }
    override fun initView() {
        pageSize=10

        popupBeans.add(PopupBean(0, getString(R.string.notebook_manager)))
        popupBeans.add(PopupBean(1, getString(R.string.notebook_create)))

        setTitle(DataBeanManager.mainListTitle[3])
        showView(iv_manager)

        privacyPassword=MethodManager.getPrivacyPassword(1)

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
            if (positionType==0&&privacyPassword!=null&&!note.isCancelPassword){
                PrivacyPasswordDialog(requireActivity(),1).builder().setOnDialogClickListener{
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
                                //删除笔记本
                                NoteDaoManager.getInstance().deleteBean(note)
                                //删除笔记本中的所有笔记
                                NoteContentDaoManager.getInstance().deleteType(note.typeStr, note.title)
                                val path= FileAddress().getPathNote(note.typeStr,note.title)
                                FileUtils.deleteFile(File(path))

                                notes.remove(note)
                                if (pageIndex>1&&notes.size==0){
                                    pageIndex-=1
                                }
                                fetchData()
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
                            NoteContentDaoManager.getInstance().editNoteTitles(note.typeStr,note.title,string)
                            note.title = string
                            mAdapter?.notifyItemChanged(position)
                            NoteDaoManager.getInstance().insertOrReplace(note)
                        }
                }
                R.id.iv_password->{
                    if (privacyPassword==null){
                        PrivacyPasswordCreateDialog(requireActivity(),1).builder().setOnDialogClickListener{
                            privacyPassword=it
                            mAdapter?.notifyDataSetChanged()
                            showToast("密本密码设置成功")
                        }
                    }
                    else{
                        val titleStr=if (note.isCancelPassword) "确定设置密码？" else "确定取消密码？"
                        CommonDialog(requireActivity()).setContent(titleStr).builder().setDialogClickListener(object : CommonDialog.OnDialogClickListener {
                            override fun cancel() {
                            }
                            override fun ok() {
                                PrivacyPasswordDialog(requireActivity(),1).builder().setOnDialogClickListener{
                                    note.isCancelPassword=!note.isCancelPassword
                                    NoteDaoManager.getInstance().insertOrReplace(note)
                                    mAdapter?.notifyItemChanged(position)
                                }
                            }
                        })
                    }
                }
                R.id.iv_upload->{
                    val noteContents = NoteContentDaoManager.getInstance().queryAll(note.typeStr,note.title)
                    if (noteContents.size==0){
                        showToast("主题没有内容无法上传")
                        return@setOnItemChildClickListener
                    }
                    CommonDialog(requireActivity()).setContent("确定上传主题到云书库？").builder().setDialogClickListener(object : CommonDialog.OnDialogClickListener {
                        override fun cancel() {
                        }
                        override fun ok() {
                            mQiniuPresenter.getToken()
                        }
                    })
                }
            }
        }

        val view =requireActivity().layoutInflater.inflate(R.layout.common_add_view,null)
        view.setOnClickListener {
            NoteModuleAddDialog(requireContext(),1).builder()
                ?.setOnDialogClickListener { moduleBean ->
                    createNote(ToolUtils.getImageResStr(activity, moduleBean.resContentId))
                }
        }
        mAdapter?.addFooterView(view)
    }

    //顶部弹出选择
    private fun setTopSelectView() {
        PopupClick(requireActivity(), popupBeans, iv_manager, 5).builder().setOnSelectListener { item ->
            when (item.id) {
                0 -> customStartActivity(Intent(activity, NotebookManagerActivity::class.java))
                1 -> addNoteBookType()
            }
        }
    }

    /**
     * tab数据设置
     */
    private fun findTabs() {
        notebooks.clear()
        if (isLoginState()){
            notebooks.add(ItemTypeBean().apply {
                title = getString(R.string.note_tab_diary)
            })
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

                NoteDaoManager.getInstance().insertOrReplace(note)
                if(notes.size==10){
                    pageIndex+=1
                }
                fetchData()
            }
    }

    override fun onEventBusMessage(msgFlag: String) {
        when(msgFlag){
            USER_EVENT->{
                privacyPassword=MethodManager.getPrivacyPassword(1)
                positionType=0
                findTabs()
            }
            NOTE_TYPE_REFRESH_EVENT->{
                findTabs()
            }
            NOTE_EVENT->{
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
            mAdapter?.setNewData(notes)
        }
    }

    override fun onUpload(token: String) {
        cloudList.clear()
        val note=notes[position]
        val path=FileAddress().getPathNote(note.typeStr,note.title)
        //获取笔记所有内容
        val noteContents = NoteContentDaoManager.getInstance().queryAll(note.typeStr,note.title)
        FileUploadManager(token).apply {
            startUpload(path,note.title)
            setCallBack{
                cloudList.add(CloudListBean().apply {
                    type=3
                    subType=ToolUtils.getDateId()
                    subTypeStr=note.typeStr
                    date=note.date
                    listJson= Gson().toJson(note)
                    contentJson= Gson().toJson(noteContents)
                    skip=1
                    downloadUrl=it
                })
                mCloudUploadPresenter.upload(cloudList)
            }
        }
    }

    override fun uploadSuccess(cloudIds: MutableList<Int>?) {
        super.uploadSuccess(cloudIds)
        val note=notes[position]
        NoteDaoManager.getInstance().deleteBean(note)
        NoteContentDaoManager.getInstance().deleteType(typeStr,note.title)
        val path= FileAddress().getPathNote(typeStr,note.title)
        FileUtils.deleteFile(File(path))
        mAdapter?.remove(position)
    }

}