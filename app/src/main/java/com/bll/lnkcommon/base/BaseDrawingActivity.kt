package com.bll.lnkcommon.base

import PopupClick
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.dialog.*
import com.bll.lnkcommon.mvp.model.PopupBean
import com.bll.lnkcommon.mvp.model.User
import com.bll.lnkcommon.net.ExceptionHandle
import com.bll.lnkcommon.net.IBaseView
import com.bll.lnkcommon.ui.activity.AccountLoginActivity
import com.bll.lnkcommon.ui.activity.MainActivity
import com.bll.lnkcommon.utils.*
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.ac_drawing.*
import kotlinx.android.synthetic.main.ac_drawing.iv_geometry
import kotlinx.android.synthetic.main.ac_drawing.ll_geometry
import kotlinx.android.synthetic.main.ac_drawing.v_content
import kotlinx.android.synthetic.main.common_drawing_bottom.*
import kotlinx.android.synthetic.main.common_drawing_geometry.*
import kotlinx.android.synthetic.main.common_page_number.*
import kotlinx.android.synthetic.main.common_title.*
import org.greenrobot.eventbus.EventBus


abstract class BaseDrawingActivity : AppCompatActivity(), IBaseView {

    var mDialog: ProgressDialog? = null
    var mSaveState:Bundle?=null
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mSaveState=savedInstanceState
        setContentView(layoutId())
        initCommonTitle()

        setStatusBarColor(ContextCompat.getColor(this, R.color.white))

        if (v_content!=null){
            elik = v_content?.pwInterFace
        }

        mDialog = ProgressDialog(this)
        initData()
        initView()

        initGeometryView()
    }


    /**
     *  加载布局
     */
    abstract fun layoutId(): Int

    /**
     * 初始化数据
     */
    abstract fun initData()

    /**
     * 初始化 View
     */
    abstract fun initView()

    @SuppressLint("WrongViewCast")
    fun initCommonTitle() {

        iv_back?.setOnClickListener { finish() }

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

        tv_page_title?.setOnClickListener {
            if (isTitleClick){
                val title=tv_page_title.text.toString()
                InputContentDialog(this,title).builder().setOnDialogClickListener { string ->
                    tv_page_title.text = string
                    setDrawingTitle(string)
                }
            }
        }

        iv_page_up?.setOnClickListener {
            onPageUp()
        }

        iv_page_down?.setOnClickListener {
            onPageDown()
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

//        elik?.setDrawEventListener(object : EinkPWInterface.PWDrawEventWithPoint {
//            override fun onTouchDrawStart(p0: Bitmap?, p1: Boolean, p2: PWInputPoint?) {
//                elik?.setShifted(isCurrent&&isParallel)
//            }
//            override fun onTouchDrawEnd(p0: Bitmap?, p1: Rect?, p2: PWInputPoint?, p3: PWInputPoint?, ) {
//                reDrawGeometry(elik!!)
//            }
//            override fun onOneWordDone(p0: Bitmap?, p1: Rect?) {
//            }
//        })
    }

    /**
     * 设置刻度重绘
     */
    private fun reDrawGeometry(elik:EinkPWInterface){
        if (isErasure)
            return
        if (isScale){
            if (currentGeometry==1||currentGeometry==2||currentGeometry==3||currentGeometry==5||currentGeometry==8||currentGeometry==9){
                GeometryScaleDialog(this,currentGeometry,circlePos).builder()
                    ?.setOnDialogClickListener{
                            width, height ->
                        when (currentGeometry) {
                            2, 5 -> {
                                elik.reDrawShape(width,height)
                            }
                            9 -> {
                                elik.reDrawShape(height,width)
                            }
                            else -> {
                                elik.reDrawShape(width,-1f)
                            }
                        }
                    }
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
        elik?.setDrawAxisProperty(axisPos+1, 10,5, isScale)
        currentGeometry=9
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
        view.setBackgroundResource(R.drawable.bg_black_stroke_0dp_corner)
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
        AppToolDialog(this).builder()?.setDialogClickListener{
            setViewElikUnable(ll_geometry)
            showView(ll_geometry)
            if (isErasure)
                stopErasure()
        }
    }

    /**
     * 设置不能手写
     */
    fun setViewElikUnable(view:View){
        elik?.addOnTopView(view)
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
     * 设置擦除
     */
    private fun onErasure(){
        setDrawOjectType(PWDrawObjectHandler.DRAW_OBJ_CHOICERASE)
    }

    /**
     * 结束擦除
     * （在展平、收屏时候都结束擦除）
     */
    private fun stopErasure(){
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

    fun setPageTitle(title: String){
        tv_title?.text=title
    }

    /**
     * 标题a操作
     */
    open fun setDrawingTitle(title:String){
    }
    /**
     * 是否登录
     */
    fun isLoginState():Boolean{
        val mUser= SPUtil.getObj("user", User::class.java)
        val token=SPUtil.getString("token")
        return token.isNotEmpty() && mUser!=null
    }

    fun getUser(): User?{
        return SPUtil.getObj("user", User::class.java)
    }
    /**
     * 显示view
     */
    protected fun showView(view: View?) {
        if (view != null && view.visibility != View.VISIBLE) {
            view.visibility = View.VISIBLE
        }
    }

    /**
     * 显示view
     */
    protected fun showView(vararg views: View?) {
        for (view in views) {
            if (view != null && view.visibility != View.VISIBLE) {
                view.visibility = View.VISIBLE
            }
        }
    }


    /**
     * 消失view
     */
    protected fun disMissView(view: View?) {
        if (view != null && view.visibility != View.GONE) {
            view.visibility = View.GONE
        }
    }

    /**
     * 消失view
     */
    protected fun disMissView(vararg views: View?) {
        for (view in views) {
            if (view != null && view.visibility != View.GONE) {
                view.visibility = View.GONE
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected fun setStatusBarColor(statusColor: Int) {
        val window = window
        //取消状态栏透明
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        //添加Flag把状态栏设为可绘制模式
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        //设置状态栏颜色
        window.statusBarColor = statusColor
        //设置系统状态栏处于可见状态
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        //让view不根据系统窗口来调整自己的布局
        val mContentView = window.findViewById<View>(Window.ID_ANDROID_CONTENT) as ViewGroup
        val mChildView = mContentView.getChildAt(0)
        if (mChildView != null) {
            ViewCompat.setFitsSystemWindows(mChildView, false)
            ViewCompat.requestApplyInsets(mChildView)
        }
    }

    /**
     * 关闭软键盘
     */
    fun hideKeyboard(){
        KeyboardUtils.hideSoftKeyboard(this)
    }

    fun showToast(s:String){
        SToast.showText(s)
    }

    fun showToast(sId:Int){
        SToast.showText(sId)
    }

    fun showLog(s:String){
        Log.d("debug",s)
    }
    fun showLog(sId:Int){
        Log.d("debug",getString(sId))
    }

    override fun addSubscription(d: Disposable) {
    }
    override fun login() {
        SToast.showText(R.string.login_timeout)
        SPUtil.putString("token", "")
        SPUtil.removeObj("user")
        EventBus.getDefault().post(Constants.USER_EVENT)
        ActivityManager.getInstance().finishOthers(MainActivity::class.java)
        startActivity(Intent(this, AccountLoginActivity::class.java))
        DataBeanManager.students.clear()
        DataBeanManager.friends.clear()
        EventBus.getDefault().post(Constants.STUDENT_EVENT)
    }

    override fun hideLoading() {
        mDialog?.dismiss()
    }

    override fun showLoading() {
        mDialog!!.show()
    }

    override fun fail(msg: String) {
        showToast(msg)
    }

    override fun onFailer(responeThrowable: ExceptionHandle.ResponeThrowable?) {
        showLog(R.string.connect_server_timeout)
    }
    override fun onComplete() {
        showLog(R.string.request_success)
    }

    override fun onPause() {
        super.onPause()
        mDialog!!.dismiss()
        hideKeyboard()
    }

}


