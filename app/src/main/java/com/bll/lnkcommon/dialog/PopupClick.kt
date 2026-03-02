import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BasePopupWindow
import com.bll.lnkcommon.mvp.model.PopupBean
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class PopupClick(
    context: Context,
    private val list: MutableList<PopupBean>,
    anchorView: View,
    layoutWidth: Int = 0,
    yOffset: Int = 0
) : BasePopupWindow(context, anchorView, layoutWidth, 0, yOffset) {

    constructor(context: Context, list: MutableList<PopupBean>, view: View, yoff: Int) :
            this(context, list, view, 0, yoff)

    override fun getLayoutResId(): Int = R.layout.popup_list

    override fun initView() {
        val mAdapter = initRecyclerView(
            rvId = R.id.rv_list,
            data = list
            ) { layoutId, data ->
            object : BaseQuickAdapter<PopupBean, BaseViewHolder>(layoutId, data) {
                override fun convert(helper: BaseViewHolder, item: PopupBean) {
                    if (item.resId == 0) {
                        helper.getView<LinearLayout>(R.id.ll_content).gravity = Gravity.CENTER
                    }
                    helper.setText(R.id.tv_name, item.name)
                    helper.setImageResource(R.id.iv_check, item.resId)
                    helper.setGone(R.id.iv_check, item.resId != 0)
                }
            }
        }

        mAdapter.setOnItemClickListener { _, _, position ->
            notifySelect(list[position])
        }
    }
}