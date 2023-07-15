package com.bll.lnkcommon.ui.fragment

import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.dialog.PopupRadioList
import com.bll.lnkcommon.mvp.model.PopupBean
import com.bll.lnkcommon.mvp.model.TeacherHomeworkList.TeacherHomeworkBean
import com.bll.lnkcommon.ui.adapter.TeacherHomeworkAdapter
import com.bll.lnkcommon.widget.SpaceItemDeco
import kotlinx.android.synthetic.main.common_fragment_title.*
import kotlinx.android.synthetic.main.common_radiogroup.*
import kotlinx.android.synthetic.main.fragment_app.*

class HomeworkFragment : BaseFragment() {

    private var tabId = 0
    private var homeworks = mutableListOf<TeacherHomeworkBean>()
    private var mAdapter: TeacherHomeworkAdapter? = null
    private var studentId = 0
    private var popupStudents = mutableListOf<PopupBean>()
    private var courseStr=""

    override fun getLayoutId(): Int {
        return R.layout.fragment_homework
    }

    override fun initView() {
        setTitle(DataBeanManager.mainListTitle[5])
        showView(tv_course)

        initStudent()
        initTab()
        initRecyclerView()

        tv_course.setOnClickListener {
            PopupRadioList(requireActivity(), DataBeanManager.popupCourses, tv_course, tv_course.width, 10).builder()
                .setOnSelectListener {
                    courseStr = it.name
                    tv_course.text = it.name
                    fetchData()
                }
        }

        tv_student.setOnClickListener {
            PopupRadioList(requireActivity(), popupStudents, tv_student, tv_student.width, 10).builder()
                .setOnSelectListener {
                    studentId = it.id
                    tv_student.text = it.name
                    fetchData()
                }
        }
    }

    override fun lazyLoad() {
        fetchData()
    }

    private fun initStudent(){
        popupStudents.clear()
        if (DataBeanManager.students.size > 0) {
            showView(tv_student)
            for (item in DataBeanManager.students) {
                popupStudents.add(PopupBean(item.accountId, item.nickname, DataBeanManager.students.indexOf(item) == 0))
            }
            studentId = popupStudents[0].id
            tv_student.text = popupStudents[0].name
        } else {
            disMissView(tv_student)
        }
    }

    private fun initTab() {
        val tabStrs = DataBeanManager.homeworkType
        for (i in tabStrs.indices) {
            rg_group.addView(getRadioButton(i, tabStrs[i], tabStrs.size - 1))
        }
        rg_group.setOnCheckedChangeListener { radioGroup, id ->
            tabId = id
            pageIndex = 1
            fetchData()
        }
    }

    private fun initRecyclerView() {
        rv_list.layoutManager = LinearLayoutManager(activity)//创建布局管理
        mAdapter = TeacherHomeworkAdapter(R.layout.item_homework_teacher, null)
        rv_list.adapter = mAdapter
        mAdapter?.bindToRecyclerView(rv_list)
        mAdapter?.setEmptyView(R.layout.common_empty)
        rv_list?.addItemDecoration(SpaceItemDeco(0, 0, 0, 40))
    }

    override fun onEventBusMessage(msgFlag: String) {
        if (msgFlag == Constants.STUDENT_EVENT) {
            initStudent()
            fetchData()
        }
    }

    override fun onRefreshData() {
        courseStr=""
        fetchData()
    }

    override fun fetchData() {
        homeworks.clear()
        if (tabId!=0){
            mAdapter?.setNewData(homeworks)
        }
        else{
            homeworks.add(TeacherHomeworkBean().apply {
                status=1
                course="语文"
                type=2
                typeStr="课堂作业本"
                content="语文作业第30、31页第五题"
                date=0
                commitDate=0
            })
            homeworks.add(TeacherHomeworkBean().apply {
                status=1
                course="语文"
                type=2
                typeStr="课堂作业本"
                content="语文作业第28、29页第五题"
                date=System.currentTimeMillis()
                commitDate=System.currentTimeMillis()
            })
            homeworks.add(TeacherHomeworkBean().apply {
                status=2
                course="数学"
                type=2
                typeStr="课堂作业本"
                content="数学作业第28、29页第五题"
                date=System.currentTimeMillis()
                commitDate=System.currentTimeMillis()
            })
            homeworks.add(TeacherHomeworkBean().apply {
                status=1
                course="语文"
                type=1
                typeStr="课堂题卷本"
                content=""
                date=System.currentTimeMillis()
                commitDate=System.currentTimeMillis()
            })
            homeworks.add(TeacherHomeworkBean().apply {
                status=3
                course="语文"
                type=2
                typeStr="课堂作业本"
                content="语文作业第2、3页第五题"
                date=System.currentTimeMillis()
                commitDate=System.currentTimeMillis()
            })
            homeworks.add(TeacherHomeworkBean().apply {
                status=3
                course="语文"
                type=1
                typeStr="课堂题卷本"
                content=""
                date=System.currentTimeMillis()
                commitDate=System.currentTimeMillis()
            })
            mAdapter?.setNewData(homeworks)
        }
    }

}