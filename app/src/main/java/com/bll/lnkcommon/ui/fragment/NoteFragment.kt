package com.bll.lnkcommon.ui.fragment

import PopupClick
import android.content.Intent
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.Constants.NOTE_BOOK_MANAGER_EVENT
import com.bll.lnkcommon.Constants.NOTE_EVENT
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.dialog.InputContentDialog
import com.bll.lnkcommon.dialog.NoteModuleAddDialog
import com.bll.lnkcommon.manager.NoteContentDaoManager
import com.bll.lnkcommon.manager.NoteDaoManager
import com.bll.lnkcommon.manager.NotebookDaoManager
import com.bll.lnkcommon.mvp.model.Note
import com.bll.lnkcommon.mvp.model.Notebook
import com.bll.lnkcommon.mvp.model.PopupBean
import com.bll.lnkcommon.ui.activity.NotebookManagerActivity
import com.bll.lnkcommon.ui.adapter.NoteAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.utils.ToolUtils
import kotlinx.android.synthetic.main.common_radiogroup.*
import kotlinx.android.synthetic.main.common_title.*
import kotlinx.android.synthetic.main.fragment_note.*
import org.greenrobot.eventbus.EventBus
import java.io.File

class NoteFragment:BaseFragment() {

    private var popupBeans = mutableListOf<PopupBean>()
    private var notebooks = mutableListOf<Notebook>()
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

        popupBeans.add(PopupBean(0, "笔记本管理", true))
        popupBeans.add(PopupBean(1, "新建笔记本", false))
        popupBeans.add(PopupBean(2, "新建笔记", false))

        setTitle(DataBeanManager.getMainData()[2].name)
        showView(iv_manager)

        iv_manager?.setOnClickListener {
            setTopSelectView()
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
            gotoNote(note)
        }
        mAdapter?.setOnItemChildClickListener { adapter, view, position ->
            this.position = position
            if (view.id == R.id.iv_delete) {
                deleteNote()
            }
            if (view.id == R.id.iv_edit) {
                editNote(notes[position].title)
            }

        }
    }

    //顶部弹出选择
    private fun setTopSelectView() {
        PopupClick(requireActivity(), popupBeans, iv_manager, 20).builder().setOnSelectListener { item ->
            when (item.id) {
                0 -> startActivity(Intent(activity, NotebookManagerActivity::class.java))
                1 -> addNoteBookType()
                else -> {
                    NoteModuleAddDialog(requireContext(), if (typeStr == resources.getString(R.string.note_tab_diary)) 0 else 1).builder()
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
        notebooks.add(Notebook().apply {
            title = getString(R.string.note_tab_diary)
        })
        notebooks.addAll(NotebookDaoManager.getInstance().queryAll())
        if (positionType>=notebooks.size){
            positionType=0
        }
        typeStr = notebooks[positionType].title
        initTab()
        fetchData()
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
                if (NotebookDaoManager.getInstance().isExist(string)){
                    showToast("已存在")
                }
                else{
                    val noteBook = Notebook()
                    noteBook.title = string
                    noteBook.date=System.currentTimeMillis()
                    notebooks.add(noteBook)
                    NotebookDaoManager.getInstance().insertOrReplace(noteBook)
                    initTab()
                }
            }
    }

    //新建笔记
    private fun createNote(resId:String) {
        val note = Note()
        InputContentDialog(requireContext(),  "请输入笔记").builder()
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

    //修改笔记
    private fun editNote(content: String) {
        InputContentDialog(requireContext(), content).builder()
            .setOnDialogClickListener { string ->
                if (NoteDaoManager.getInstance().isExist(typeStr,string)){
                    showToast("已存在")
                    return@setOnDialogClickListener
                }
                val note=notes[position]
                //修改内容分类
                NoteContentDaoManager.getInstance().editNotes(note.typeStr,note.title,string)
                note.title = string
                mAdapter?.notifyItemChanged(position)
                NoteDaoManager.getInstance().insertOrReplace(note)
                EventBus.getDefault().post(NOTE_EVENT)//更新全局通知
            }
    }

    //删除
    private fun deleteNote() {
        CommonDialog(requireActivity()).setContent("确定要删除笔记？").builder()
            .setDialogClickListener(object : CommonDialog.OnDialogClickListener {
                override fun cancel() {
                }
                override fun ok() {
                    val note = notes[position]
                    mAdapter?.remove(position)
                    //删除笔记本
                    NoteDaoManager.getInstance().deleteBean(note)
                    //删除笔记本中的所有笔记
                    NoteContentDaoManager.getInstance().deleteType(note.typeStr, note.title)
                    EventBus.getDefault().post(NOTE_EVENT)//更新全局通知
                    val path= FileAddress().getPathNote(note.typeStr,note.title)
                    FileUtils.deleteFile(File(path))
                }

            })
    }

    override fun onEventBusMessage(msgFlag: String) {
        when(msgFlag){
            NOTE_BOOK_MANAGER_EVENT->{
                findTabs()
            }
            NOTE_EVENT->{
                fetchData()
            }
        }
    }

    override fun fetchData() {
        notes = NoteDaoManager.getInstance().queryAll(typeStr, pageIndex, pageSize)
        val total = NoteDaoManager.getInstance().queryAll(typeStr)
        setPageNumber(total.size)
        mAdapter?.setNewData(notes)
    }


}