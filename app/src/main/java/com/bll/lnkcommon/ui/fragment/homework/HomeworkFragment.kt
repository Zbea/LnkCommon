package com.bll.lnkcommon.ui.fragment.homework

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.dialog.ImageDialog
import com.bll.lnkcommon.mvp.model.Score
import com.bll.lnkcommon.mvp.model.TeacherHomeworkList
import com.bll.lnkcommon.mvp.model.TeacherHomeworkList.TeacherHomeworkBean
import com.bll.lnkcommon.mvp.presenter.HomeworkPresenter
import com.bll.lnkcommon.mvp.view.IContractView.IHomeworkView
import com.bll.lnkcommon.ui.activity.ScoreActivity
import com.bll.lnkcommon.ui.adapter.TeacherHomeworkAdapter
import com.bll.lnkcommon.widget.SpaceItemDeco
import kotlinx.android.synthetic.main.fragment_homework.rv_list

class HomeworkFragment : BaseFragment(),IHomeworkView {

    private var index=0//1作业2考卷
    private val presenter=HomeworkPresenter(this)
    private var homeworks = mutableListOf<TeacherHomeworkBean>()
    private var mAdapterHomework: TeacherHomeworkAdapter? = null
    private var studentId = 0

    private var courseStr=""
    private var position=0

    /**
     * 实例 传送数据
     */
    fun newInstance(index:Int): HomeworkFragment {
        val fragment= HomeworkFragment()
        val bundle= Bundle()
        bundle.putInt("index",index)
        fragment.arguments=bundle
        return fragment
    }

    override fun onList(item: TeacherHomeworkList) {
        setPageNumber(item.total)
        homeworks=item.list
        mAdapterHomework?.setNewData(homeworks)
    }
    override fun onDeleteSuccess() {
        mAdapterHomework?.remove(position)
    }

    override fun onScore(scores: MutableList<Score>?) {
    }


    override fun getLayoutId(): Int {
        return R.layout.fragment_homework
    }

    override fun initView() {
        index= arguments?.getInt("index")!!
        pageSize=6

        initRecyclerView()

        if(DataBeanManager.students.size>0)
            studentId=DataBeanManager.students[0].childId
    }

    override fun lazyLoad() {
        fetchData()
    }

    private fun initRecyclerView() {
        rv_list.layoutManager = LinearLayoutManager(activity)//创建布局管理
        mAdapterHomework = TeacherHomeworkAdapter(R.layout.item_homework_teacher, null,index)
        rv_list.adapter = mAdapterHomework
        mAdapterHomework?.bindToRecyclerView(rv_list)
        mAdapterHomework?.setEmptyView(R.layout.common_empty)
        rv_list?.addItemDecoration(SpaceItemDeco(0, 0, 0, 60))
        mAdapterHomework?.setOnItemChildClickListener { adapter, view, position ->
            this.position=position
            val item=homeworks[position]
            when(view.id){
                R.id.iv_delete->{
                    CommonDialog(requireActivity()).setContent("确定删除？").builder().setDialogClickListener(
                        object : CommonDialog.OnDialogClickListener {
                            override fun cancel() {
                            }
                            override fun ok() {
                                val map=HashMap<String,Any>()
                                map["ids"]=arrayOf(item.id)
                                presenter.deleteHomeworks(map)
                            }
                        })
                }
                R.id.tv_image_content->{
                    setImageView(item.homeworkContent)
                }
                R.id.tv_image_commit->{
                    setImageView(item.submitContent)
                }
                R.id.tv_image_correct->{
                    setImageView(item.correctContent)
                }
                R.id.iv_rank->{
                    customStartActivity(Intent(requireActivity(),ScoreActivity::class.java).setFlags(item.studentTaskId))
                }
            }
        }
    }

    /**
     * 查看图片
     */
    private fun setImageView(path:String){
        val images=path.split(",").toList()
        ImageDialog(requireActivity(),images).builder()
    }

    fun onChangeStudent(id:Int){
        pageIndex=1
        studentId=id
        fetchData()
    }

    fun onChangeCourse(coures:String){
        courseStr=coures
        pageIndex=1
        fetchData()
    }

    override fun onRefreshData() {
        courseStr=""
        fetchData()
    }

    override fun fetchData() {
        homeworks.clear()
        val map = HashMap<String, Any>()
        map["page"] = pageIndex
        map["size"] = pageSize
        map["childId"]=studentId
        map["type"] =index
        if (courseStr.isNotEmpty())
            map["subject"]=courseStr
        presenter.getHomeworks(map)
    }


}