package com.bll.lnkcommon.ui.fragment.homework

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.MyApplication
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseFragment
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.dialog.HomeworkPublishDialog
import com.bll.lnkcommon.dialog.InputContentDialog
import com.bll.lnkcommon.dialog.LongClickManageDialog
import com.bll.lnkcommon.mvp.model.HomeworkTypeList
import com.bll.lnkcommon.mvp.model.ItemList
import com.bll.lnkcommon.mvp.presenter.MyHomeworkPresenter
import com.bll.lnkcommon.mvp.view.IContractView.IMyHomeworkView
import com.bll.lnkcommon.ui.adapter.HomeworkTypeAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.NetworkUtil
import com.bll.lnkcommon.widget.SpaceGridItemDeco
import com.bll.lnkcommon.widget.SpaceGridItemDeco1
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_list_content.*

class MyHomeworkFragment:BaseFragment(),IMyHomeworkView {

    private var presenter=MyHomeworkPresenter(this)
    private var studentId=0
    private var homeworkTypes= mutableListOf<HomeworkTypeList.HomeworkTypeBean>()
    private var mAdapter:HomeworkTypeAdapter?=null
    private var position=0
    private var editNameStr=""

    override fun onList(homeworkTypeList: HomeworkTypeList) {
        setPageNumber(homeworkTypeList.total)
        homeworkTypes=homeworkTypeList.list
        mAdapter?.setNewData(homeworkTypes)
    }
    override fun onCreateSuccess() {
        pageIndex=1
        fetchData()
    }

    override fun onEditSuccess() {
        homeworkTypes[position].name=editNameStr
        mAdapter?.notifyItemChanged(position)
    }

    override fun onDelete() {
        mAdapter?.remove(position)
    }

    override fun onSendSuccess() {
        showToast("布置成功")
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_list_content
    }
    override fun initView() {
        pageSize=9
        if (DataBeanManager.students.size>0)
            studentId=DataBeanManager.students[0].accountId
        initRecyclerView()
    }
    override fun lazyLoad() {
        if (NetworkUtil.isNetworkConnected())
            fetchData()
    }

    private fun initRecyclerView() {
        val layoutParams= LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.setMargins(DP2PX.dip2px(requireActivity(),30f), DP2PX.dip2px(requireActivity(),30f), DP2PX.dip2px(requireActivity(),30f),0)
        layoutParams.weight=1f
        rv_list.layoutParams= layoutParams

        mAdapter = HomeworkTypeAdapter(R.layout.item_my_homework, null).apply {
            rv_list.layoutManager = GridLayoutManager(activity, 3)
            rv_list.adapter = this
            bindToRecyclerView(rv_list)
            rv_list.addItemDecoration(SpaceGridItemDeco(3, 85))
            setOnItemClickListener { adapter, view, position ->
                sendHomework(homeworkTypes[position])
            }
            setOnItemLongClickListener { adapter, view, position ->
                this@MyHomeworkFragment.position=position
                onLongClick()
                true
            }
        }
    }

    private fun onLongClick(){
        val item=homeworkTypes[position]
        val beans = mutableListOf<ItemList>()
        beans.add(ItemList().apply {
            name = "删除"
            resId = R.mipmap.icon_setting_delete
        })
        beans.add(ItemList().apply {
            name = "重命名"
            resId = R.mipmap.icon_setting_edit
        })
        LongClickManageDialog(requireActivity(),item.name, beans).builder()
            .setOnDialogClickListener { position->
                when(position){
                    0->{
                        val map=HashMap<String,Any>()
                        map["ids"]= arrayOf(item.id)
                        presenter.deleteHomeworkType(map)
                    }
                    1->{
                        InputContentDialog(requireActivity(),item.name).builder().setOnDialogClickListener{
                            editNameStr=it
                            val map=HashMap<String,Any>()
                            map["id"]= item.id
                            map["name"]= it
                            presenter.editHomeworkType(map)
                        }
                    }
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
        presenter.createHomeworkType(map)
    }

    fun onChangeStudent(id:Int){
        studentId=id
        pageIndex=1
        fetchData()
    }

    override fun onRefreshData() {
        lazyLoad()
    }

    override fun fetchData() {
        val map=HashMap<String,Any>()
        map["size"]=pageSize
        map["page"]=pageIndex
        map["childId"]=studentId
        presenter.getHomeworks(map)
    }

    override fun onEventBusMessage(msgFlag: String) {
        when (msgFlag) {
            Constants.NETWORK_CONNECTION_COMPLETE_EVENT ->{
                lazyLoad()
            }
        }
    }

}