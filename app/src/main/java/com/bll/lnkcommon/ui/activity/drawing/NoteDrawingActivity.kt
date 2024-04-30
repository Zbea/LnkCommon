package com.bll.lnkcommon.ui.activity.drawing

import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDrawingActivity
import com.bll.lnkcommon.dialog.CatalogDialog
import com.bll.lnkcommon.dialog.InputContentDialog
import com.bll.lnkcommon.manager.NoteContentDaoManager
import com.bll.lnkcommon.manager.NoteDaoManager
import com.bll.lnkcommon.mvp.model.ItemList
import com.bll.lnkcommon.mvp.model.Note
import com.bll.lnkcommon.mvp.model.NoteContent
import com.bll.lnkcommon.utils.DateUtils
import com.bll.lnkcommon.utils.ToolUtils
import kotlinx.android.synthetic.main.ac_drawing.*
import kotlinx.android.synthetic.main.common_drawing_tool.*

class NoteDrawingActivity : BaseDrawingActivity() {

    private var typeStr = ""
    private var note: Note? = null
    private var noteContent: NoteContent? = null//当前内容
    private var noteContents = mutableListOf<NoteContent>() //所有内容
    private var page = 0//页码

    override fun layoutId(): Int {
        return R.layout.ac_drawing
    }

    override fun initData() {
        val id = intent.getLongExtra("noteId",0)
        note=NoteDaoManager.getInstance().queryBean(id)
        typeStr = note?.typeStr.toString()

        noteContents = NoteContentDaoManager.getInstance().queryAll(typeStr,note?.title)

        if (noteContents.size > 0) {
            noteContent = noteContents[noteContents.size - 1]
            page = note?.page!!
        } else {
            newNoteContent()
        }

    }

    override fun initView() {
        disMissView(iv_btn)
        v_content.setImageResource(ToolUtils.getImageResId(this,note?.contentResId))//设置背景

        tv_page.setOnClickListener {
            InputContentDialog(this,noteContent?.title!!).builder().setOnDialogClickListener { string ->
                noteContent?.title = string
                noteContents[page-1].title = string
                NoteContentDaoManager.getInstance().insertOrReplaceNote(noteContent)
            }
        }

        changeContent()
    }


    override fun onCatalog() {
        var titleStr=""
        val list= mutableListOf<ItemList>()
        for (item in noteContents){
            val itemBean= ItemList()
            itemBean.name=item.title
            itemBean.page=item.page
            if (titleStr != item.title)
            {
                titleStr=item.title
                list.add(itemBean)
            }
        }
        CatalogDialog(this,list).builder().setOnDialogClickListener { position ->
            page = noteContents[position].page
            changeContent()
        }
    }


    override fun onPageDown() {
        val total=noteContents.size-1
        when(page){
            total->{
                newNoteContent()
            }
            else->{
                page += 1
            }
        }
        changeContent()
    }

    override fun onPageUp() {
        if (page>0){
            page-=1
            changeContent()
        }
    }

    //翻页内容更新切换
    private fun changeContent() {
        noteContent = noteContents[page]
        tv_page.text = "${page+1}/${noteContents.size}"
        elik?.setLoadFilePath(noteContent?.filePath!!, true)
    }

    //创建新的作业内容
    private fun newNoteContent() {

        val date=System.currentTimeMillis()
        val path=FileAddress().getPathNote(typeStr,note?.title,date)
        val pathName = DateUtils.longToString(date)

        noteContent = NoteContent()
        noteContent?.date = date
        noteContent?.typeStr=typeStr
        noteContent?.notebookTitle = note?.title
        noteContent?.resId = note?.contentResId
        noteContent?.title="未命名${noteContents.size+1}"
        noteContent?.filePath = "$path/$pathName.tch"
        noteContent?.pathName=pathName
        page = noteContents.size

        NoteContentDaoManager.getInstance().insertOrReplaceNote(noteContent)
        val id= NoteContentDaoManager.getInstance().insertId
        noteContent?.id=id

        noteContents.add(noteContent!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        note?.page=page
        NoteDaoManager.getInstance().insertOrReplace(note)
    }

}