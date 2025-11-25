package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.DataBeanManager.getResultStandardStr
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.teaching.ResultStandardItem
import com.bll.lnkcommon.ui.adapter.teaching.HomeworkResultRecordAdapter
import com.bll.lnkcommon.ui.adapter.teaching.HomeworkResultStandardAdapter
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.widget.SpaceItemDeco
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ResultStandardDetailsDialog(val context: Context, private val title:String, private val score:Double,private val questionType:Int,private val question:String,private val items:MutableList<ResultStandardItem>) {

    constructor(context: Context,title: String,score: Double,question: String):this(context, title, score, 11, question, mutableListOf<ResultStandardItem>())

    fun builder(): ResultStandardDetailsDialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.common_correct_result_standard)
        dialog.show()

        val ivClose=dialog.findViewById<ImageView>(R.id.iv_close)
        ivClose.setOnClickListener {
            dialog.dismiss()
        }

        val tvTitle=dialog.findViewById<TextView>(R.id.tv_title)
        tvTitle.text=title

        val tvScore=dialog.findViewById<TextView>(R.id.tv_score)
        val ratingBar=dialog.findViewById<RatingBar>(R.id.ratingBar)
        val recyclerview = dialog.findViewById<RecyclerView>(R.id.rv_list)

        when(questionType){
            10->{
                recyclerview.visibility=View.GONE
                ratingBar.visibility=View.VISIBLE
                tvScore.text=score.toString()
                ratingBar.rating=score.toFloat()
            }
            11->{
                if (question.isNotEmpty()&&question.length>20){
                    val results= Gson().fromJson(question, object : TypeToken<MutableList<ResultStandardItem>>() {}.type) as MutableList<ResultStandardItem>

                    recyclerview.layoutManager = LinearLayoutManager(context)//创建布局管理
                    HomeworkResultRecordAdapter(R.layout.item_homework_result_record,results).apply {
                        recyclerview.adapter = this
                        bindToRecyclerView(recyclerview)
                        recyclerview.addItemDecoration(SpaceItemDeco(20))
                    }
                }
                tvScore.text=score.toString()
            }
            else->{
                if (question.isNotEmpty()){
                    if (question.length<20){
                        val types= Gson().fromJson(question, object : TypeToken<MutableList<Int>>() {}.type) as MutableList<Int>
                        for (i in types.indices){
                            val type=types[i]
                            if (i<items.size){
                                val item = items[i]
                                for (childItem in item.list) {
                                    if (childItem.sort == type) {
                                        childItem.isCheck = true
                                    }
                                }
                            }
                        }
                    }
                }

                val layoutParam1= LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                layoutParam1.setMargins(
                    DP2PX.dip2px(context,if (questionType==2)10f else 40f), DP2PX.dip2px(context,30f),
                    DP2PX.dip2px(context,if (questionType==2)10f else 40f), DP2PX.dip2px(context,30f))
                recyclerview.layoutParams=layoutParam1

                val layoutManager=if (questionType==2) GridLayoutManager(context,items.size) else LinearLayoutManager(context)
                val layoutResId=if (questionType==2) R.layout.item_homework_result_standard_high else R.layout.item_homework_result_standard
                recyclerview.layoutManager = layoutManager//创建布局管理
                HomeworkResultStandardAdapter(layoutResId, questionType,items).apply {
                    recyclerview.adapter = this
                    bindToRecyclerView(recyclerview)
                    if (correctModule!=2){
                        recyclerview.addItemDecoration(SpaceItemDeco(50))
                    }
                }
                tvScore.text= getResultStandardStr(score,questionType)
            }
        }

        return this
    }

}