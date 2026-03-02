package com.bll.lnkcommon.dialog

import android.content.Context
import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.Constants
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseDialog
import com.bll.lnkcommon.manager.AppDaoManager
import com.bll.lnkcommon.mvp.model.AppBean
import com.bll.lnkcommon.ui.activity.drawing.PlanOverviewActivity
import com.bll.lnkcommon.utils.AppUtils
import com.bll.lnkcommon.utils.BitmapUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

/**
 * 应用工具弹窗：继承BaseDialog，复用通用逻辑
 */
class AppToolDialog(context: Context) : BaseDialog(context) {

    private lateinit var appLists: List<AppBean>
    override val defaultGravity: Int = Gravity.BOTTOM or Gravity.START // 左下位置
    override val defaultXOffset: Float = 42f // X偏移42dp
    override val defaultYOffset: Float = 5f // Y偏移5dp
    private var onDialogClickListener: OnDialogClickListener? = null

    override fun getLayoutResId(): Int = R.layout.dialog_app_tool

    override fun initView(contentView: View) {
        appLists = AppDaoManager.getInstance().queryToolAll()
        // 过滤几何包（PlanOverviewActivity场景）
        if (context is PlanOverviewActivity) {
            val appBean = AppDaoManager.getInstance().queryBeanByPackageName(Constants.PACKAGE_GEOMETRY)
            appBean?.let { appLists = appLists.filter { it != appBean } }
        }

        val rvList = contentView.findViewById<RecyclerView>(R.id.rv_list)
        rvList?.layoutManager = LinearLayoutManager(context)
        val mAdapter = MyAdapter(R.layout.item_app_name_list, appLists)
        mAdapter.bindToRecyclerView(rvList)

        mAdapter.setOnItemClickListener { _, _, position ->
            val packageName = appLists[position].packageName
            if (packageName == Constants.PACKAGE_GEOMETRY) {
                // 触发几何包回调
                onDialogClickListener?.onClick()
            } else {
                // 启动其他应用
                AppUtils.startAPP(context, packageName)
            }
            dismiss()
        }
    }

    fun interface OnDialogClickListener {
        fun onClick()
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener?): AppToolDialog {
        this.onDialogClickListener = listener
        return this
    }

    override fun builder(): AppToolDialog {
        super.builder()
        return this
    }

    class MyAdapter(layoutResId: Int, data: List<AppBean>?) : BaseQuickAdapter<AppBean, BaseViewHolder>(layoutResId, data) {
        override fun convert(helper: BaseViewHolder, item: AppBean) {
            helper.setText(R.id.tv_name, item.appName)
            helper.setImageDrawable(R.id.iv_image, BitmapUtils.byteToDrawable(item.imageByte))
        }
    }
}