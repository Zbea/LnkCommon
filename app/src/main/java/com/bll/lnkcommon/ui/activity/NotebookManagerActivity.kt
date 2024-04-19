package com.bll.lnkcommon.ui.activity

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.dialog.InputContentDialog
import com.bll.lnkcommon.manager.ItemTypeDaoManager
import com.bll.lnkcommon.manager.NoteContentDaoManager
import com.bll.lnkcommon.manager.NoteDaoManager
import com.bll.lnkcommon.mvp.model.ItemTypeBean
import com.bll.lnkcommon.ui.adapter.NotebookManagerAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.FileUtils
import kotlinx.android.synthetic.main.ac_list.*
import kotlinx.android.synthetic.main.common_page_number.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.util.*

class NotebookManagerActivity : BaseActivity() {

    private var noteBooks= mutableListOf<ItemTypeBean>()
    private var mAdapter: NotebookManagerAdapter? = null
    private var position=0

    override fun layoutId(): Int {
        return R.layout.ac_list
    }

    override fun initData() {
        noteBooks= ItemTypeDaoManager.getInstance().queryAll(1)
    }

    override fun initView() {
        setPageTitle("笔记本管理")
        disMissView(ll_page_number)
        initRecyclerView()
    }


    private fun initRecyclerView() {
        val layoutParams= LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(DP2PX.dip2px(this,100f), DP2PX.dip2px(this,20f),DP2PX.dip2px(this,100f),DP2PX.dip2px(this,20f))
        layoutParams.weight=1f
        rv_list.layoutParams= layoutParams

        rv_list.layoutManager = LinearLayoutManager(this)//创建布局管理
        mAdapter = NotebookManagerAdapter(R.layout.item_notebook_manager, noteBooks)
        rv_list.adapter = mAdapter
        mAdapter?.bindToRecyclerView(rv_list)
        mAdapter?.setOnItemChildClickListener { adapter, view, position ->
            this.position=position
            if (view.id==R.id.iv_edit){
                editNoteBook(noteBooks[position].title)
            }
            if (view.id==R.id.iv_delete){
                deleteNotebook()
            }
            if (view.id==R.id.iv_top){
                val date=noteBooks[0].date//拿到最小时间
                noteBooks[position].date=date-1000
                ItemTypeDaoManager.getInstance().insertOrReplace(noteBooks[position])
                Collections.swap(noteBooks,position,0)
                setNotify()
            }
        }

    }

    //设置刷新通知
    private fun setNotify(){
        mAdapter?.notifyDataSetChanged()
        EventBus.getDefault().post(Constants.NOTE_TYPE_REFRESH_EVENT)
    }

    //删除
    private fun deleteNotebook(){
        CommonDialog(this).setContent("确定删除笔记本？").builder()
            .setDialogClickListener(object : CommonDialog.OnDialogClickListener {
                override fun cancel() {
                }
                override fun ok() {
                    val noteType=noteBooks[position]
                    noteBooks.removeAt(position)
                    //删除笔记本
                    ItemTypeDaoManager.getInstance().deleteBean(noteType)

                    val notebooks= NoteDaoManager.getInstance().queryAll(noteType.title)
                    //删除该笔记分类中的所有笔记本及其内容
                    for (note in notebooks){
                        NoteDaoManager.getInstance().deleteBean(note)
                        NoteContentDaoManager.getInstance().deleteType(note.typeStr,note.title)
                        val path= FileAddress().getPathNote(note.typeStr,note.title)
                        FileUtils.deleteFile(File(path))
                    }

                    setNotify()
                }

            })
    }

    //修改笔记本
    private fun editNoteBook(content:String){
        InputContentDialog(this,content).builder().setOnDialogClickListener { string ->
            if (ItemTypeDaoManager.getInstance().isExist(string,1)){
                showToast("已存在")
                return@setOnDialogClickListener
            }
            val noteBook=noteBooks[position]
            //修改笔记、内容分类
            val notes=NoteDaoManager.getInstance().queryAll(noteBook.title)
            for (note in notes){
                NoteContentDaoManager.getInstance().editNoteTypes(note.typeStr,note.title,string)
            }
            NoteDaoManager.getInstance().editNotes(noteBook.title,string)

            noteBook.title = string
            ItemTypeDaoManager.getInstance().insertOrReplace(noteBook)

            setNotify()
        }
    }

}