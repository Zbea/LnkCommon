package com.bll.lnkcommon.ui.fragment.homework

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.dialog.ImageDialog
import com.bll.lnkcommon.mvp.model.ExamList
import com.bll.lnkcommon.mvp.model.Score
import com.bll.lnkcommon.mvp.model.TeacherHomeworkList
import com.bll.lnkcommon.mvp.model.TeacherHomeworkList.TeacherHomeworkBean
import com.bll.lnkcommon.mvp.presenter.ExamPresenter
import com.bll.lnkcommon.mvp.presenter.HomeworkPresenter
import com.bll.lnkcommon.mvp.view.IContractView.IExamView
import com.bll.lnkcommon.mvp.view.IContractView.IHomeworkView
import com.bll.lnkcommon.ui.activity.ScoreActivity
import com.bll.lnkcommon.ui.adapter.ExamAdapter
import com.bll.lnkcommon.ui.adapter.TeacherHomeworkAdapter
import com.bll.lnkcommon.utils.NetworkUtil
import com.bll.lnkcommon.widget.SpaceItemDeco
import kotlinx.android.synthetic.main.fragment_homework.rv_list

class ExamFragment : BaseFragment(),IExamView {
    private val presenter=ExamPresenter(this)
    private var exams = mutableListOf<ExamList.ExamBean>()
    private var mAdapter: ExamAdapter? = null
    private var studentId = 0
    private var courseStr=""
    private var position=0

    override fun onList(item: ExamList) {
        setPageNumber(item.total)
        exams=item.list
        mAdapter?.setNewData(exams)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_homework
    }

    override fun initView() {
        pageSize=6

        initRecyclerView()

        if(DataBeanManager.students.size>0)
            studentId=DataBeanManager.students[0].accountId
    }

    override fun lazyLoad() {
        if (NetworkUtil.isNetworkAvailable(requireActivity()))
            fetchData()
    }

    private fun initRecyclerView() {
        rv_list.layoutManager = LinearLayoutManager(activity)//创建布局管理
        mAdapter = ExamAdapter(R.layout.item_homework_exam, null)
        rv_list.adapter = mAdapter
        mAdapter?.bindToRecyclerView(rv_list)
        mAdapter?.setEmptyView(R.layout.common_empty)
        rv_list?.addItemDecoration(SpaceItemDeco(0, 0, 0, 60))
        mAdapter?.setOnItemChildClickListener { adapter, view, position ->
            this.position=position
            val item=exams[position]
            when(view.id){
                R.id.tv_image_content->{
                    setImageView(item.examUrl)
                }
                R.id.tv_image_commit->{
                    setImageView(item.studentUrl)
                }
                R.id.tv_image_correct->{
                    setImageView(item.teacherUrl)
                }
                R.id.iv_rank->{
                    customStartActivity(Intent(requireActivity(),ScoreActivity::class.java)
                        .setFlags(2)
                        .putExtra("id",item.schoolExamJobId)
                        .putExtra("classId",item.classId)
                        .putExtra("className",item.className)
                    )
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
        lazyLoad()
    }

    override fun fetchData() {
        exams.clear()
        val map = HashMap<String, Any>()
        map["page"] = pageIndex
        map["size"] = pageSize
        map["childId"]=studentId
        if (courseStr.isNotEmpty())
            map["type"]=DataBeanManager.getCourseId(courseStr)
        presenter.getExams(map)
    }


}