package com.bll.lnkcommon.ui.activity

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.dialog.PermissionTimeSelectorDialog
import com.bll.lnkcommon.mvp.model.PermissionTimeBean
import com.bll.lnkcommon.mvp.model.StudentBean
import com.bll.lnkcommon.ui.adapter.PermissionTimeAdapter
import kotlinx.android.synthetic.main.ac_student_permission_set.*

class PermissionSettingActivity:BaseActivity() {

    private var mStudentBean:StudentBean?=null
    private var mBookAdapter:PermissionTimeAdapter?=null
    private var mVideoAdapter:PermissionTimeAdapter?=null
    private var bookTimes= mutableListOf<PermissionTimeBean>()
    private var videoTimes= mutableListOf<PermissionTimeBean>()

    override fun layoutId(): Int {
        return R.layout.ac_student_permission_set
    }

    override fun initData() {
        mStudentBean = intent.getBundleExtra("bundle")?.getSerializable("studentInfo") as StudentBean
    }

    override fun initView() {
        setPageTitle(mStudentBean?.nickname+"    权限设置")

        st_money.isChecked=mStudentBean?.isAllowMoney!!
        st_money.setOnClickListener {
            val titleStr=if (mStudentBean?.isAllowMoney!!) "确定允许学生使用青豆？" else "确定不允许学生使用青豆？"
            CommonDialog(this).setContent(titleStr).builder().onDialogClickListener= object : CommonDialog.OnDialogClickListener {
                override fun cancel() {
                }
                override fun ok() {
                    mStudentBean?.isAllowMoney=!mStudentBean?.isAllowMoney!!
                    st_money.isChecked=mStudentBean?.isAllowMoney!!
                }
            }
        }


        iv_book_add.visibility=if (mStudentBean?.isAllowBook!!) View.GONE else View.VISIBLE
        st_book.isChecked=mStudentBean?.isAllowBook!!
        st_book.setOnCheckedChangeListener { compoundButton, b ->
            mStudentBean?.isAllowBook=b
            iv_book_add.visibility=if (mStudentBean?.isAllowBook!!) View.GONE else View.VISIBLE
            bookTimes.clear()
            mBookAdapter?.setNewData(bookTimes)
        }

        iv_video_add.visibility=if (mStudentBean?.isAllowVideo!!) View.GONE else View.VISIBLE
        st_video.isChecked=mStudentBean?.isAllowVideo!!
        st_video.setOnCheckedChangeListener { compoundButton, b ->
            mStudentBean?.isAllowVideo=b
            iv_video_add.visibility=if (mStudentBean?.isAllowVideo!!) View.GONE else View.VISIBLE
            videoTimes.clear()
            mVideoAdapter?.setNewData(videoTimes)
        }

        iv_book_add.setOnClickListener {
            val weeks= mutableListOf<Int>()
            for (item in bookTimes){
                for (i in item.weeks){
                    weeks.add(i)
                }
            }
            PermissionTimeSelectorDialog(this, weeks).builder().setOnDateListener{
                startStr,startLon,endStr,endLon,weeks->
                val ids= mutableListOf<Int>()
                for (week in weeks){
                    ids.add(week.week)
                }
                val item=PermissionTimeBean()
                item.timeStr= "$startStr ~ $endStr"
                item.startTime=startLon
                item.endTime=endLon
                item.weeks=ids
                bookTimes.add(item)
                mBookAdapter?.setNewData(bookTimes)
            }
        }

        iv_video_add.setOnClickListener {
            val weeks= mutableListOf<Int>()
            for (item in videoTimes){
                for (i in item.weeks){
                    weeks.add(i)
                }
            }
            PermissionTimeSelectorDialog(this, weeks).builder().setOnDateListener{
                    startStr,startLon,endStr,endLon,weeks->
                val ids= mutableListOf<Int>()
                for (week in weeks){
                    ids.add(week.week)
                }
                val item=PermissionTimeBean()
                item.timeStr= "$startStr ~ $endStr"
                item.startTime=startLon
                item.endTime=endLon
                item.weeks=ids
                videoTimes.add(item)
                mVideoAdapter?.setNewData(videoTimes)
            }
        }

        rv_book_list.layoutManager = LinearLayoutManager(this)//创建布局管理
        mBookAdapter = PermissionTimeAdapter(R.layout.item_permission_time, null).apply {
            rv_book_list.adapter = this
            bindToRecyclerView(rv_book_list)
        }


        rv_video_list.layoutManager = LinearLayoutManager(this)//创建布局管理
        mVideoAdapter = PermissionTimeAdapter(R.layout.item_permission_time, null).apply {
            rv_video_list.adapter = this
            bindToRecyclerView(rv_video_list)
        }

    }

}