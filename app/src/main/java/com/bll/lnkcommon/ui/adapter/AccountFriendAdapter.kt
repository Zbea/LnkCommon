package com.bll.lnkcommon.ui.adapter

import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.FriendList
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class AccountFriendAdapter(layoutResId: Int, data: List<FriendList.FriendBean>?) : BaseQuickAdapter<FriendList.FriendBean, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: FriendList.FriendBean) {
        helper.apply {
            setText(R.id.tv_friend_id,item.account)
            setText(R.id.tv_friend_name,item.nickname)
            addOnClickListener(R.id.tv_friend_cancel)
        }
    }

}
