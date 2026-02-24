package com.bll.lnkcommon.widget

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.mvp.model.teaching.ResultStandardItem
import com.bll.lnkcommon.mvp.model.teaching.ScoreItem
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class ScoreTreeLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {
    private val levelPadding= 40//缩放间距
    private var isShowResult=true
    private var childNodeAdapter: ChildNodeAdapter?=null
    private var childResultAdapter: ChildResultStandardAdapter?=null

    enum class ClickViewType {
        TV_SCORE, // 点击了分数文本
        IV_RESULT // 点击了对错图标
    }

    interface OnScoreItemClickListener {
        fun onScoreItemClick(clickViewType: ClickViewType, item: ScoreItem)
    }

    fun interface OnResultItemClickListener {
        fun onResultItemClick(position:Int)
    }

    private var mGlobalListener: OnScoreItemClickListener? = null
    private var mResultListener: OnResultItemClickListener? = null

    init {
        orientation = VERTICAL
        setPadding(60, 10, 60, 10)
    }

    /**
     * 绑定分数
     */
    fun bindData(rootDataList: MutableList<ScoreItem>?, isShow:Boolean=true, listener: OnScoreItemClickListener?=null) {
        isShowResult=isShow
        mGlobalListener = listener
        removeAllViews()
        if (rootDataList.isNullOrEmpty()) return

        val isSingleLevel = rootDataList.all { it.childScores.isEmpty() }
        if (isSingleLevel){
            createSingleLevelRecyclerView(rootDataList)
        }
        else{
            processScoreItemBottomFlag(rootDataList)
            rootDataList.forEach { rootItem ->
                val singleTree = ScoreItemView(context)
                singleTree.bindData(rootItem)
                addView(singleTree, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
            }
        }
    }


    /**
     * 绑定标准结果
     */
    fun bindData(rootDataList: MutableList<ResultStandardItem.ResultChildItem>?, listener: OnResultItemClickListener?=null) {
        mResultListener = listener
        removeAllViews()
        if (rootDataList.isNullOrEmpty()) return

        createResultRecyclerView(rootDataList)
    }

    fun updateResult(){
        childResultAdapter?.notifyDataSetChanged()
    }


    /**
     * 更新节点+父节点
     * @param item 被点击的子节点
     */
    fun updateNodeAndParent(item: ScoreItem) {
        if (childNodeAdapter==null){
            //刷新当前节点
            findTargetTree(item)?.refreshScore()
            //递归更新父节点
            updateParentScoreRecursive(item.parentItem)
        }
        else{
            childNodeAdapter?.notifyDataSetChanged()
        }
    }

    /**
     * 递归查找节点
     */
    private fun findTargetTree(target: ScoreItem): ScoreItemView? {
        for (i in 0 until childCount) {
            (getChildAt(i) as? ScoreItemView)?.let { singleTree ->
                val result = singleTree.findTargetRecursive(target)
                if (result != null) return result
            }
        }
        Log.e("ScoreTreeLayout", "未找到目标节点：${target.sortStr}")
        return null
    }

    /**
     * 递归更新父节点分数
     */
    private fun updateParentScoreRecursive(parentItem: ScoreItem?) {
        parentItem ?: return
        // 汇总子节点分数
        val totalScore = parentItem.childScores.sumOf { it.score }
        parentItem.score = totalScore
        Log.d("ScoreTreeLayout", "更新父节点${parentItem.sortStr}分数：$totalScore")
        // 刷新父节点 + 递归更新祖父节点
        findTargetTree(parentItem)?.refreshScore()
        updateParentScoreRecursive(parentItem.parentItem)
    }

    /**
     * 数据的isBottom赋值逻辑
     * 1. 计算最大层级（isLevel=true的节点自身算层级，其子节点不计入）
     * 2. 按分支判定父节点isBottom：只要父节点下有一个子节点是isLevel=true，父节点isBottom=false；否则=true
     */
    private fun processScoreItemBottomFlag(rootList: MutableList<ScoreItem>) {
        val nodeLevelMap = mutableMapOf<ScoreItem, Int>()
        var maxLevel = 0

        // 递归遍历计算层级
        fun traverseNode(node: ScoreItem, currentLevel: Int) {
            nodeLevelMap[node] = currentLevel
            if (currentLevel > maxLevel) maxLevel = currentLevel
            // 子节点规则：isLevel=true → 子节点不计入层级（不递归）
            if (!node.isChildLevel) {
                node.childScores.forEach { child ->
                    traverseNode(child, currentLevel + 1)
                }
            }
        }
        // 初始化遍历根节点（层级=1）
        rootList.forEach { traverseNode(it, 1) }

        //筛选所有最大层级的节点（去重）
        val maxLevelNodes = nodeLevelMap.filter { it.value == maxLevel }.keys.toList()
        if (maxLevelNodes.isEmpty()) return

        // 按「父节点」分组处理
        val parentNodeGroups = maxLevelNodes.groupBy { it.parentItem }

        parentNodeGroups.forEach { (parentNode, childNodes) ->
            parentNode ?: return@forEach
            // 规则：父节点下只要有一个子节点是isLevel=true → isBottom=false；否则=true
            val hasIsLevelTrueChild = childNodes.any { it.isChildLevel }
            parentNode.isChildBottom = !hasIsLevelTrueChild
        }
    }

    /**
     * 创建单层数据的横向RecyclerView
     */
    private fun createResultRecyclerView(dataList: MutableList<ResultStandardItem.ResultChildItem>) {
        val singleLevelRv = RecyclerView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        childResultAdapter=initResultRecyclerView(singleLevelRv,dataList)
        addView(singleLevelRv)
    }


    /**
     * 创建单层数据的横向RecyclerView
     */
    private fun createSingleLevelRecyclerView(dataList: MutableList<ScoreItem>) {
        val params=LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        params.marginStart=40
        params.marginEnd=40
        val singleLevelRv = RecyclerView(context).apply {
            layoutParams = params
        }
        childNodeAdapter=initSingleRecyclerView(singleLevelRv,dataList)
        addView(singleLevelRv)
    }


    inner class ScoreItemView(context: Context) : ViewGroup(context) {
        var data: ScoreItem? = null
        private var itemView: View? = null
        private var rvListLevel: RecyclerView? = null
        private var rvListBottom: RecyclerView? = null
        private var ivResult: ImageView? = null
        private var tvScore: TextView? = null
        private var childNodeAdapter: ChildNodeAdapter? = null

        fun bindData(item: ScoreItem) {
            data = item
            removeAllViews()

            createItemView()

            data?.let { scoreItem ->
                if (scoreItem.childScores.isEmpty()) return

                if (scoreItem.isChildLevel) {
                    childNodeAdapter=initHorizontalRecyclerView(rvListLevel!!,scoreItem.childScores) // 横排
                }
                else if (scoreItem.isChildBottom){
                    childNodeAdapter=initHorizontalRecyclerView(rvListBottom!!,scoreItem.childScores) // 横排
                }
                else {
                    // 竖排：递归创建子节点
                    scoreItem.childScores.forEach { child ->
                        val childTree = ScoreItemView(context)
                        childTree.bindData(child)
                        addView(childTree, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
                    }
                }
            }
        }

        /**
         * 刷新当前节点（包含子节点）
         */
        fun refreshScore() {
            data?.let { scoreItem ->

                refreshScoreResult(tvScore,ivResult,scoreItem)

                // 刷新RecyclerView子节点
                if (scoreItem.isChildLevel ||scoreItem.isChildBottom) {
                    childNodeAdapter?.notifyDataSetChanged()
                    rvListLevel?.invalidate()
                    rvListBottom?.invalidate()
                }

                // 递归刷新子节点
                for (i in 0 until childCount) {
                    (getChildAt(i) as? ScoreItemView)?.refreshScore()
                }

                // 强制重绘
                invalidate()
                requestLayout()
            }
        }

        /**
         * 递归查找当前节点及其子节点
         */
        fun findTargetRecursive(target: ScoreItem): ScoreItemView? {
            if (data === target) return this
            for (i in 0 until childCount) {
                (getChildAt(i) as? ScoreItemView)?.let { childTree ->
                    val result = childTree.findTargetRecursive(target)
                    if (result != null) return result
                }
            }
            return null
        }

        /**
         * 统一创建ItemView
         */
        private fun createItemView() {
            data?.let { scoreItem ->
                itemView = LayoutInflater.from(context).inflate(R.layout.item_topic_score_vertical, this, false) ?: return

                val tvSort = itemView?.findViewById<TextView>(R.id.tv_sort)
                tvScore = itemView?.findViewById(R.id.tv_score)
                ivResult = itemView?.findViewById(R.id.iv_result)
                rvListLevel = itemView?.findViewById(R.id.rv_list_level)
                rvListBottom = itemView?.findViewById(R.id.rv_list_child)

                tvSort?.text = scoreItem.sortStr
                refreshScoreResult(tvScore,ivResult,scoreItem)
                rvListLevel?.visibility = if (scoreItem.isChildLevel) View.VISIBLE else View.GONE
                rvListBottom?.visibility=if (scoreItem.isChildBottom) View.VISIBLE else View.GONE

                if (isShowResult){
                    ivResult?.visibility = if (scoreItem.childScores.isNullOrEmpty()) View.VISIBLE else View.GONE
                }
                else{
                    ivResult?.visibility=View.GONE
                }

                bindItemClickListener(tvScore,ivResult,scoreItem)

                itemView?.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                addView(itemView)
            }
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            data ?: return
            itemView ?: return

            // 测量当前ItemView
            measureChild(itemView, widthMeasureSpec, heightMeasureSpec)
            var totalWidth = itemView!!.measuredWidth + paddingLeft + paddingRight
            var totalHeight = itemView!!.measuredHeight + paddingTop + paddingBottom

            // 测量子节点（仅竖排）
            if (!data!!.isChildLevel &&!data!!.isChildBottom) {
                for (i in 0 until childCount) {
                    val child = getChildAt(i)
                    if (child != itemView) {
                        measureChild(child, widthMeasureSpec, heightMeasureSpec)
                        totalHeight += child.measuredHeight
                        totalWidth = Math.max(totalWidth, child.measuredWidth + paddingLeft + paddingRight + levelPadding)
                    }
                }
            }

            setMeasuredDimension(
                resolveSize(totalWidth, widthMeasureSpec),
                resolveSize(totalHeight, heightMeasureSpec)
            )
        }

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            data ?: return
            itemView ?: return

            // 布局当前ItemView
            val itemLeft = paddingLeft
            val itemTop = paddingTop
            itemView?.layout(itemLeft, itemTop, itemLeft + itemView!!.measuredWidth, itemTop + itemView!!.measuredHeight)

            // 布局竖排子节点
            if (!data!!.isChildLevel) {
                var currentTop = itemTop + itemView!!.measuredHeight
                for (i in 0 until childCount) {
                    val child = getChildAt(i)
                    if (child != itemView) {
                        val childLeft = paddingLeft + levelPadding
                        child.layout(childLeft, currentTop, childLeft + child.measuredWidth, currentTop + child.measuredHeight)
                        currentTop += child.measuredHeight
                    }
                }
            }
        }

        override fun generateLayoutParams(attrs: AttributeSet?) = LayoutParams(context, attrs)
        override fun generateLayoutParams(p: LayoutParams) = LayoutParams(p)
        override fun generateDefaultLayoutParams() = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    /**
     * 统一绑定Item点击事件
     */
    private fun bindItemClickListener(tvScore:TextView?,ivResult: ImageView?,scoreItem: ScoreItem) {
        if (scoreItem.childScores.isNullOrEmpty()&&isShowResult) {
            tvScore?.setOnClickListener {
                mGlobalListener?.onScoreItemClick(ClickViewType.TV_SCORE, scoreItem)
            }
            ivResult?.setOnClickListener {
                mGlobalListener?.onScoreItemClick(ClickViewType.IV_RESULT, scoreItem)
            }
        } else {
            tvScore?.setOnClickListener(null)
            ivResult?.setOnClickListener(null)
        }
    }

    /**
     * 刷新分数以及结果显示图片
     */
    private fun refreshScoreResult(tvScore:TextView?,ivResult: ImageView?,scoreItem:ScoreItem){
        tvScore?.text = if (scoreItem.score==0.0) "0" else scoreItem.score.toString()
        ivResult?.setImageResource(if (scoreItem.result == 1) R.mipmap.icon_correct_right else R.mipmap.icon_correct_wrong)
    }

    /**
     * 初始化横排RecyclerView
     */
    private fun initResultRecyclerView(recyclerView: RecyclerView,list:MutableList<ResultStandardItem.ResultChildItem>): ChildResultStandardAdapter {
        recyclerView.layoutManager =GridLayoutManager(context,3)

        recyclerView.setHasFixedSize(true)
        recyclerView.isNestedScrollingEnabled = false

        val childNodeAdapter=ChildResultStandardAdapter(list)
        recyclerView.adapter = childNodeAdapter
        return childNodeAdapter
    }

    /**
     * 初始化单层横排RecyclerView
     */
    private fun initSingleRecyclerView(recyclerView: RecyclerView,list:MutableList<ScoreItem>): ChildNodeAdapter {

        recyclerView.layoutManager =GridLayoutManager(context,3)

        recyclerView.setHasFixedSize(true)
        recyclerView.isNestedScrollingEnabled = false

        val childNodeAdapter=ChildNodeAdapter(list)
        recyclerView.adapter = childNodeAdapter

        // 添加Item间距
        recyclerView.addItemDecoration(SpaceGridItemDeco1(3,40,20))
        return childNodeAdapter
    }

    /**
     * 初始化横排RecyclerView
     */
    private fun initHorizontalRecyclerView(recyclerView: RecyclerView,list:MutableList<ScoreItem>): ChildNodeAdapter {

        recyclerView.layoutManager = FlexboxLayoutManager(context).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP // 强制换行
            justifyContent = JustifyContent.FLEX_START // 左对齐
            alignItems = AlignItems.CENTER // 垂直居中，统一排版
            isAutoMeasureEnabled = false
        }

        recyclerView.setHasFixedSize(true)
        recyclerView.isNestedScrollingEnabled = false

        val childNodeAdapter=ChildNodeAdapter(list)
        recyclerView.adapter = childNodeAdapter

        // 添加Item间距
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.right = 30
            }
        })
        return childNodeAdapter
    }

    inner class ChildNodeAdapter(data: MutableList<ScoreItem>?) : BaseQuickAdapter<ScoreItem, BaseViewHolder>(R.layout.item_topic_score, data) {
        override fun convert(holder: BaseViewHolder, item: ScoreItem) {
            holder.setText(R.id.tv_sort, item.sortStr)
            val tvScore=holder.getView<TextView>(R.id.tv_score)
            val ivResult=holder.getView<ImageView>(R.id.iv_result)

            ivResult?.visibility = if (isShowResult) View.VISIBLE else View.GONE

            refreshScoreResult(tvScore,ivResult,item)
            bindItemClickListener(tvScore,ivResult,item)
        }
    }

    inner class ChildResultStandardAdapter(data: List<ResultStandardItem.ResultChildItem>?)
        : BaseQuickAdapter<ResultStandardItem.ResultChildItem, BaseViewHolder>(R.layout.item_homework_result_standard_child, data) {

        override fun convert(helper: BaseViewHolder, item: ResultStandardItem.ResultChildItem) {
            helper.setText(R.id.tv_score,item.sortStr)
            helper.setImageResource(R.id.iv_result,if (item.isCheck) R.mipmap.icon_correct_right else R.mipmap.icon_correct_wrong)
            val ivResult=helper.getView<ImageView>(R.id.iv_result)
            ivResult?.setOnClickListener {
                mResultListener?.onResultItemClick(helper.layoutPosition)
            }
        }

    }

}