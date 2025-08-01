package com.bll.lnkcommon.ui.activity.account

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.dialog.PermissionTimeSelectorDialog
import com.bll.lnkcommon.mvp.model.DateWeek
import com.bll.lnkcommon.mvp.model.PermissionTimeBean
import com.bll.lnkcommon.mvp.model.StudentBean
import com.bll.lnkcommon.mvp.presenter.PermissionSettingPresenter
import com.bll.lnkcommon.mvp.view.IContractView.IPermissionSettingView
import com.bll.lnkcommon.ui.adapter.PermissionTimeAdapter
import kotlinx.android.synthetic.main.ac_student_permission_set.*

class PermissionSettingActivity:BaseActivity(),IPermissionSettingView {

    private var mPresenter=PermissionSettingPresenter(this)
    private var mStudentBean:StudentBean?=null
    private var mBookAdapter:PermissionTimeAdapter?=null
    private var mVideoAdapter:PermissionTimeAdapter?=null
    private var bookTimes= mutableListOf<PermissionTimeBean>()
    private var videoTimes= mutableListOf<PermissionTimeBean>()
    private var type=0
    private var position=0
    private var weekStr=""
    private var startLong=0L
    private var endLong=0L

    override fun onStudent(studentBean: StudentBean) {
        mStudentBean=studentBean

        st_money.setImageResource(if (mStudentBean?.isAllowMoney!!) R.mipmap.icon_switch_true else R.mipmap.icon_switch_false)
        st_book.setImageResource(if (mStudentBean?.isAllowBook!!) R.mipmap.icon_switch_true else R.mipmap.icon_switch_false)
        st_video.setImageResource(if (mStudentBean?.isAllowVideo!!) R.mipmap.icon_switch_true else R.mipmap.icon_switch_false)
        setBookStateView()
        setVideoStateView()

        bookTimes= mStudentBean?.bookList as MutableList<PermissionTimeBean>
        videoTimes= mStudentBean?.videoList as MutableList<PermissionTimeBean>
        mBookAdapter?.setNewData(bookTimes)
        mVideoAdapter?.setNewData(videoTimes)
    }

    override fun onSuccess() {
        mPresenter.onStudent(mStudentBean?.accountId!!)
    }


    override fun onChangeSuccess() {
        when (type) {
            1 -> {
                mStudentBean?.isAllowMoney=!mStudentBean?.isAllowMoney!!
                st_money.setImageResource(if (mStudentBean?.isAllowMoney!!) R.mipmap.icon_switch_true else R.mipmap.icon_switch_false)
            }
            2 -> {
                mStudentBean?.isAllowBook=!mStudentBean?.isAllowBook!!
                st_book.setImageResource(if (mStudentBean?.isAllowBook!!) R.mipmap.icon_switch_true else R.mipmap.icon_switch_false)
                setBookStateView()
            }
            3 -> {
                mStudentBean?.isAllowVideo=!mStudentBean?.isAllowVideo!!
                st_video.setImageResource(if (mStudentBean?.isAllowVideo!!) R.mipmap.icon_switch_true else R.mipmap.icon_switch_false)
                setVideoStateView()
            }
        }
    }

    override fun onEditSuccess() {
        if (type==2){
            val item=bookTimes[position]
            item.startTime=startLong
            item.endTime=endLong
            item.weeks=weekStr
            mBookAdapter?.notifyItemChanged(position)
        }
        else{
            val item=videoTimes[position]
            item.startTime=startLong
            item.endTime=endLong
            item.weeks=weekStr
            mVideoAdapter?.notifyItemChanged(position)
        }
    }


    override fun layoutId(): Int {
        return R.layout.ac_student_permission_set
    }

    override fun initData() {
        mStudentBean = intent.getBundleExtra("bundle")?.getSerializable("studentInfo") as StudentBean
        bookTimes= mStudentBean?.bookList as MutableList<PermissionTimeBean>
        videoTimes= mStudentBean?.videoList as MutableList<PermissionTimeBean>
        mPresenter.onStudent(mStudentBean?.accountId!!)
    }

    override fun initView() {
        setPageTitle(mStudentBean?.nickname+"    权限设置")

        st_money.setOnClickListener {
            val titleStr=if (mStudentBean?.isAllowMoney!!) "确定不允许该学生使用青豆？" else "确定允许该学生使用青豆？"
            CommonDialog(this).setContent(titleStr).builder().onDialogClickListener= object : CommonDialog.OnDialogClickListener {
                override fun cancel() {
                }
                override fun ok() {
                    type=1
                    val map=HashMap<String,Any>()
                    map["accountId"]=mStudentBean?.accountId!!
                    map["buyState"]=if (mStudentBean?.isAllowMoney!!) 2 else 1
                    mPresenter.onChangeAllow(map)
                }
            }
        }

        st_book.setOnClickListener {
            type=2
            val map=HashMap<String,Any>()
            map["accountId"]=mStudentBean?.accountId!!
            map["bookState"]=if (mStudentBean?.isAllowBook!!) 2 else 1
            mPresenter.onChangeAllow(map)
        }

        st_video.setOnClickListener {
            type=3
            val map=HashMap<String,Any>()
            map["accountId"]=mStudentBean?.accountId!!
            map["videoState"]=if (mStudentBean?.isAllowVideo!!) 2 else 1
            mPresenter.onChangeAllow(map)
        }

        iv_book_add.setOnClickListener {
            PermissionTimeSelectorDialog(this, getWeeks(1)).builder().setOnDateListener{
                startLon,endLon,weeks->
                val map=HashMap<String,Any>()
                map["type"]=1
                map["startTime"]=startLon
                map["endTime"]=endLon
                map["userId"]=mStudentBean?.accountId!!
                map["weeks"]=getWeekStr(weeks)
                mPresenter.onInsertTime(map)
            }
        }

        iv_video_add.setOnClickListener {
            PermissionTimeSelectorDialog(this, getWeeks(2)).builder().setOnDateListener{
                    startLon,endLon,weeks->
                val map=HashMap<String,Any>()
                map["type"]=2
                map["startTime"]=startLon
                map["endTime"]=endLon
                map["userId"]=mStudentBean?.accountId!!
                map["weeks"]=getWeekStr(weeks)
                mPresenter.onInsertTime(map)
            }
        }

        rv_book_list.layoutManager = LinearLayoutManager(this)//创建布局管理
        mBookAdapter = PermissionTimeAdapter(R.layout.item_permission_time, bookTimes).apply {
            rv_book_list.adapter = this
            bindToRecyclerView(rv_book_list)
            setOnItemChildClickListener { adapter, view, position ->
                if (view.id==R.id.iv_delete){
                    val map=HashMap<String,Any>()
                    map["id"]=bookTimes[position].id
                    map["userId"]=mStudentBean?.accountId!!
                    mPresenter.onDeleteTime(map)
                }
            }
            setOnItemClickListener { adapter, view, position ->
                this@PermissionSettingActivity.position=position
                type=2
                val item=bookTimes[position]
                PermissionTimeSelectorDialog(this@PermissionSettingActivity, getWeeks(1),item).builder().setOnDateListener{
                        startLon,endLon,weeks->
                    startLong=startLon
                    endLong=endLon
                    weekStr=getWeekStr(weeks)
                    val map=HashMap<String,Any>()
                    map["type"]=1
                    map["startTime"]=startLon
                    map["endTime"]=endLon
                    map["userId"]=mStudentBean?.accountId!!
                    map["weeks"]=weekStr
                    map["id"]=item.id
                    mPresenter.onEditTime(map)
                }
            }
        }


        rv_video_list.layoutManager = LinearLayoutManager(this)//创建布局管理
        mVideoAdapter = PermissionTimeAdapter(R.layout.item_permission_time, videoTimes).apply {
            rv_video_list.adapter = this
            bindToRecyclerView(rv_video_list)
            setOnItemChildClickListener { adapter, view, position ->
                if (view.id==R.id.iv_delete){
                    val map=HashMap<String,Any>()
                    map["id"]=videoTimes[position].id
                    map["userId"]=mStudentBean?.accountId!!
                    mPresenter.onDeleteTime(map)
                }
            }
            setOnItemClickListener { adapter, view, position ->
                this@PermissionSettingActivity.position=position
                type=3
                val item=videoTimes[position]
                PermissionTimeSelectorDialog(this@PermissionSettingActivity, getWeeks(1),item).builder().setOnDateListener{
                        startLon,endLon,weeks->
                    startLong=startLon
                    endLong=endLon
                    weekStr=getWeekStr(weeks)
                    val map=HashMap<String,Any>()
                    map["type"]=2
                    map["startTime"]=startLon
                    map["endTime"]=endLon
                    map["userId"]=mStudentBean?.accountId!!
                    map["weeks"]=weekStr
                    map["id"]=item.id
                    mPresenter.onEditTime(map)
                }
            }
        }

    }

    private fun getWeeks(type:Int):List<Int>{
        val weeks= mutableListOf<Int>()
        val list=if (type==1) bookTimes else videoTimes
        for (item in list){
            val week=item.weeks.split(",")
            for (i in week){
                weeks.add(i.toInt())
            }
        }
        return weeks
    }

    private fun getWeekStr(weeks:List<DateWeek>):String{
        var week=""
        for (i in weeks.indices){
            week += if (i == weeks.size - 1) {
                "${weeks[i].week}"
            } else {
                "${weeks[i].week},"
            }
        }
        return week
    }

    private fun setBookStateView(){
        iv_book_add.visibility=if (mStudentBean?.isAllowBook!!) View.VISIBLE else View.GONE
        rv_book_list.visibility=if (mStudentBean?.isAllowBook!!) View.VISIBLE else View.GONE
    }

    private fun setVideoStateView(){
        iv_video_add.visibility=if (mStudentBean?.isAllowVideo!!) View.VISIBLE else View.GONE
        rv_video_list.visibility=if (mStudentBean?.isAllowVideo!!) View.VISIBLE else View.GONE
    }

}