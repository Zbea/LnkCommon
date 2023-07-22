package com.bll.lnkcommon.ui.activity

import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.PopupRadioList
import com.bll.lnkcommon.mvp.model.PopupBean
import com.bll.lnkcommon.mvp.model.Score
import com.bll.lnkcommon.mvp.model.StudentBean
import com.bll.lnkcommon.mvp.model.TeacherHomeworkList
import com.bll.lnkcommon.mvp.presenter.HomeworkPresenter
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.ui.adapter.ScoreAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.widget.SpaceGridItemDecoScore
import kotlinx.android.synthetic.main.ac_score.*
import kotlinx.android.synthetic.main.common_title.*

class ScoreActivity:BaseActivity(),IContractView.IHomeworkView{

    private val mPresenter= HomeworkPresenter(this)
    private var mAdapter: ScoreAdapter?=null
    private var datas= mutableListOf<Score>()

    override fun onList(item: TeacherHomeworkList?) {
    }
    override fun onDeleteSuccess() {
    }
    override fun onScore(scores: MutableList<Score>) {
        datas=scores
        mAdapter?.setNewData(datas)
    }

    override fun layoutId(): Int {
        return R.layout.ac_score
    }

    override fun initData() {
        mPresenter.onScore(intent.flags)
    }

    override fun initView() {
        setPageTitle("成绩统计")

        initRecyclerView()
    }

    private fun initRecyclerView(){
        mAdapter = ScoreAdapter(R.layout.item_score,datas)
        rv_list.layoutManager = GridLayoutManager(this,2)
        rv_list.adapter = mAdapter
        mAdapter?.bindToRecyclerView(rv_list)
        rv_list.addItemDecoration(SpaceGridItemDecoScore(DP2PX.dip2px(this,40f),0))
    }

}