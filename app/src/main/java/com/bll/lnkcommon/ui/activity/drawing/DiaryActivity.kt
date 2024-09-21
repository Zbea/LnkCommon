package com.bll.lnkcommon.ui.activity.drawing

import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.FileAddress
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
import kotlinx.android.synthetic.main.common_drawing_tool.*
import java.io.File

class DiaryActivity:BaseDrawingActivity() {

    private var nowLong=0L//当前时间
    private var diaryBean:DiaryBean?=null
    private var images = mutableListOf<String>()//手写地址
    private var posImage=0
    private var bgRes=""

    override fun layoutId(): Int {
        return R.layout.ac_diary
    }

    override fun initData() {
        nowLong=DateUtils.getStartOfDayInMillis()

        diaryBean=DiaryDaoManager.getInstance().queryBean(nowLong)
        if (diaryBean==null){
            initCurrentDiaryBean()
        }
    }

    override fun initView() {
        elik?.addOnTopView(ll_date)
        elik?.addOnTopView(tv_digest)

        iv_up.setOnClickListener {
            val lastDiaryBean=DiaryDaoManager.getInstance().queryBean(nowLong,0)
            if (lastDiaryBean!=null){
                saveDiary()
                diaryBean=lastDiaryBean
                nowLong=lastDiaryBean.date
                changeContent()
            }
        }

        iv_down.setOnClickListener {
            val nextDiaryBean=DiaryDaoManager.getInstance().queryBean(nowLong,1)
            if (nextDiaryBean!=null){
                saveDiary()
                diaryBean=nextDiaryBean
                nowLong=nextDiaryBean.date
                changeContent()
            }
            else{
                if (nowLong<DateUtils.getStartOfDayInMillis()){
                    nowLong=DateUtils.getStartOfDayInMillis()
                    initCurrentDiaryBean()
                    changeContent()
                }
            }
        }

        tv_date.setOnClickListener {
            CalendarDiaryDialog(this).builder().setOnDateListener{
                saveDiary()
                nowLong=it
                diaryBean=DiaryDaoManager.getInstance().queryBean(nowLong)
                if (diaryBean==null){
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
                    v_content?.setBackgroundResource(ToolUtils.getImageResId(this, bgRes))
                    SPUtil.putString("dirayBgRes",bgRes)
                }
        }

        tv_digest.setOnClickListener {
            InputContentDialog(this,if (diaryBean?.title.isNullOrEmpty()) "输入摘要" else diaryBean?.title!!).builder().setOnDialogClickListener{
                diaryBean?.title=it
            }
        }

        changeContent()
    }

    /**
     * 初始化
     */
    private fun initCurrentDiaryBean(){
        bgRes= SPUtil.getString("dirayBgRes").ifEmpty { ToolUtils.getImageResStr(this,R.mipmap.icon_diary_details_bg_1) }
        diaryBean= DiaryBean()
        diaryBean?.userId=if (isLoginState()) getUser()?.accountId else 0
        diaryBean?.date=nowLong
        diaryBean?.year=DateUtils.getYear()
        diaryBean?.month=DateUtils.getMonth()
        diaryBean?.bgRes=bgRes
    }

    override fun onCatalog() {
        val diaryBeans=DiaryDaoManager.getInstance().queryListByTitle()
        CatalogDiaryDialog(this,diaryBeans).builder().setOnDialogClickListener{
            saveDiary()
            diaryBean=diaryBeans[it]
            nowLong=diaryBean?.date!!
            changeContent()
        }
    }

    override fun onPageDown() {
        posImage += 1
        setContentImage()
    }

    override fun onPageUp() {
        if (posImage > 0) {
            posImage -= 1
            setContentImage()
        }
    }

    /**
     * 切换日记
     */
    private fun changeContent(){
        bgRes=diaryBean?.bgRes.toString()
        images= diaryBean?.paths as MutableList<String>
        posImage=diaryBean?.page!!
        setContentImage()
    }

    /**
     * 显示内容
     */
    private fun setContentImage() {
        tv_date.text=DateUtils.longToStringWeek(nowLong)
        v_content?.setBackgroundResource(ToolUtils.getImageResId(this, bgRes))

        val path = FileAddress().getPathDiary(DateUtils.longToStringCalender(nowLong)) + "/${posImage + 1}.png"
        //判断路径是否已经创建
        if (!images.contains(path)) {
            images.add(path)
        }
        tv_page.text = "${posImage + 1}"
        tv_page_total.text="${images.size}"

        elik?.setPWEnabled(!diaryBean?.isUpload!!)
        if (diaryBean?.isUpload!!){
            GlideUtils.setImageUrl(this, path, v_content)
        }
        else{
            elik?.setLoadFilePath(path, true)
        }

    }

    private fun saveDiary() {
        val path=FileAddress().getPathDiary(DateUtils.longToStringCalender(nowLong))
        if (FileUtils.isExistContent(path)){
            diaryBean?.paths = images
            diaryBean?.page=posImage
            DiaryDaoManager.getInstance().insertOrReplace(diaryBean)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        saveDiary()
    }

}