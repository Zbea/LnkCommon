package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.ScoreItemUtils
import com.bll.lnkcommon.widget.ScoreTreeLayout

class ScoreDetailsDialog(val context: Context, private val title:String, private val score:Double,
                         private val correctMode:Int,private val scoreMode:Int,private val answerImages:MutableList<String>,
                         private val commitJson:String) {

    private var isExpend=false

    fun builder(): ScoreDetailsDialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.common_correct_score)
        dialog.show()

        val ivClose=dialog.findViewById<ImageView>(R.id.iv_close)
        ivClose.setOnClickListener {
            dialog.dismiss()
        }
        val iv_score_up=dialog.findViewById<ImageView>(R.id.iv_score_up)
        val iv_score_down=dialog.findViewById<ImageView>(R.id.iv_score_down)

        val rl_score_content=dialog.findViewById<RelativeLayout>(R.id.rl_score_content)
        val iv_expand_arrow=dialog.findViewById<ImageView>(R.id.iv_expand_arrow)
        if (correctMode<=0)
            iv_expand_arrow.visibility=View.GONE
        iv_expand_arrow.setOnClickListener {
            isExpend = !isExpend
            val layoutParams = rl_score_content.layoutParams
            if (isExpend) {
                iv_expand_arrow.setImageResource(R.mipmap.icon_topic_arrow_shrink)
                layoutParams.height = DP2PX.dip2px(context, 1000f)
            } else {
                iv_expand_arrow.setImageResource(R.mipmap.icon_topic_arrow_expend)
                layoutParams.height = DP2PX.dip2px(context, 500f)
            }
            rl_score_content.layoutParams = layoutParams
        }

        val tvTitle=dialog.findViewById<TextView>(R.id.tv_title)
        tvTitle.text=title

        val tvScore=dialog.findViewById<TextView>(R.id.tv_score)
        tvScore.text= DataBeanManager.getScoreStandardStr(score,correctMode)

        val tvAnswer=dialog.findViewById<TextView>(R.id.tv_answer)
        tvAnswer.visibility=if (answerImages.isEmpty()) View.GONE else View.VISIBLE
        tvAnswer.setOnClickListener {
            ImageDialog(context, answerImages).builder()
        }
        val sv_score = dialog.findViewById<ScrollView>(R.id.sv_score)
        val sl_score = dialog.findViewById<ScoreTreeLayout>(R.id.sl_score)


        if (correctMode >0) {
            val currentScores = ScoreItemUtils.questionToList(commitJson,correctMode)
            sl_score.bindData(currentScores,false)
        }
        else{
            val currentResults=ArrayList(DataBeanManager.getResultChildItems())
            for (item in currentResults){
                if (item.sort==score.toInt()){
                    item.isCheck=true
                }
            }
            sl_score.bindData(currentResults)
        }


        iv_score_up.setOnClickListener {
            sv_score.scrollBy(0,-DP2PX.dip2px(context,300f))
        }

        iv_score_down.setOnClickListener {
            sv_score.scrollBy(0, DP2PX.dip2px(context,300f))
        }

        return this
    }
}