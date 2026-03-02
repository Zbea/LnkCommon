package com.bll.lnkcommon.dialog

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDialog
import com.bll.lnkcommon.utils.ScoreItemUtils
import com.bll.lnkcommon.widget.ScoreTreeLayout

class ScoreDetailsDialog(
    context: Context,
    private val title: String,
    private val score: Double,
    private val correctMode: Int,
    private val scoreMode: Int,
    private val answerImages: MutableList<String>,
    private val commitJson: String
) : BaseDialog(context) {

    private var isExpend = false

    // 布局ID
    override fun getLayoutResId(): Int = R.layout.common_correct_score

    // 初始化视图和业务逻辑
    override fun initView(contentView: View) {
        // 关闭按钮
        val ivClose = contentView.findViewById<ImageView>(R.id.iv_close)
        ivClose.setOnClickListener { dismiss() }

        // 滚动控制按钮
        val ivScoreUp = contentView.findViewById<ImageView>(R.id.iv_score_up)
        val ivScoreDown = contentView.findViewById<ImageView>(R.id.iv_score_down)

        // 展开/收起相关
        val rlScoreContent = contentView.findViewById<RelativeLayout>(R.id.rl_score_content)
        val ivExpandArrow = contentView.findViewById<ImageView>(R.id.iv_expand_arrow)

        // 标题和评分
        val tvTitle = contentView.findViewById<TextView>(R.id.tv_title)
        val tvScore = contentView.findViewById<TextView>(R.id.tv_score)
        val tvAnswer = contentView.findViewById<TextView>(R.id.tv_answer)

        // 滚动视图和评分布局
        val svScore = contentView.findViewById<ScrollView>(R.id.sv_score)
        val slScore = contentView.findViewById<ScoreTreeLayout>(R.id.sl_score)

        // 设置标题
        tvTitle.text = title

        // 展开/收起箭头控制
        ivExpandArrow.visibility = if (correctMode <= 0) View.GONE else View.VISIBLE
        ivExpandArrow.setOnClickListener {
            isExpend = !isExpend
            val layoutParams = rlScoreContent.layoutParams
            layoutParams.height = if (isExpend) {
                ivExpandArrow.setImageResource(R.mipmap.icon_topic_arrow_shrink)
                dp2px(1000f)
            } else {
                ivExpandArrow.setImageResource(R.mipmap.icon_topic_arrow_expend)
                dp2px(500f)
            }
            rlScoreContent.layoutParams = layoutParams
        }

        // 设置评分文本
        tvScore.text = DataBeanManager.getScoreStandardStr(score, correctMode)

        // 答题图片显示控制
        tvAnswer.visibility = if (answerImages.isEmpty()) View.GONE else View.VISIBLE
        tvAnswer.setOnClickListener {
            ImageDialog(context, answerImages).builder()
        }

        // 绑定评分数据
        if (correctMode > 0) {
            val currentScores = ScoreItemUtils.questionToList(commitJson, correctMode)
            slScore.bindData(currentScores, false)
        } else {
            val currentResults = ArrayList(DataBeanManager.getResultChildItems())
            currentResults.forEach { item ->
                if (item.sort == score.toInt()) {
                    item.isCheck = true
                }
            }
            slScore.bindData(currentResults)
        }

        // 滚动控制
        ivScoreUp.setOnClickListener {
            svScore.scrollBy(0, -dp2px(300f))
        }

        ivScoreDown.setOnClickListener {
            svScore.scrollBy(0, dp2px(300f))
        }
    }
}