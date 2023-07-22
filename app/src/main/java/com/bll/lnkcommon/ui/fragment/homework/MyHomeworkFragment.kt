package com.bll.lnkcommon.ui.fragment.homework

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.dialog.HomeworkPublishDialog
import com.bll.lnkcommon.mvp.model.HomeworkTypeBean
import com.bll.lnkcommon.ui.adapter.HomeworkTypeAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.widget.SpaceGridItemDeco1
import kotlinx.android.synthetic.main.fragment_my_homework.*

class MyHomeworkFragment:BaseFragment() {

    private var studentId=0
    private var homeworkTypes= mutableListOf<HomeworkTypeBean>()
    private var mAdapter:HomeworkTypeAdapter?=null

    override fun getLayoutId(): Int {
        return R.layout.fragment_my_homework
    }
    override fun initView() {
        if (DataBeanManager.students.size>0)
            studentId=DataBeanManager.students[0].childId
        initRecyclerView()
    }
    override fun lazyLoad() {
        homeworkTypes.add(HomeworkTypeBean().apply {
            name="辅导作业本"
            subject="语文"
        })

        homeworkTypes.add(HomeworkTypeBean().apply {
            name="数学习题本"
            subject="数学"
        })

        mAdapter?.setNewData(homeworkTypes)
    }

    private fun initRecyclerView() {
        mAdapter = HomeworkTypeAdapter(R.layout.item_my_homework, null).apply {
            rv_list.layoutManager = GridLayoutManager(activity, 3)
            rv_list.adapter = this
            bindToRecyclerView(rv_list)
            rv_list.addItemDecoration(SpaceGridItemDeco1(3, DP2PX.dip2px(activity, 33f), 40))
            setOnItemClickListener { adapter, view, position ->
                sendHomework()
            }
        }
    }

    /**
     * 布置作业
     */
    private fun sendHomework(){
        HomeworkPublishDialog(requireActivity()).builder().setOnDialogClickListener{
            content,date->

        }
    }

    fun onChangeStudent(id:Int){

    }

}