import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.PopupBean
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class DialogClick(var context: Context, var list: MutableList<PopupBean>) {

    fun builder(): DialogClick {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_list)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        val rvList = dialog.findViewById<RecyclerView>(R.id.rv_list)
        rvList.layoutManager = LinearLayoutManager(context)//创建布局管理
        val mAdapter = MAdapter(R.layout.item_popwindow_list, list)
        rvList.adapter=mAdapter
        mAdapter.bindToRecyclerView(rvList)
        mAdapter.setOnItemClickListener { adapter, view, position ->
            onSelectListener?.onClick(list[position])
            dialog.dismiss()
        }

        return this
    }

    private var onSelectListener: OnItemClickListener? = null

    fun setOnSelectListener(onSelectListener: OnItemClickListener) {
        this.onSelectListener = onSelectListener
    }

    fun interface OnItemClickListener {
        fun onClick(item: PopupBean)
    }

    private class MAdapter(layoutResId: Int, data: List<PopupBean>?) :
        BaseQuickAdapter<PopupBean, BaseViewHolder>(layoutResId, data) {

        override fun convert(helper: BaseViewHolder, item: PopupBean) {
            helper.setText(R.id.tv_name, item.name)
            helper.setImageResource(R.id.iv_check, item.resId)
            helper.setGone(R.id.iv_check, item.resId != 0)
        }

    }

}