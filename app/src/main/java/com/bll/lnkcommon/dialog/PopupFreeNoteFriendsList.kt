
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
import com.bll.lnkcommon.mvp.model.FriendList.FriendBean
import com.bll.lnkcommon.utils.DP2PX
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class PopupFreeNoteFriendsList(var context: Context, var list: MutableList<FriendBean>, var view: View ) {

    private var mPopupWindow: PopupWindow? = null

    fun builder(): PopupFreeNoteFriendsList {
        val popView = LayoutInflater.from(context).inflate(R.layout.popup_freenote_friends, null, false)
        mPopupWindow = PopupWindow(context).apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            // 设置PopupWindow的内容view
            contentView = popView
            isFocusable = true // 设置PopupWindow可获得焦点
            isTouchable = true // 设置PopupWindow可触摸
            isOutsideTouchable = true // 设置非PopupWindow区域可触摸
            isClippingEnabled = false
            width= DP2PX.dip2px(context,280f)
        }

        val rvList = popView.findViewById<RecyclerView>(R.id.rv_list)
        rvList.layoutManager = LinearLayoutManager(context)//创建布局管理
        val mAdapter = MAdapter(R.layout.item_freenote_friend, list)
        rvList.adapter = mAdapter
        mAdapter.bindToRecyclerView(rvList)
        mAdapter.setOnItemChildClickListener { adapter, view, position ->
            if (view.id==R.id.iv_delete){
                onSelectListener?.onClick(position,list[position])
                dismiss()
            }
        }

        show()
        return this
    }

    fun dismiss() {
        if (mPopupWindow != null) {
            mPopupWindow?.dismiss()
        }
    }

    fun show() {
        if (mPopupWindow != null) {
            mPopupWindow?.showAsDropDown(view, 0, 0, Gravity.RIGHT)
        }
    }

    private var onSelectListener: OnClickListener? = null

    fun setOnSelectListener(onSelectListener: OnClickListener) {
        this.onSelectListener = onSelectListener
    }

    fun interface OnClickListener {
        fun onClick(pos:Int,item: FriendBean)
    }

    private class MAdapter(layoutResId: Int, data: List<FriendBean>?) :
        BaseQuickAdapter<FriendBean, BaseViewHolder>(layoutResId, data) {

        override fun convert(helper: BaseViewHolder, item: FriendBean) {
            helper.setText(R.id.tv_friend_id, item.account)
            helper.setText(R.id.tv_friend_name, item.nickname)
            helper.addOnClickListener(R.id.iv_delete)
        }

    }

}