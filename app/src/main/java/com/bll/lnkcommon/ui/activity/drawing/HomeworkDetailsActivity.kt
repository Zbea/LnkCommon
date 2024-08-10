package com.bll.lnkcommon.ui.activity.drawing

import android.text.TextUtils
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDrawingActivity
import com.bll.lnkcommon.dialog.ImageDialog
import com.bll.lnkcommon.mvp.model.ExamScoreItem
import com.bll.lnkcommon.mvp.model.TeacherHomeworkList
import com.bll.lnkcommon.ui.adapter.TopicMultiScoreAdapter
import com.bll.lnkcommon.ui.adapter.TopicScoreAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.GlideUtils
import com.bll.lnkcommon.widget.SpaceGridItemDeco
import com.bll.lnkcommon.widget.SpaceItemDeco
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.ac_drawing.*
import kotlinx.android.synthetic.main.ac_drawing.v_content
import kotlinx.android.synthetic.main.common_drawing_tool.*

class HomeworkDetailsActivity:BaseDrawingActivity() {

    private var homeworkBean:TeacherHomeworkList.TeacherHomeworkBean?=null
    private var scoreMode=0
    private var correctMode=0
    private var currentScores= mutableListOf<ExamScoreItem>()
    private var answerImages= mutableListOf<String>()
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
        tv_total_score.text=homeworkBean?.score!!.toString()

        initScoreView()
        setContentImage()
    }

    /**
     * 设置成绩分数
     */
    private fun initScoreView(){
        iv_correct_close?.setOnClickListener {
            disMissView(ll_score)
            showView(iv_score)
        }

        iv_score.setOnClickListener {
            disMissView(iv_score)
            showView(ll_score)
        }

        tv_answer.setOnClickListener {
            if (answerImages.size>0)
                ImageDialog(this, answerImages).builder()
        }

        if (correctMode>0){
            if (correctMode<3){
                rv_list_score?.layoutManager = GridLayoutManager(this,2)
                TopicScoreAdapter(R.layout.item_topic_child_score,scoreMode,correctMode,currentScores).apply {
                    rv_list_score?.adapter = this
                    bindToRecyclerView(rv_list_score)
                }
                rv_list_score.addItemDecoration(SpaceGridItemDeco(2,DP2PX.dip2px(this,15f)))
            }
            else{
                rv_list_multi?.layoutManager = LinearLayoutManager(this)
                TopicMultiScoreAdapter(R.layout.item_topic_multi_score,scoreMode,currentScores).apply {
                    rv_list_multi?.adapter = this
                    bindToRecyclerView(rv_list_multi)
                }
                rv_list_multi.addItemDecoration(SpaceItemDeco(DP2PX.dip2px(this,15f)))
            }
        }
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

    /**
     * 格式序列化  题目分数转行list集合
     */
    private fun scoreJsonToList(json:String):List<ExamScoreItem>{
        var items= mutableListOf<ExamScoreItem>()
        if (correctMode<3){
            items= Gson().fromJson(json, object : TypeToken<List<ExamScoreItem>>() {}.type) as MutableList<ExamScoreItem>
            for (item in items){
                item.sort=items.indexOf(item)
            }
        }
        else{
            val scores= Gson().fromJson(json, object : TypeToken<List<List<ExamScoreItem>>>() {}.type) as MutableList<List<ExamScoreItem>>
            for (i in scores.indices){
                items.add(ExamScoreItem().apply {
                    sort=i
                    if (scoreMode==1){
                        var totalLabel=0
                        for (item in scores[i]){
                            totalLabel+=item.label
                        }
                        label=totalLabel
                        var totalItem=0
                        for (item in scores[i]){
                            totalItem+= getScore(item.score)
                        }
                        score=totalItem.toString()
                    }
                    else{
                        var totalRight=0
                        for (item in scores[i]){
                            item.score=item.result.toString()
                            if (item.result==1) {
                                totalRight+= 1
                            }
                        }
                        score=totalRight.toString()
                    }
                    for (item in scores[i]){
                        item.sort=scores[i].indexOf(item)
                    }
                    childScores=scores[i]
                })
            }
        }
        return items
    }

    private fun getScore(scoreStr: String?): Int {
        return if (scoreStr == null || scoreStr.isEmpty() || !TextUtils.isDigitsOnly(scoreStr)) {
            0
        } else {
            Integer.valueOf(scoreStr)
        }
    }
}