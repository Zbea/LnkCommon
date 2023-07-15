package com.bll.lnkcommon.ui.activity

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.view.EinkPWInterface
import android.view.PWDrawObjectHandler
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.CatalogDialog
import com.bll.lnkcommon.dialog.InputContentDialog
import com.bll.lnkcommon.manager.NoteContentDaoManager
import com.bll.lnkcommon.mvp.model.ItemList
import com.bll.lnkcommon.mvp.model.Note
import com.bll.lnkcommon.mvp.model.NoteContent
import com.bll.lnkcommon.utils.DateUtils
import com.bll.lnkcommon.utils.ToolUtils
import kotlinx.android.synthetic.main.ac_note_draw_details.*
import kotlinx.android.synthetic.main.common_drawing_bottom.*

class NoteDrawingActivity : BaseActivity() {

    private var elik:EinkPWInterface?=null
    private var isErasure=false
    private var typeStr = ""
    private var note: Note? = null
    private var noteContent: NoteContent? = null//当前内容
    private var noteContents = mutableListOf<NoteContent>() //所有内容
    private var page = 0//页码

    override fun layoutId(): Int {
        return R.layout.ac_note_draw_details
    }

    override fun initData() {
        val bundle = intent.getBundleExtra("bundle")
        note = bundle?.getSerializable("noteBundle") as Note
        typeStr = note?.typeStr.toString()

        noteContents = NoteContentDaoManager.getInstance().queryAll(typeStr,note?.title)

        if (noteContents.size > 0) {
            noteContent = noteContents[noteContents.size - 1]
            page = noteContents.size - 1
        } else {
            newNoteContent()
        }

    }


    override fun initView() {

        v_content.setImageResource(ToolUtils.getImageResId(this,note?.contentResId))//设置背景
        elik = v_content.pwInterFace
        changeContent()

        tv_title.setOnClickListener {
            val title=tv_title.text.toString()
            InputContentDialog(this,title).builder().setOnDialogClickListener { string ->
                tv_title.text = string
                noteContent?.title = string
                noteContents[page-1].title = string
                NoteContentDaoManager.getInstance().insertOrReplaceNote(noteContent)
            }
        }


        iv_page_down.setOnClickListener {
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

        iv_page_up.setOnClickListener {
            if (page>0){
                page-=1
                changeContent()
            }
        }

        iv_catalog.setOnClickListener {
            showCatalog()
        }

        iv_erasure.setOnClickListener {
            isErasure=!isErasure
            if (isErasure){
                iv_erasure?.setImageResource(R.mipmap.icon_draw_erasure_big)
                elik?.drawObjectType = PWDrawObjectHandler.DRAW_OBJ_CHOICERASE
            }
            else{
                iv_erasure?.setImageResource(R.mipmap.icon_draw_erasure)
                elik?.drawObjectType =PWDrawObjectHandler.DRAW_OBJ_RANDOM_PEN
            }
        }

    }



    /**
     * 弹出目录
     */
    private fun showCatalog(){
        var titleStr=""
        val list= mutableListOf<ItemList>()
        for (item in noteContents){
            val itemList= ItemList()
            itemList.name=item.title
            itemList.page=item.page
            if (titleStr != item.title)
            {
                titleStr=item.title
                list.add(itemList)
            }

        }
        CatalogDialog(this,list,0).builder().setOnDialogClickListener { position ->
            page = noteContents[position].page
            changeContent()
        }
    }

    //翻页内容更新切换
    private fun changeContent() {

        noteContent = noteContents[page]
        tv_title.text=noteContent?.title
        tv_page.text = (page + 1).toString()

        elik?.setPWEnabled(true)
        elik?.setLoadFilePath(noteContent?.filePath!!, true)
        elik?.setDrawEventListener(object : EinkPWInterface.PWDrawEvent {
            override fun onTouchDrawStart(p0: Bitmap?, p1: Boolean) {
            }

            override fun onTouchDrawEnd(p0: Bitmap?, p1: Rect?, p2: ArrayList<Point>?) {
            }

            override fun onOneWordDone(p0: Bitmap?, p1: Rect?) {
                elik?.saveBitmap(true) {}
            }

        })
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
        noteContent?.page = noteContents.size
        page = noteContents.size

        NoteContentDaoManager.getInstance().insertOrReplaceNote(noteContent)
        val id= NoteContentDaoManager.getInstance().insertId
        noteContent?.id=id

        noteContents.add(noteContent!!)
    }

}