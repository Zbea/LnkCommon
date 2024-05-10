package com.bll.lnkcommon.ui.fragment

import PopupClick
import android.view.View
import androidx.fragment.app.Fragment
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.dialog.HomeworkCreateDialog
import com.bll.lnkcommon.dialog.PopupRadioList
import com.bll.lnkcommon.mvp.model.ItemTypeBean
import com.bll.lnkcommon.ui.fragment.homework.ExamFragment
import com.bll.lnkcommon.ui.fragment.homework.HomeworkCorrectFragment
import com.bll.lnkcommon.ui.fragment.homework.HomeworkFragment
import com.bll.lnkcommon.ui.fragment.homework.MyHomeworkFragment
import kotlinx.android.synthetic.main.common_fragment_title.*
import kotlinx.android.synthetic.main.common_fragment_title.iv_manager
import kotlinx.android.synthetic.main.common_fragment_title.tv_course
import kotlinx.android.synthetic.main.common_title.*

class HomeworkManagerFragment:BaseFragment() {

    private var homeworkFragment: HomeworkFragment? = null
    private var testPaperFragment: HomeworkFragment? = null
    private var examFragment: ExamFragment? = null
    private var myHomeworkFragment:MyHomeworkFragment?=null
    private var homeworkCorrectFragment:HomeworkCorrectFragment?=null

    private var lastPosition = 0
    private var lastFragment: Fragment? = null


    override fun getLayoutId(): Int {
        return R.layout.fragment_homework_manager
    }
    override fun initView() {
        setTitle(DataBeanManager.mainListTitle[5])
        showView(tv_course)
        iv_manager.setImageResource(R.mipmap.icon_add)

        if (DataBeanManager.students.size>1){
            showView(tv_student)
            tv_student.text = DataBeanManager.students[0].nickname
        }

        val coursePops=DataBeanManager.popupCourses

        homeworkFragment = HomeworkFragment().newInstance(1)
        testPaperFragment=HomeworkFragment().newInstance(2)
        examFragment = ExamFragment()
        myHomeworkFragment= MyHomeworkFragment()
        homeworkCorrectFragment= HomeworkCorrectFragment()

        switchFragment(lastFragment, homeworkFragment)

        tv_course.setOnClickListener {
            PopupClick(requireActivity(), coursePops, tv_course, 5).builder()
                .setOnSelectListener {
                    tv_course.text = it.name
                    when(lastPosition){
                        0->{
                            homeworkFragment?.onChangeCourse(it.name)
                        }
                        1->{
                            testPaperFragment?.onChangeCourse(it.name)
                        }
                        2->{
                            examFragment?.onChangeCourse(it.name)
                        }
                    }
                }
        }

        tv_student.setOnClickListener {
            PopupRadioList(requireActivity(), DataBeanManager.popupStudents, tv_student, tv_student.width, 10).builder()
                .setOnSelectListener {
                    tv_student.text = it.name
                    changeFragmentStudent(it.id)
                }
        }

        iv_manager.setOnClickListener {
            HomeworkCreateDialog(requireActivity()).builder().setOnDialogClickListener {
                    contentStr, courseId ->
                myHomeworkFragment?.createHomeworkType(contentStr,courseId)
            }
        }

        initTab()

    }
    override fun lazyLoad() {
    }

    private fun changeFragmentStudent(id:Int){
        homeworkFragment?.onChangeStudent(id)
        testPaperFragment?.onChangeStudent(id)
        examFragment?.onChangeStudent(id)
        myHomeworkFragment?.onChangeStudent(id)
        homeworkCorrectFragment?.onChangeStudent(id)
    }

    private fun initTab(){
        val tabStrs = DataBeanManager.homeworkType
        for (i in tabStrs.indices) {
            itemTabTypes.add(ItemTypeBean().apply {
                title=tabStrs[i]
                isCheck=i==0
            })
        }
        mTabTypeAdapter?.setNewData(itemTabTypes)
    }

    override fun onTabClickListener(view: View, position: Int) {
        tv_course.text=getString(R.string.selector_subject)
        when(position){
            0->{
                showView(tv_course)
                disMissView(iv_manager)
                switchFragment(lastFragment, homeworkFragment)
            }
            1->{
                showView(tv_course)
                disMissView(iv_manager)
                switchFragment(lastFragment, testPaperFragment)
            }
            2->{
                showView(tv_course)
                disMissView(iv_manager)
                switchFragment(lastFragment, examFragment)
            }
            3->{
                showView(iv_manager)
                disMissView(tv_course)
                switchFragment(lastFragment, myHomeworkFragment)
            }
            4->{
                disMissView(tv_course,iv_manager)
                switchFragment(lastFragment, homeworkCorrectFragment)
            }
        }
        lastPosition=position
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
            if (DataBeanManager.students.size==1){
                disMissView(tv_student)
                changeFragmentStudent(DataBeanManager.students[0].accountId)
            }
            else if (DataBeanManager.students.size>1){
                showView(tv_student)
                tv_student.text = DataBeanManager.students[0].nickname
                changeFragmentStudent(DataBeanManager.students[0].accountId)
            }
            else{
                disMissView(tv_student)
                changeFragmentStudent(0)
            }
        }
    }

}