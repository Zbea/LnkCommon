package com.bll.lnkcommon.ui.activity.drawing

import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDrawingActivity
import com.bll.lnkcommon.dialog.ResultStandardDetailsDialog
import com.bll.lnkcommon.dialog.ScoreDetailsDialog
import com.bll.lnkcommon.mvp.model.ResultStandardItem
import com.bll.lnkcommon.mvp.model.ScoreItem
import com.bll.lnkcommon.mvp.model.TeacherHomeworkList
import com.bll.lnkcommon.utils.GlideUtils
import com.bll.lnkcommon.utils.ScoreItemUtils
import kotlinx.android.synthetic.main.ac_drawing.*
import kotlinx.android.synthetic.main.common_drawing_tool.*
import java.util.stream.Collectors

class HomeworkDetailsActivity:BaseDrawingActivity() {

    private var homeworkBean:TeacherHomeworkList.TeacherHomeworkBean?=null
    private var images= mutableListOf<String>()
    private var posImage=0

    var items= mutableListOf<ResultStandardItem>()

    override fun layoutId(): Int {
        return R.layout.ac_drawing
    }

    override fun initData() {
        homeworkBean=intent.getBundleExtra("bundle")?.getSerializable("homeworkBean") as TeacherHomeworkList.TeacherHomeworkBean
        when (homeworkBean?.status!!){
            1->{
                images= homeworkBean?.homeworkContent?.split(",") as MutableList<String>
            }
            2->{
                images= homeworkBean?.submitContent?.split(",") as MutableList<String>
            }
            3->{
                images= homeworkBean?.correctContent?.split(",") as MutableList<String>
            }
        }

        if (homeworkBean?.type==1){
           items=when(homeworkBean?.subType){
                3->{
                    DataBeanManager.getResultStandardItem3s()
                }
                6->{
                    DataBeanManager.getResultStandardItem6s()
                }
                8->{
                    DataBeanManager.getResultStandardItem8s()
                }
                else->{
                    if (homeworkBean?.typeName=="作文作业本"){
                        DataBeanManager.getResultStandardItem2s()
                    }
                    else{
                        DataBeanManager.getResultStandardItems()
                    }
                }
            }
        }

    }

    override fun initView() {
        disMissView(iv_btn,iv_tool,iv_catalog)
        setDisableTouchInput(true)

        if (homeworkBean?.status==3)
            showView(iv_score)

        iv_score.setOnClickListener {
            if (homeworkBean?.type==1&&homeworkBean?.subType!=1){
                ResultStandardDetailsDialog(this,homeworkBean?.title!!,homeworkBean?.score!!.toDouble(),homeworkBean?.question!!,items).builder()
            }
            else{
                val answerImages= homeworkBean?.answerUrl!!.split(",") as MutableList<String>
                ScoreDetailsDialog(this,homeworkBean!!.title,homeworkBean!!.score.toDouble(),homeworkBean?.questionType!!,homeworkBean?.questionMode!!,answerImages,homeworkBean!!.question).builder()
            }
        }

        setContentImage()
    }


    override fun onPageDown() {
        if (posImage< images.size-1){
            posImage+=1
            setContentImage()
        }
    }

    override fun onPageUp() {
        if (posImage>0){
            posImage-=1
            setContentImage()
        }
    }

    /**
     * 设置学生提交图片展示
     */
    private fun setContentImage(){
        tv_page.text="${posImage+1}"
        tv_page_total.text="${images.size}"
        GlideUtils.setImageCacheUrl(this, images[posImage],v_content)
    }

}