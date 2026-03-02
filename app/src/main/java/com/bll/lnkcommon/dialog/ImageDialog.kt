package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.utils.GlideUtils
import com.bll.lnkcommon.utils.ToolUtils
import com.bll.lnkcommon.base.BaseDialog

/**
 * 图片预览弹窗：继承BaseDialog
 * @param context 上下文
 * @param images 图片URL列表
 * @param bgRes 背景资源列表（为空则传空列表）
 */
class ImageDialog(context: Context, private val images: List<String>, private val bgRes: List<String> = mutableListOf() ) : BaseDialog(context) {

    private var page = 0
    private val total = images.size - 1

    private var tvPage: TextView? = null
    private var ivImage: ImageView? = null
    private var ivBgImage: ImageView? = null

    override fun getLayoutResId(): Int = R.layout.dialog_image

    override fun initView(contentView: View) {
        ivImage = contentView.findViewById(R.id.iv_image)
        ivBgImage = contentView.findViewById(R.id.iv_bgres)
        val ivClose = contentView.findViewById<ImageView>(R.id.iv_close)
        val rlPage = contentView.findViewById<RelativeLayout>(R.id.rl_page)
        val ivUp = contentView.findViewById<ImageView>(R.id.iv_up)
        val ivDown = contentView.findViewById<ImageView>(R.id.iv_down)
        tvPage = contentView.findViewById(R.id.tv_page)

        ivClose.setOnClickListener {
            dismiss()
        }

        if (images.isNotEmpty()) {
            rlPage.visibility = View.VISIBLE
            setChange()
        }

        ivUp.setOnClickListener {
            if (page > 0) {
                page -= 1
                setChange()
            }
        }

        ivDown.setOnClickListener {
            if (page < total) {
                page += 1
                setChange()
            }
        }
    }

    private fun setChange() {
        // 设置背景资源
        if (bgRes.isNotEmpty()) {
            ivBgImage?.setBackgroundResource(ToolUtils.getImageResId(context, bgRes[page]))
        }
        // 加载图片
        GlideUtils.setImageUrl(context, images[page], ivImage)
        // 更新页码显示
        tvPage?.text = "${page + 1}/${total + 1}"
    }

    override fun builder(): ImageDialog {
        super.builder()
        return this
    }
}