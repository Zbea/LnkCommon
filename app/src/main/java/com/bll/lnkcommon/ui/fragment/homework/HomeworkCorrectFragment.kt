package com.bll.lnkcommon.ui.fragment.homework

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.mvp.model.HomeworkCorrectList
import com.bll.lnkcommon.mvp.model.HomeworkCorrectList.CorrectBean
import com.bll.lnkcommon.mvp.presenter.HomeworkCorrectPresenter
import com.bll.lnkcommon.mvp.view.IContractView.IHomeworkCorrectView
import com.bll.lnkcommon.ui.activity.HomeworkCorrectActivity
import com.bll.lnkcommon.ui.adapter.HomeworkCorrectAdapter
import com.bll.lnkcommon.widget.SpaceItemDeco
import kotlinx.android.synthetic.main.fragment_my_homework.rv_list
import kotlinx.android.synthetic.main.item_homework_correct.*

class HomeworkCorrectFragment:BaseFragment(),IHomeworkCorrectView {

    private val mPresenter=HomeworkCorrectPresenter(this)
    private var mAdapter:HomeworkCorrectAdapter?=null
    private var homeworks= mutableListOf<CorrectBean>()
    private var studentId=0
    private var position=0

    override fun onList(list: HomeworkCorrectList?) {
        setPageNumber(list?.total!!)
        homeworks=list.list
        mAdapter?.setNewData(homeworks)
    }

    override fun onToken(token: String?) {
    }
    override fun onUpdateSuccess() {
    }
    override fun onDeleteSuccess() {
        showToast("删除成功")
        fetchData()
    }
    override fun onSendSuccess() {
        showToast("发送成功")
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_homework
    }
    override fun initView() {
        pageSize=6
        initRecyclerView()
        if(DataBeanManager.students.size>0)
            studentId= DataBeanManager.students[0].childId
    }
    override fun lazyLoad() {
        fetchData()
    }

    private fun initRecyclerView() {
        rv_list.layoutManager = LinearLayoutManager(activity)//创建布局管理
        mAdapter = HomeworkCorrectAdapter(R.layout.item_homework_correct, null)
        rv_list.adapter = mAdapter
        mAdapter?.bindToRecyclerView(rv_list)
        mAdapter?.setEmptyView(R.layout.common_empty)
        rv_list?.addItemDecoration(SpaceItemDeco(0, 0, 0, 40))
        mAdapter?.setOnItemClickListener { adapter, view, position ->
            this.position=position
            val item=homeworks[position]
            if (item.endTime==0L)
                return@setOnItemClickListener
            if (item.status!=1){
                val intent= Intent(requireActivity(), HomeworkCorrectActivity::class.java)
                val bundle= Bundle()
                bundle.putSerializable("correctBean", item)
                intent.putExtra("bundle", bundle)
                startActivity(intent)
            }
            else{
                showToast("学生作业未提交")
            }
        }
        mAdapter?.setOnItemChildClickListener { adapter, view, position ->
            this.position=position
            if (view.id==R.id.iv_delete){
                val map=HashMap<String,Any>()
                map["ids"]= arrayOf(homeworks[position].id)
                mPresenter.deleteCorrect(map)
            }
            if (view.id==R.id.tv_send){
                val map=HashMap<String,Any>()
                map["ids"]= arrayOf(homeworks[position].id)
                mPresenter.sendCorrect(map)
            }
        }
    }

    fun onChangeStudent(id:Int){
        pageIndex=1
        studentId=id
    }

    override fun onRefreshData() {
        super.onRefreshData()
        fetchData()
    }

    override fun fetchData() {
        val map=HashMap<String,Any>()
        map["page"]=pageIndex
        map["size"]=6
        map["childId"]=studentId
        mPresenter.getCorrects(map)
    }

}