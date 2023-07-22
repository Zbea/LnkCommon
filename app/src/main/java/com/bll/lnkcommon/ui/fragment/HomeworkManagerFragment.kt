package com.bll.lnkcommon.ui.fragment

import androidx.fragment.app.Fragment
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.dialog.PopupRadioList
import com.bll.lnkcommon.mvp.model.PopupBean
import com.bll.lnkcommon.mvp.model.StudentBean
import com.bll.lnkcommon.mvp.presenter.StudentPresenter
import com.bll.lnkcommon.mvp.view.IContractView.IStudentView
import com.bll.lnkcommon.ui.fragment.homework.HomeworkFragment
import com.bll.lnkcommon.ui.fragment.homework.MyHomeworkFragment
import kotlinx.android.synthetic.main.common_fragment_title.*
import kotlinx.android.synthetic.main.common_radiogroup.*

class HomeworkManagerFragment:BaseFragment(),IStudentView {

    private val presenter=StudentPresenter(this)
    private var popupStudents = mutableListOf<PopupBean>()

    private var homeworkFragment: HomeworkFragment? = null
    private var examFragment: HomeworkFragment? = null
    private var myHomeworkFragment:MyHomeworkFragment?=null

    private var lastPosition = 0
    private var lastFragment: Fragment? = null

    override fun onListStudents(list: MutableList<StudentBean>) {
        DataBeanManager.students=list
        initStudent()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_homework_manager
    }
    override fun initView() {
        setTitle(DataBeanManager.mainListTitle[5])
        showView(tv_course)
        iv_manager.setImageResource(R.mipmap.icon_add)

        homeworkFragment = HomeworkFragment().newInstance(1)
        examFragment = HomeworkFragment().newInstance(2)
        myHomeworkFragment= MyHomeworkFragment()

        switchFragment(lastFragment, homeworkFragment)

        initStudent()
        initTab()

        tv_course.setOnClickListener {
            PopupRadioList(requireActivity(), DataBeanManager.popupCourses, tv_course,tv_course.width, 10).builder()
                .setOnSelectListener {
                    tv_course.text = it.name
                    when(lastPosition){
                        0->{
                            homeworkFragment?.onChangeCourse(it.name)
                        }
                        1->{
                            examFragment?.onChangeCourse(it.name)
                        }
                    }
                }
        }

        tv_student.setOnClickListener {
            PopupRadioList(requireActivity(), popupStudents, tv_student, tv_student.width, 10).builder()
                .setOnSelectListener {
                    tv_student.text = it.name
                    changeFragmentStudent(it.id)
                }
        }
    }
    override fun lazyLoad() {
        if (DataBeanManager.students.size==0)
            presenter.getStudents()
    }

    private fun initStudent(){
        popupStudents.clear()
        if (DataBeanManager.students.size > 0) {
            showView(tv_student)
            for (item in DataBeanManager.students) {
                popupStudents.add(PopupBean(item.accountId, item.nickname, DataBeanManager.students.indexOf(item) == 0))
            }
            tv_student.text = popupStudents[0].name
            changeFragmentStudent(popupStudents[0].id)
        }
    }

    private fun changeFragmentStudent(id:Int){
        homeworkFragment?.onChangeStudent(id)
        examFragment?.onChangeStudent(id)
        myHomeworkFragment?.onChangeStudent(id)
    }

    private fun initTab() {
        val tabStrs = DataBeanManager.homeworkType
        for (i in tabStrs.indices) {
            rg_group.addView(getRadioButton(i, tabStrs[i], tabStrs.size - 1))
        }
        rg_group.setOnCheckedChangeListener { radioGroup, id ->
            tv_course.text="选择科目"
            when(id){
                0->{
                    showView(tv_course)
                    disMissView(iv_manager)
                    switchFragment(lastFragment, homeworkFragment)
                }
                1->{
                    showView(tv_course)
                    disMissView(iv_manager)
                    switchFragment(lastFragment, examFragment)
                }
                2->{
                    showView(iv_manager)
                    disMissView(tv_course)
                    switchFragment(lastFragment, myHomeworkFragment)
                }
                3->{
                    disMissView(tv_course,iv_manager)
                }
            }
            lastPosition=id
        }
    }

    //页码跳转
    private fun switchFragment(from: Fragment?, to: Fragment?) {
        if (from != to) {
            lastFragment = to
            val fm = activity?.supportFragmentManager
            val ft = fm?.beginTransaction()

            if (!to?.isAdded!!) {
                if (from != null) {
                    ft?.hide(from)
                }
                ft?.add(R.id.fl_content_group, to)?.commit()
            } else {
                if (from != null) {
                    ft?.hide(from)
                }
                ft?.show(to)?.commit()
            }
        }
    }

    override fun onEventBusMessage(msgFlag: String) {
        if (msgFlag == Constants.STUDENT_EVENT) {
            initStudent()
        }
    }

}