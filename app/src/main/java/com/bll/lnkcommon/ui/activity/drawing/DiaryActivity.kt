package com.bll.lnkcommon.ui.activity.drawing

import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDrawingActivity
import com.bll.lnkcommon.dialog.CalendarDiaryDialog
import com.bll.lnkcommon.dialog.CatalogDiaryDialog
import com.bll.lnkcommon.dialog.InputContentDialog
import com.bll.lnkcommon.dialog.ModuleSelectDialog
import com.bll.lnkcommon.manager.DiaryDaoManager
import com.bll.lnkcommon.mvp.model.DiaryBean
import com.bll.lnkcommon.utils.*
import kotlinx.android.synthetic.main.ac_diary.*
import kotlinx.android.synthetic.main.ac_plan_overview.v_content
import kotlinx.android.synthetic.main.common_date_arrow.iv_down
import kotlinx.android.synthetic.main.common_date_arrow.iv_up
import kotlinx.android.synthetic.main.common_date_arrow.tv_date
import kotlinx.android.synthetic.main.common_drawing_tool.*
import java.io.File

class DiaryActivity:BaseDrawingActivity() {

    private var nowLong=0L//当前时间
    private var uploadId=0
    private var diaryBean:DiaryBean?=null
    private var images = mutableListOf<String>()//手写地址
    private var posImage=0
    private var bgRes=""

    override fun layoutId(): Int {
        return R.layout.ac_diary
    }

    override fun initData() {
        uploadId=intent.flags
        nowLong=DateUtils.getStartOfDayInMillis()

        if (uploadId==0){
            diaryBean=DiaryDaoManager.getInstance().queryBean(nowLong,uploadId)
            if (diaryBean==null){
                initCurrentDiaryBean()
            }
            changeContent()
        }
        else{
            diaryBean=DiaryDaoManager.getInstance().queryBean(uploadId)
            if (diaryBean!=null){
                changeContent()
            }
        }
    }

    override fun initView() {
        elik?.addOnTopView(ll_date)
        elik?.addOnTopView(tv_digest)

        iv_up.setOnClickListener {
            val lastDiaryBean=DiaryDaoManager.getInstance().queryBeanByDate(nowLong,0,uploadId)
            if (lastDiaryBean!=null){
                saveDiary()
                diaryBean=lastDiaryBean
                changeContent()
            }
        }

        iv_down.setOnClickListener {
            val nextDiaryBean=DiaryDaoManager.getInstance().queryBeanByDate(nowLong,1,uploadId)
            if (nextDiaryBean!=null){
                saveDiary()
                diaryBean=nextDiaryBean
                changeContent()
            }
            else{
                //本地日记：当最新的当天还没有保存时，可以切换到当天
                if (uploadId==0){
                    if (nowLong<DateUtils.getStartOfDayInMillis()){
                        saveDiary()
                        nowLong=DateUtils.getStartOfDayInMillis()
                        initCurrentDiaryBean()
                        changeContent()
                    }
                }
            }
        }

        tv_date.setOnClickListener {
            CalendarDiaryDialog(this,uploadId).builder().setOnDateListener{
                saveDiary()
                nowLong=it
                diaryBean=DiaryDaoManager.getInstance().queryBean(nowLong,uploadId)
                if (nowLong==DateUtils.getStartOfDayInMillis()&&diaryBean == null) {
                    initCurrentDiaryBean()
                }
                changeContent()
            }
        }

        iv_btn.setOnClickListener {
            ModuleSelectDialog(this, 0,DataBeanManager.diaryModules).builder()
                ?.setOnDialogClickListener { moduleBean ->
                    bgRes= ToolUtils.getImageResStr(this, moduleBean.resContentId)
                    diaryBean?.bgRes=bgRes
                    MethodManager.setImageResource(this,ToolUtils.getImageResId(this, bgRes),v_content)
                    SPUtil.putString(Constants.SP_DIARY_BG_SET,bgRes)
                }
        }

        tv_digest.setOnClickListener {
            InputContentDialog(this,if (diaryBean?.title.isNullOrEmpty()) "输入摘要" else diaryBean?.title!!).builder().setOnDialogClickListener{
                diaryBean?.title=it
                saveDiary()
            }
        }

        changeContent()
    }

    /**
     * 初始化
     */
    private fun initCurrentDiaryBean(){
        bgRes= SPUtil.getString(Constants.SP_DIARY_BG_SET).ifEmpty { ToolUtils.getImageResStr(this,R.mipmap.icon_diary_details_bg_1) }
        diaryBean= DiaryBean()
        diaryBean?.userId=if (MethodManager.isLogin()) MethodManager.getUser()?.accountId else 0
        diaryBean?.date=nowLong
        diaryBean?.year=DateUtils.getYear()
        diaryBean?.month=DateUtils.getMonth()
        diaryBean?.bgRes=bgRes
        diaryBean?.paths= mutableListOf(getPath(posImage))
    }

    /**
     * 切换日记
     */
    private fun changeContent(){
        nowLong = diaryBean?.date!!
        if (nowLong==DateUtils.getStartOfDayInMillis()&&uploadId==0){
            showView(iv_btn)
        }
        else{
            disMissView(iv_btn)
        }
        bgRes=diaryBean?.bgRes.toString()
        images= diaryBean?.paths as MutableList<String>
        posImage=diaryBean?.page!!
        tv_date.text=DateUtils.longToStringWeek(nowLong)
        MethodManager.setImageResource(this,ToolUtils.getImageResId(this, bgRes),v_content)
        setContentImage()
    }

    override fun onCatalog() {
        val diaryBeans=DiaryDaoManager.getInstance().queryListByTitle(uploadId)
        CatalogDiaryDialog(this,diaryBeans).builder().setOnDialogClickListener{
            if (nowLong != diaryBeans[it]?.date) {
                saveDiary()
                diaryBean = diaryBeans[it]
                changeContent()
            }
        }
    }

    override fun onPageDown() {
        if (posImage<images.size-1){
            posImage += 1
            setContentImage()
        }
        else{
            if (isDrawLastContent()){
                images.add(getPath(images.size))
            }
            posImage=images.size-1
            setContentImage()
        }
    }

    override fun onPageUp() {
        if (posImage > 0) {
            posImage -= 1
            setContentImage()
        }
    }

    /**
     * 显示内容
     */
    private fun setContentImage() {
        val path = getPath(posImage)
        elik?.setLoadFilePath(path, true)

        tv_page.text = "${posImage + 1}"
        tv_page_total.text="${images.size}"

        setDisableTouchInput(diaryBean?.isUpload!!)
    }

    /**
     * 当前本地日记并且最后一个已写
     */
    private fun isDrawLastContent():Boolean{
        val path = images.last()
        return File(path).exists()&&uploadId==0
    }

    private fun getPath(index:Int):String{
        return FileAddress().getPathDiary(DateUtils.longToStringCalender(nowLong)) + "/${index + 1}.png"
    }

    private fun saveDiary() {
        val path=FileAddress().getPathDiary(DateUtils.longToStringCalender(nowLong))
        if (FileUtils.isExistContent(path)||!diaryBean?.title.isNullOrEmpty()){
            diaryBean?.paths = images
            diaryBean?.page=posImage
            DiaryDaoManager.getInstance().insertOrReplace(diaryBean)
        }
    }

    override fun onPause() {
        super.onPause()
        saveDiary()
    }

}