package com.bll.lnkcommon.base

import PopupClick
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.os.Handler
import android.text.TextUtils
import android.view.EinkPWInterface
import android.view.PWDrawObjectHandler
import android.view.PWInputPoint
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.dialog.AppToolDialog
import com.bll.lnkcommon.dialog.GeometryScaleDialog
import com.bll.lnkcommon.dialog.ImageDialog
import com.bll.lnkcommon.mvp.model.ExamScoreItem
import com.bll.lnkcommon.mvp.model.PopupBean
import com.bll.lnkcommon.ui.adapter.TopicMultiScoreAdapter
import com.bll.lnkcommon.ui.adapter.TopicScoreAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.ToolUtils
import com.bll.lnkcommon.widget.SpaceGridItemDeco
import com.bll.lnkcommon.widget.SpaceItemDeco
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.ac_drawing.iv_correct_close
import kotlinx.android.synthetic.main.ac_drawing.iv_geometry
import kotlinx.android.synthetic.main.ac_drawing.iv_score
import kotlinx.android.synthetic.main.ac_drawing.ll_geometry
import kotlinx.android.synthetic.main.ac_drawing.ll_score
import kotlinx.android.synthetic.main.ac_drawing.rv_list_multi
import kotlinx.android.synthetic.main.ac_drawing.rv_list_score
import kotlinx.android.synthetic.main.ac_drawing.tv_answer
import kotlinx.android.synthetic.main.ac_drawing.v_content
import kotlinx.android.synthetic.main.common_drawing_geometry.iv_angle
import kotlinx.android.synthetic.main.common_drawing_geometry.iv_arc
import kotlinx.android.synthetic.main.common_drawing_geometry.iv_axis
import kotlinx.android.synthetic.main.common_drawing_geometry.iv_circle
import kotlinx.android.synthetic.main.common_drawing_geometry.iv_line
import kotlinx.android.synthetic.main.common_drawing_geometry.iv_oval
import kotlinx.android.synthetic.main.common_drawing_geometry.iv_parabola
import kotlinx.android.synthetic.main.common_drawing_geometry.iv_pen
import kotlinx.android.synthetic.main.common_drawing_geometry.iv_rectangle
import kotlinx.android.synthetic.main.common_drawing_geometry.iv_vertical
import kotlinx.android.synthetic.main.common_drawing_geometry.ll_angle
import kotlinx.android.synthetic.main.common_drawing_geometry.ll_arc
import kotlinx.android.synthetic.main.common_drawing_geometry.ll_axis
import kotlinx.android.synthetic.main.common_drawing_geometry.ll_circle
import kotlinx.android.synthetic.main.common_drawing_geometry.ll_line
import kotlinx.android.synthetic.main.common_drawing_geometry.ll_oval
import kotlinx.android.synthetic.main.common_drawing_geometry.ll_parabola
import kotlinx.android.synthetic.main.common_drawing_geometry.ll_pen
import kotlinx.android.synthetic.main.common_drawing_geometry.ll_rectangle
import kotlinx.android.synthetic.main.common_drawing_geometry.ll_vertical
import kotlinx.android.synthetic.main.common_drawing_geometry.tv_axis
import kotlinx.android.synthetic.main.common_drawing_geometry.tv_circle
import kotlinx.android.synthetic.main.common_drawing_geometry.tv_gray_line
import kotlinx.android.synthetic.main.common_drawing_geometry.tv_out
import kotlinx.android.synthetic.main.common_drawing_geometry.tv_parallel
import kotlinx.android.synthetic.main.common_drawing_geometry.tv_reduce
import kotlinx.android.synthetic.main.common_drawing_geometry.tv_revocation
import kotlinx.android.synthetic.main.common_drawing_geometry.tv_scale
import kotlinx.android.synthetic.main.common_drawing_tool.iv_catalog
import kotlinx.android.synthetic.main.common_drawing_tool.iv_erasure
import kotlinx.android.synthetic.main.common_drawing_tool.iv_page_down
import kotlinx.android.synthetic.main.common_drawing_tool.iv_page_up
import kotlinx.android.synthetic.main.common_drawing_tool.iv_tool
import java.util.regex.Pattern


abstract class BaseDrawingActivity : BaseActivity() {

    var elik: EinkPWInterface? = null
    var isErasure=false
    var isTitleClick=true//标题是否可以编
    private var circlePos=0
    private var axisPos=0
    private var isGeometry=false//是否处于几何绘图
    private var isParallel=false//是否选中平行
    private var isCurrent=false//当前支持的几何绘图笔形
    private var isScale=false//是否选中刻度
    private var currentGeometry=0
    private var currentDrawObj=PWDrawObjectHandler.DRAW_OBJ_RANDOM_PEN//当前笔形
    var scoreMode=0
    var correctMode=0
    var answerImages= mutableListOf<String>()
    var currentScores= mutableListOf<ExamScoreItem>()

    override fun initCreate() {
        onInStanceElik()

        initClick()
        initGeometryView()
    }

    open fun onInStanceElik(){
        if (v_content!=null){
            elik = v_content?.pwInterFace
        }
    }

    private fun initClick(){
        iv_tool?.setOnClickListener {
            showDialogAppTool()
        }

        iv_erasure?.setOnClickListener {
            isErasure=!isErasure
            if (isErasure){
                iv_erasure?.setImageResource(R.mipmap.icon_draw_erasure_big)
                onErasure()
            }
            else{
                stopErasure()
            }
        }

        iv_page_up?.setOnClickListener {
            onPageUp()
        }

        iv_page_down?.setOnClickListener {
            onPageDown()
        }

        iv_catalog?.setOnClickListener {
            onCatalog()
        }

    }

    /**
     * 几何绘图
     */
    private fun initGeometryView(){

        val popsCircle= mutableListOf<PopupBean>()
        popsCircle.add(PopupBean(0,getString(R.string.circle_1),R.mipmap.icon_geometry_circle_1))
        popsCircle.add(PopupBean(1,getString(R.string.circle_2),R.mipmap.icon_geometry_circle_2))
        popsCircle.add(PopupBean(2,getString(R.string.circle_3),R.mipmap.icon_geometry_circle_3))

        val popsAxis= mutableListOf<PopupBean>()
        popsAxis.add(PopupBean(0,getString(R.string.axis_one),R.mipmap.icon_geometry_axis_1))
        popsAxis.add(PopupBean(1,getString(R.string.axis_two),R.mipmap.icon_geometry_axis_2))
        popsAxis.add(PopupBean(2,getString(R.string.axis_three),R.mipmap.icon_geometry_axis_3))

        val pops= mutableListOf<PopupBean>()
        pops.add(PopupBean(0,getString(R.string.line_black),false))
        pops.add(PopupBean(1,getString(R.string.line_gray),false))
        pops.add(PopupBean(2,getString(R.string.line_dotted),false))

        iv_geometry?.setOnClickListener {
            setViewElikUnable(ll_geometry)
            showView(ll_geometry)
            disMissView(iv_geometry)
        }

        iv_line?.setOnClickListener {
            setCheckView(ll_line)
            setDrawOjectType(PWDrawObjectHandler.DRAW_OBJ_LINE)
            currentGeometry=1
        }

        iv_rectangle?.setOnClickListener {
            setCheckView(ll_rectangle)
            setDrawOjectType(PWDrawObjectHandler.DRAW_OBJ_RECTANGLE)
            currentGeometry=2
        }

        tv_circle?.setOnClickListener {
            PopupClick(this,popsCircle,tv_circle,5).builder().setOnSelectListener{
                iv_circle.setImageResource(it.resId)
                circlePos=it.id
                setEilkCircle()
            }
        }

        iv_circle?.setOnClickListener {
            setEilkCircle()
        }

        iv_arc?.setOnClickListener {
            setCheckView(ll_arc)
            setDrawOjectType(PWDrawObjectHandler.DRAW_OBJ_ARC)
            currentGeometry=4
        }

        iv_oval?.setOnClickListener {
            setCheckView(ll_oval)
            setDrawOjectType(PWDrawObjectHandler.DRAW_OBJ_OVAL)
            currentGeometry=5
        }

        iv_vertical?.setOnClickListener {
            setCheckView(ll_vertical)
            setDrawOjectType(PWDrawObjectHandler.DRAW_OBJ_VERTICALLINE)
            currentGeometry=6
        }

        iv_parabola?.setOnClickListener {
            setCheckView(ll_parabola)
            setDrawOjectType(PWDrawObjectHandler.DRAW_OBJ_PARABOLA)
            currentGeometry=7
        }

        iv_angle?.setOnClickListener {
            setCheckView(ll_angle)
            setDrawOjectType(PWDrawObjectHandler.DRAW_OBJ_ANGLE)
            currentGeometry=8
        }

        tv_axis?.setOnClickListener {
            PopupClick(this,popsAxis,tv_axis,5).builder().setOnSelectListener{
                iv_axis?.setImageResource(it.resId)
                axisPos=it.id
                setEilkAxis()
            }
        }

        iv_axis?.setOnClickListener {
            setEilkAxis()
        }

        iv_pen?.setOnClickListener {
            setDrawing()
        }

        tv_revocation?.setOnClickListener {
            elik?.unDo()
        }

        tv_gray_line?.setOnClickListener {
            if (!isGeometry){
                return@setOnClickListener
            }
            PopupClick(this,pops,tv_gray_line,5).builder().setOnSelectListener{
                tv_gray_line?.text=it.name
                setLine(it.id)
            }
        }

        tv_scale?.setOnClickListener {
            if (isErasure){
                stopErasure()
            }
            tv_scale.isSelected=!tv_scale.isSelected
            isScale=tv_scale.isSelected
            tv_scale.setTextColor(getColor(if (isScale) R.color.white else R.color.black))
        }

        tv_parallel?.setOnClickListener {
            if (isErasure){
                stopErasure()
            }
            tv_parallel.isSelected=!isParallel
            isParallel=tv_parallel.isSelected
            tv_parallel.setTextColor(getColor(if (isParallel) R.color.white else R.color.black))
        }

        tv_reduce?.setOnClickListener {
            setDrawing()
            disMissView(ll_geometry)
            showView(iv_geometry)
            setViewElikUnable(iv_geometry)
            if (isParallel){
                tv_parallel?.callOnClick()
            }
            if (isScale){
                tv_scale?.callOnClick()
            }
        }

        tv_out?.setOnClickListener {
            setDrawing()
            disMissView(ll_geometry,iv_geometry)
            if (isParallel){
                tv_parallel?.callOnClick()
            }
            if (isScale){
                tv_scale?.callOnClick()
            }
        }

        elik?.setDrawEventListener(object : EinkPWInterface.PWDrawEventWithPoint {
            override fun onTouchDrawStart(p0: Bitmap?, p1: Boolean, p2: PWInputPoint?) {
                elik?.setShifted(isCurrent&&isParallel)
            }
            override fun onTouchDrawEnd(p0: Bitmap?, p1: Rect?, p2: PWInputPoint?, p3: PWInputPoint?) {
                if (elik?.curDrawObjStatus == true){
                    reDrawGeometry(elik!!)
                }
            }
            override fun onOneWordDone(p0: Bitmap?, p1: Rect?) {
                elik?.saveBitmap(true) {}
            }
        })
    }

    /**
     * 设置刻度重绘
     */
    private fun reDrawGeometry(elik:EinkPWInterface){
        if (isErasure)
            return
        if (isScale){
            if (currentGeometry==1||currentGeometry==2||currentGeometry==3||currentGeometry==5||currentGeometry==7||currentGeometry==8||currentGeometry==9){
                Handler().postDelayed({
                    v_content.invalidate()
                    GeometryScaleDialog(this,currentGeometry,circlePos).builder()
                        ?.setOnDialogClickListener{
                                width, height ->
                            when (currentGeometry) {
                                2, 5 -> {
                                    elik.reDrawShape(width,height)
                                }
                                7->{
                                    val info=elik.curHandlerInfo
                                    elik.reDrawShape(if (setA(info)>0) width else -width ,info.split("&")[1].toFloat())
                                }
                                9 -> {
                                    elik.reDrawShape(width,5f)
                                }
                                else -> {
                                    elik.reDrawShape(width,-1f)
                                }
                            }
                        }
                },300)
            }
        }
    }

    /**
     * 画圆
     */
    private fun setEilkCircle(){
        setCheckView(ll_circle)
        when(circlePos){
            0->setDrawOjectType(PWDrawObjectHandler.DRAW_OBJ_CIRCLE)
            1->setDrawOjectType(PWDrawObjectHandler.DRAW_OBJ_CIRCLE2)
            else->setDrawOjectType(PWDrawObjectHandler.DRAW_OBJ_CIRCLE3)
        }
        currentGeometry=3
    }

    /**
     * 画坐标
     */
    private fun setEilkAxis(){
        setCheckView(ll_axis)
        setDrawOjectType(PWDrawObjectHandler.DRAW_OBJ_AXIS)
        when(axisPos){
            0->{
                setDrawOjectType(PWDrawObjectHandler.DRAW_OBJ_AXIS)
                elik?.setDrawAxisProperty(1, 10, 5,isScale)
            }
            1->{
                setDrawOjectType(PWDrawObjectHandler.DRAW_OBJ_AXIS2)
                elik?.setDrawAxisProperty(2, 10, 5,isScale)
            }
            2->{
                setDrawOjectType(PWDrawObjectHandler.DRAW_OBJ_AXIS3)
                elik?.setDrawAxisProperty(3, 10, 5,isScale)
            }
        }
        currentGeometry=9
    }

    /**
     * 获取a值
     */
    private fun setA(info:String):Float{
        val list= mutableListOf<String>()
        val pattern= Pattern.compile("-?\\d+(\\.\\d+)") // 编译正则表达式，匹配连续的数字
        val matcher= pattern.matcher(info) // 创建匹配器对象
        while (matcher.find()){
            list.add(matcher.group())
        }
        return list[0].toFloat()
    }

    /**
     * 设置线
     */
    private fun setLine(type: Int){
        when(type){
            0->{
                elik?.penColor = Color.BLACK
                elik?.setPaintEffect(0)
            }
            1->{
                elik?.penColor = Color.parseColor("#999999")
                elik?.setPaintEffect(0)
            }
            else->{
                elik?.penColor = Color.BLACK
                elik?.setPaintEffect(1)
            }
        }
    }

    /**
     * 设置选中笔形
     */
    private fun setCheckView(view:View){
        if (isErasure){
            stopErasure()
        }
        if (view!=ll_pen){
            isGeometry=true
        }
        //当前支持平行的view
        isCurrent = view==ll_line||view==ll_angle||view==ll_axis
        ll_line?.setBackgroundResource(R.color.color_transparent)
        ll_rectangle?.setBackgroundResource(R.color.color_transparent)
        ll_circle?.setBackgroundResource(R.color.color_transparent)
        ll_arc?.setBackgroundResource(R.color.color_transparent)
        ll_oval?.setBackgroundResource(R.color.color_transparent)
        ll_vertical?.setBackgroundResource(R.color.color_transparent)
        ll_parabola?.setBackgroundResource(R.color.color_transparent)
        ll_angle?.setBackgroundResource(R.color.color_transparent)
        ll_axis?.setBackgroundResource(R.color.color_transparent)
        ll_pen?.setBackgroundResource(R.color.color_transparent)
        view.setBackgroundResource(R.drawable.bg_geometry_select)
    }

    /**
     * 设置笔类型
     */
    private fun setDrawOjectType(type:Int){
        elik?.drawObjectType = type
        if (type!=PWDrawObjectHandler.DRAW_OBJ_CHOICERASE)
            currentDrawObj=type
    }

    /**
     * 设置标题是否可以编辑
     */
    fun setDrawingTitleClick(boolean: Boolean){
        isTitleClick=boolean
    }


    /**
     * 工具栏弹窗
     */
    private fun showDialogAppTool(){
        AppToolDialog(this).builder().setDialogClickListener{
            setViewElikUnable(ll_geometry)
            showView(ll_geometry)
            if (isErasure)
                stopErasure()
        }
    }

    /**
     * 设置不能手写
     */
    fun setViewElikUnable(vararg views: View?){
        for (view in views)
            elik?.addOnTopView(view)
    }

    fun setPWEnabled(boolean: Boolean){
        elik?.setPWEnabled(boolean)
    }

    /**
     * 格式序列化  题目分数转行list集合
     */
    fun scoreJsonToList(json:String):List<ExamScoreItem>{
        var items= mutableListOf<ExamScoreItem>()
        if (correctMode<3){
            items= Gson().fromJson(json, object : TypeToken<List<ExamScoreItem>>() {}.type) as MutableList<ExamScoreItem>
            for (item in items){
                item.sort=items.indexOf(item)
            }
        }
        else{
            var totalChildSort=0
            val scores= Gson().fromJson(json, object : TypeToken<List<List<ExamScoreItem>>>() {}.type) as MutableList<List<ExamScoreItem>>
            for (i in scores.indices){
                items.add(ExamScoreItem().apply {
                    sort=i
                    if (scoreMode==1){
                        var totalLabel=0.0
                        for (item in scores[i]){
                            totalLabel+=item.label
                        }
                        label=totalLabel
                        var totalItem=0.0
                        for (item in scores[i]){
                            totalItem+= getScore(item.score)
                        }
                        score=ToolUtils.getFormatNum(totalItem,"0.0")
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
                        if (correctMode==3){
                            item.sort=scores[i].indexOf(item)
                        }
                        else{
                            item.sort=totalChildSort+scores[i].indexOf(item)
                        }
                    }
                    childScores=scores[i]
                    totalChildSort+=scores[i].size
                })
            }
        }
        return items
    }

    /**
     * 获取分数
     * @param scoreStr
     * @return
     */
    private fun getScore(scoreStr: String?): Double {
        return if (scoreStr.isNullOrEmpty()) {
            0.0
        } else {
            scoreStr.toDouble()
        }
    }

    /**
     * 设置成绩分数
     */
    fun initScoreView(){
        iv_correct_close?.setOnClickListener {
            disMissView(ll_score)
            showView(iv_score)
        }

        iv_score?.setOnClickListener {
            disMissView(iv_score)
            showView(ll_score)
        }

        tv_answer?.setOnClickListener {
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

    /**
     * 下一页
     */
    open fun onPageDown(){
    }

    /**
     * 上一页
     */
    open fun onPageUp(){
    }

    /**
     * 打开目录
     */
    open fun onCatalog(){

    }


    /**
     * 设置擦除
     */
    private fun onErasure(){
        setDrawOjectType(PWDrawObjectHandler.DRAW_OBJ_CHOICERASE)
    }

    /**
     * 结束擦除
     * （在展平、收屏时候都结束擦除）
     */
    fun stopErasure(){
        isErasure=false
        //关闭橡皮擦
        iv_erasure?.setImageResource(R.mipmap.icon_draw_erasure)
        setDrawOjectType(currentDrawObj)
    }

    /**
     * 恢复手写
      */
    private fun setDrawing(){
        setCheckView(ll_pen)
        setDrawOjectType(PWDrawObjectHandler.DRAW_OBJ_RANDOM_PEN)
        currentGeometry=0
        //设置黑线
        setLine(0)
        tv_gray_line?.text=getString(R.string.line_black)

        isGeometry=false
    }

    /**
     * 标题a操作
     */
    open fun setDrawingTitle(title:String){
    }


}


