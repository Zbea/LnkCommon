package com.bll.lnkcommon.ui.activity.drawing

import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDrawingActivity
import com.bll.lnkcommon.dialog.ResultStandardDetailsDialog
import com.bll.lnkcommon.dialog.ScoreDetailsDialog
import com.bll.lnkcommon.mvp.model.ResultStandardItem
import com.bll.lnkcommon.mvp.model.TeacherHomeworkList
import com.bll.lnkcommon.utils.GlideUtils
import kotlinx.android.synthetic.main.ac_drawing.iv_score
import kotlinx.android.synthetic.main.ac_drawing.v_content
import kotlinx.android.synthetic.main.common_drawing_tool.iv_btn
import kotlinx.android.synthetic.main.common_drawing_tool.iv_catalog
import kotlinx.android.synthetic.main.common_drawing_tool.iv_tool
import kotlinx.android.synthetic.main.common_drawing_tool.tv_page
import kotlinx.android.synthetic.main.common_drawing_tool.tv_page_total
import java.util.stream.Collectors

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
    }

    override fun initView() {
        disMissView(iv_btn,iv_tool,iv_catalog)
        setDisableTouchInput(true)

        if (homeworkBean?.status==3)
            showView(iv_score)

        iv_score.setOnClickListener {
            if (homeworkBean?.type==1&&homeworkBean?.subType!=1){
                val items=DataBeanManager.getResultStandardItems(homeworkBean!!.subType,homeworkBean!!.typeName,homeworkBean!!.questionType).stream().collect(Collectors.toList())
                ResultStandardDetailsDialog(this,homeworkBean?.title!!,homeworkBean?.score!!.toDouble(),if (homeworkBean?.subType==10)10 else homeworkBean?.questionType!!,homeworkBean?.question!!,items).builder()
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