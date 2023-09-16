package com.bll.lnkcommon.ui.activity


import com.bll.lnkcommon.base.BaseDrawingActivity
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.utils.DateUtils
import com.bll.lnkcommon.utils.FileUtils
import com.bll.lnkcommon.utils.ToolUtils
import kotlinx.android.synthetic.main.ac_plan_overview.*
import java.io.File

class PlanOverviewActivity: BaseDrawingActivity() {

    private val weekTime=7*24*60*60*1000
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
    }
    override fun initView() {
        setPageTitle("计划总览")
        elik=v_content.pwInterFace

        rg_group.setOnCheckedChangeListener { radioGroup, i ->
            type = if (i==R.id.rb_month){
                1
            } else{
                2
            }
            setChangeDate()
        }

        nowYear=DateUtils.getYear()
        nowMonth=DateUtils.getMonth()

        weekStartDate=DateUtils.getCurrentWeekTimeFrame()[0]
        weekEndDate=DateUtils.getCurrentWeekTimeFrame()[1]
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

    }

    override fun onPageUp() {
        if (posImage>0)
            posImage-=1
        setContentImage()
    }

    override fun onPageDown() {
        posImage+=1
        setContentImage()
    }

    /**
     * 得到文件大小
     */
    private fun getPathsSize(){
        val path=if (type==1){
            FileAddress().getPathPlan(nowYear,nowMonth)
        }
        else{
            FileAddress().getPathPlan(DateUtils.longToString(weekStartDate))
        }
        images.clear()
        if (File(path).exists()){
            for (file in FileUtils.getFiles(path,"tch")){
                images.add(file.path)
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
        val path = if (type==1){
            FileAddress().getPathPlan(nowYear,nowMonth)+ "/${posImage + 1}.png"
        }
        else{
            FileAddress().getPathPlan(DateUtils.longToString(weekStartDate))+ "/${posImage + 1}.png"
        }
        //判断路径是否已经创建
        if (!images.contains(path)) {
            images.add(path)
        }
        tv_page.text = "${posImage + 1}/${images.size}"

        elik?.setLoadFilePath(path, true)
    }

}