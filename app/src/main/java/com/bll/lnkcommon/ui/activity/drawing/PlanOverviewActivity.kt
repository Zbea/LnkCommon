package com.bll.lnkcommon.ui.activity.drawing


import com.bll.lnkcommon.Constants.weekTime
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.MethodManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDrawingActivity
import com.bll.lnkcommon.dialog.CalendarSingleDialog
import com.bll.lnkcommon.utils.DateUtils
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.utils.GlideUtils
import com.bll.lnkcommon.utils.ToolUtils
import kotlinx.android.synthetic.main.ac_plan_overview.*
import kotlinx.android.synthetic.main.common_date_arrow.iv_down
import kotlinx.android.synthetic.main.common_date_arrow.iv_up
import kotlinx.android.synthetic.main.common_date_arrow.tv_date
import kotlinx.android.synthetic.main.common_drawing_tool.*
import java.io.File

class PlanOverviewActivity: BaseDrawingActivity() {

    private var type=1//1月计划 2 周计划
    private var nowYear=0
    private var nowMonth=1
    private var weekStartDate=0L
    private var weekEndDate=0L
    private var posImage = 0
    private var images = mutableListOf<String>()//手写地址

    override fun layoutId(): Int {
        return R.layout.ac_plan_overview
    }

    override fun initData() {
        nowYear=DateUtils.getYear()
        nowMonth=DateUtils.getMonth()

        weekStartDate=DateUtils.getCurrentWeekTimeFrame()[0]
        weekEndDate=DateUtils.getCurrentWeekTimeFrame()[1]
    }
    override fun initView() {
        disMissView(iv_catalog,iv_btn)
        setPageTitle("规划")

        MethodManager.setImageResource(this,R.mipmap.icon_freenote_bg_1,v_content)

        rg_group.setOnCheckedChangeListener { radioGroup, i ->
            type = if (i==R.id.rb_month){
                1
            } else{
                2
            }
            setChangeDate()
        }

        setChangeDate()

        iv_up.setOnClickListener {
            if (type==1){
                if (nowMonth==1){
                    nowMonth=12
                    nowYear-=1
                }
                else{
                    nowMonth-=1
                }
            }
            else{
                weekStartDate-=weekTime
                weekEndDate-=weekTime
            }
            setChangeDate()
        }

        iv_down.setOnClickListener {
            if (type==1){
                if (nowMonth==12){
                    nowMonth=1
                    nowYear+=1
                }
                else{
                    nowMonth+=1
                }
            }
            else{
                weekStartDate+=weekTime
                weekEndDate+=weekTime
            }
            setChangeDate()
        }

        tv_date.setOnClickListener {
            CalendarSingleDialog(this,45f,200f).builder().setOnDateListener{
                if (type==1){
                    val dateStr=DateUtils.longToStringDataNoHour(it).split("-")
                    nowYear=dateStr[0].toInt()
                    nowMonth=dateStr[1].toInt()
                }
                else{
                    val weekLong=DateUtils.getCurrentWeekTimeFrame(it)
                    weekStartDate=weekLong[0]
                    weekEndDate=weekLong[1]
                }
                setChangeDate()
            }
        }

    }

    override fun onPageUp() {
        if (posImage>0)
            posImage-=1
        setContentImage()
    }

    override fun onPageDown() {
        if (posImage<images.size-1){
            posImage+=1
            setContentImage()
        }
        else{
            if (isDrawLastContent()){
                posImage+=1
                images.add(getPathStr())
                setContentImage()
            }
        }
    }


    private fun setChangeDate(){
        if (type==1){
            tv_date.text=nowYear.toString()+"年"+ToolUtils.getFormatNum(nowMonth,"00")+"月"
        }
        else{
            tv_date.text= DateUtils.longToStringDataNoYear(weekStartDate)+"~"+DateUtils.longToStringDataNoYear(weekEndDate)
        }
        posImage=0
        getPathsSize()
        setContentImage()
    }

    /**
     * 更换内容
     */
    private fun setContentImage() {
        tv_page.text = "${posImage + 1}"
        tv_page_total.text="${images.size}"
        elik?.setLoadFilePath(images[posImage], true)
    }

    private fun getPath():String{
        return if (type==1){
            FileAddress().getPathPlan(nowYear,nowMonth)
        }
        else{
            FileAddress().getPathPlan(DateUtils.longToString(weekStartDate))
        }
    }

    private fun getPathStr():String{
        return getPath()+"/${posImage + 1}.png"
    }

    /**
     * 最后一个是否已写
     */
    private fun isDrawLastContent():Boolean{
        return File(images.last()).exists()
    }

    /**
     * 得到文件大小
     */
    private fun getPathsSize(){
        images.clear()
        for (file in FileUtils.getAscFiles(getPath())){
            images.add(file.path)
        }
        if (images.isEmpty()){
            images.add(getPathStr())
        }
    }
}