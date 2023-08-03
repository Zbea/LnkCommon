package com.bll.lnkcommon.ui.fragment.homework

import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.dialog.HomeworkPublishDialog
import com.bll.lnkcommon.mvp.model.HomeworkTypeList
import com.bll.lnkcommon.mvp.presenter.MyHomeworkPresenter
import com.bll.lnkcommon.mvp.view.IContractView.IMyHomeworkView
import com.bll.lnkcommon.ui.adapter.HomeworkTypeAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.widget.SpaceGridItemDeco1
import kotlinx.android.synthetic.main.fragment_my_homework.*

class MyHomeworkFragment:BaseFragment(),IMyHomeworkView {

    private var presenter=MyHomeworkPresenter(this)
    private var studentId=0
    private var homeworkTypes= mutableListOf<HomeworkTypeList.HomeworkTypeBean>()
    private var mAdapter:HomeworkTypeAdapter?=null
    private var position=0

    override fun onList(homeworkTypeList: HomeworkTypeList) {
        setPageNumber(homeworkTypeList.total)
        homeworkTypes=homeworkTypeList.list
        mAdapter?.setNewData(homeworkTypes)
    }
    override fun onCreateSuccess() {
        pageIndex=1
        fetchData()
    }
    override fun onDeleteSuccess() {
        mAdapter?.remove(position)
    }

    override fun onSendSuccess() {
        showToast("布置成功")
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_my_homework
    }
    override fun initView() {
        pageSize=9
        if (DataBeanManager.students.size>0)
            studentId=DataBeanManager.students[0].childId
        initRecyclerView()
    }
    override fun lazyLoad() {
        fetchData()
    }

    private fun initRecyclerView() {
        mAdapter = HomeworkTypeAdapter(R.layout.item_my_homework, null).apply {
            rv_list.layoutManager = GridLayoutManager(activity, 3)
            rv_list.adapter = this
            bindToRecyclerView(rv_list)
            rv_list.addItemDecoration(SpaceGridItemDeco1(3, DP2PX.dip2px(activity, 33f), 80))
            setOnItemClickListener { adapter, view, position ->
                sendHomework(homeworkTypes[position])
            }
            setOnItemLongClickListener { adapter, view, position ->
                this@MyHomeworkFragment.position=position
                CommonDialog(requireActivity()).setContent("确定删除作业本").builder().setDialogClickListener(
                    object : CommonDialog.OnDialogClickListener {
                        override fun cancel() {
                        }
                        override fun ok() {
                            val map=HashMap<String,Any>()
                            map["ids"]= arrayOf(homeworkTypes[position].id)
                            presenter.deleteHomeworkType(map)
                        }
                    })
                true
            }
        }
    }

    /**
     * 布置作业
     */
    private fun sendHomework(item:HomeworkTypeList.HomeworkTypeBean){
        HomeworkPublishDialog(requireActivity()).builder().setOnDialogClickListener{
            content,date->
            val map=HashMap<String,Any>()
            map["id"]=item.id
            map["title"]=content
            map["endTime"]=date
            presenter.sendHomework(map)
        }
    }

    /**
     * 创建作业本
     */
    fun createHomeworkType(name:String,courseId:Int){
        val map=HashMap<String,Any>()
        map["name"]=name
        map["subject"]=courseId
        map["type"]=1
        map["childId"]=studentId
        map["imageUrl"]=DataBeanManager.homeworkCoverStr()
        presenter.createHomeworkType(map)
    }

    fun onChangeStudent(id:Int){
        studentId=id
        pageIndex=1
        fetchData()
    }

    override fun onRefreshData() {
        fetchData()
    }

    override fun fetchData() {
        val map=HashMap<String,Any>()
        map["size"]=pageSize
        map["page"]=pageIndex
        map["childId"]=studentId
        presenter.getHomeworks(map)
    }

}