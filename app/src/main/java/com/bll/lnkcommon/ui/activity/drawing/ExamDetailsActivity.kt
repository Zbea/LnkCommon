package com.bll.lnkcommon.ui.activity.drawing

import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDrawingActivity
import com.bll.lnkcommon.dialog.ScoreDetailsDialog
import com.bll.lnkcommon.mvp.model.ExamList
import com.bll.lnkcommon.mvp.model.ScoreItem
import com.bll.lnkcommon.utils.GlideUtils
import com.bll.lnkcommon.utils.ScoreItemUtils
import kotlinx.android.synthetic.main.ac_drawing.*
import kotlinx.android.synthetic.main.common_drawing_tool.*

class ExamDetailsActivity:BaseDrawingActivity() {

    private var examBean:ExamList.ExamBean?=null
    private var images= mutableListOf<String>()
    private var posImage=0

    override fun layoutId(): Int {
        return R.layout.ac_drawing
    }

    override fun initData() {
        examBean=intent.getBundleExtra("bundle")?.getSerializable("examBean") as ExamList.ExamBean
        images= examBean?.teacherUrl?.split(",") as MutableList<String>
    }

    override fun initView() {
        disMissView(iv_btn,iv_tool,iv_catalog)
        setDisableTouchInput(true)
        showView(iv_score)

        iv_score.setOnClickListener {
            val answerImages=if (examBean?.answerUrl.isNullOrEmpty()){
                mutableListOf()
            }
            else{
                examBean?.answerUrl?.split(",") as MutableList<String>
            }
            ScoreDetailsDialog(this,examBean!!.examName,examBean!!.score.toDouble(),examBean?.questionType!!,examBean?.questionMode!!,answerImages,examBean!!.question).builder()
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