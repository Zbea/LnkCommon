package com.bll.lnkcommon.ui.activity.drawing

import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDrawingActivity
import com.bll.lnkcommon.mvp.model.ExamScoreItem
import com.bll.lnkcommon.mvp.model.TeacherHomeworkList
import com.bll.lnkcommon.utils.GlideUtils
import kotlinx.android.synthetic.main.ac_drawing.*
import kotlinx.android.synthetic.main.common_drawing_tool.*

class HomeworkDetailsActivity:BaseDrawingActivity() {

    private var homeworkBean:TeacherHomeworkList.TeacherHomeworkBean?=null
    private var images= mutableListOf<String>()
    private var posImage=0

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
        scoreMode=homeworkBean?.questionMode!!
        correctMode=homeworkBean?.questionType!!
        if (homeworkBean?.question?.isNotEmpty() == true)
            currentScores= scoreJsonToList(homeworkBean?.question!!) as MutableList<ExamScoreItem>
        if (homeworkBean?.answerUrl?.isNotEmpty()==true)
            answerImages= homeworkBean?.answerUrl!!.split(",") as MutableList<String>
    }

    override fun initView() {
        disMissView(iv_btn,iv_tool,iv_catalog)
        setViewElikUnable(iv_score,ll_score)
        setDisableTouchInput(true)

        if (homeworkBean?.status==3)
            showView(iv_score)
        if (correctMode<3){
            showView(rv_list_score)
            disMissView(rv_list_multi)
        }
        else{
            showView(rv_list_multi)
            disMissView(rv_list_score)
        }

        if (answerImages.size>0){
            showView(tv_answer)
        }
        else{
            disMissView(tv_answer)
        }

        tv_correct_title.text=homeworkBean?.title
        tv_total_score.text=homeworkBean?.score

        initScoreView()
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
        GlideUtils.setImageUrl(this, images[posImage],v_content)
    }

}