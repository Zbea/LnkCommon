package com.bll.lnkcommon.dialog

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDialog
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.GlideUtils

class PreviewDialog(
    context: Context,
    private val title: String,
    private val content: String,
    private val images: List<String>
) : BaseDialog(context) {

    // 自定义宽度
    override val defaultWidth: Int
        get() = if (images.isNotEmpty()) DP2PX.dip2px(context,720f) else DP2PX.dip2px(context,500f)

    private var page = 0
    private val total get() = images.size - 1
    private var tvPage: TextView? = null
    private var ivImage: ImageView? = null

    override fun getLayoutResId(): Int = R.layout.dialog_preview

    override fun initView(contentView: View) {
        // 初始化控件
        ivImage = contentView.findViewById(R.id.iv_image)
        val ivClose = contentView.findViewById<ImageView>(R.id.iv_close)
        val rlImage = contentView.findViewById<RelativeLayout>(R.id.rl_image)
        val ivUp = contentView.findViewById<ImageView>(R.id.iv_up)
        val ivDown = contentView.findViewById<ImageView>(R.id.iv_down)
        tvPage = contentView.findViewById(R.id.tv_page)
        val tvContent = contentView.findViewById<TextView>(R.id.tv_content)
        val tvTitle = contentView.findViewById<TextView>(R.id.tv_title)

        // 设置标题和内容
        tvTitle.text = title
        tvContent.text = content

        // 关闭按钮
        ivClose.setOnClickListener { dismiss() }

        // 图片区域显示控制
        if (images.isNotEmpty()) {
            rlImage.visibility = View.VISIBLE
            setChange()
        }

        // 上一页
        ivUp.setOnClickListener {
            if (page > 0) {
                page -= 1
                setChange()
            }
        }

        // 下一页
        ivDown.setOnClickListener {
            if (page < total) {
                page += 1
                setChange()
            }
        }
    }

    private fun setChange() {
        // 加载图片
        GlideUtils.setImageUrl(context, images[page], ivImage)
        // 更新页码
        tvPage?.text = "${page + 1}/${total + 1}"
    }
}