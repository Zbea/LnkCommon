package com.bll.lnkcommon.utils

import com.bll.lnkcommon.mvp.model.teaching.ScoreItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ScoreItemUtils {

    /**
     * 格式序列化  题目分数转行list集合
     */
    fun questionToList(json: String,correctModule: Int): MutableList<ScoreItem> {
        if (json.isEmpty()){
            return mutableListOf()
        }
        val list: MutableList<ScoreItem> = try {
            Gson().fromJson(json, object : TypeToken<MutableList<ScoreItem>>() {}.type) as MutableList<ScoreItem>
        } catch (e:Exception){
            mutableListOf()
        }

        setInitListScore(list)
        jsonListToModuleList(list, correctModule)

//        //定义排除策略：忽略 ScoreItem 类的 parentItem 字段
//        val exclusionStrategy: ExclusionStrategy = object : ExclusionStrategy {
//            override fun shouldSkipField(f: FieldAttributes): Boolean {
//                // 条件：字段名是 "parentItem" 且所属类是 ScoreItem
//                return "parentItem" == f.name && ScoreItem::class.java == f.declaringClass
//            }
//
//            override fun shouldSkipClass(clazz: Class<*>?): Boolean {
//                return false // 不忽略任何类
//            }
//        }
//        val gson = GsonBuilder()
//            .setExclusionStrategies(exclusionStrategy) // 应用自定义排除规则
//            .setPrettyPrinting() // 格式化输出（可选）
//            .create()
//        Log.d(Constants.DEBUG,gson.toJson(list[0]))

        return list
    }

    /**
     * 题目分数多级树列表转成模板级数
     */
    private fun jsonListToModuleList(list: List<ScoreItem>,correctModule: Int){
        when (correctModule) {
            1,2 -> {
                list.forEachIndexed { index, scoreItem ->
                    val sort=index+1
                    scoreItem.sortStr=if (correctModule==1) ChineseNumberConverter.toMixedChineseNumber(sort)  else "$sort"
                }
            }
            5->{
                list.forEachIndexed { index, scoreItem ->
                    scoreItem.sortStr="${index+1}"
                    if (hasChild(scoreItem)){
                        scoreItem.childScores.forEachIndexed { index1, scoreItem1 ->
                            scoreItem1.parentItem=scoreItem
                            if (!scoreItem.isChildLevel)
                                scoreItem1.sortStr="(${index1+1})"

                            if (hasChild(scoreItem1)) {
                                scoreItem1.childScores.forEachIndexed { index2, scoreItem2 ->
                                    scoreItem2.parentItem=scoreItem1
                                    if (!scoreItem1.isChildLevel)
                                        scoreItem2.sortStr= ChineseNumberConverter.toCircleNumber(index2+1)
                                }
                            }
                        }
                    }
                }
            }
            3,6->{
                list.forEachIndexed { rootIndex, rootItem ->
                    rootItem.sortStr= ChineseNumberConverter.toMixedChineseNumber(rootIndex+1)
                    if (hasChild(rootItem)){
                        rootItem.childScores.forEachIndexed { index, scoreItem ->
                            scoreItem.parentItem=rootItem
                            if (!rootItem.isChildLevel)
                                scoreItem.sortStr="${index+1}"

                            if (hasChild(scoreItem)){
                                scoreItem.childScores.forEachIndexed { index1, scoreItem1 ->
                                    scoreItem1.parentItem=scoreItem
                                    if (!scoreItem.isChildLevel)
                                        scoreItem1.sortStr="(${index1+1})"

                                    if (hasChild(scoreItem1)) {
                                        scoreItem1.childScores.forEachIndexed { index2, scoreItem2 ->
                                            scoreItem2.parentItem=scoreItem1
                                            if (!scoreItem1.isChildLevel)
                                                scoreItem2.sortStr= ChineseNumberConverter.toCircleNumber(index2+1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            4,7->{
                var baseIndex=0
                list.forEachIndexed { rootIndex, rootItem ->
                    rootItem.sortStr= ChineseNumberConverter.toMixedChineseNumber(rootIndex+1)
                    if (hasChild(rootItem)){
                        rootItem.childScores.forEachIndexed { index, scoreItem ->
                            scoreItem.parentItem=rootItem
                            if (rootItem.isChildLevel)
                                scoreItem.sortStr="${baseIndex+index+1}"

                            if (hasChild(scoreItem)){
                                scoreItem.childScores.forEachIndexed { index1, scoreItem1 ->
                                    scoreItem1.parentItem=scoreItem
                                    if (scoreItem.isChildLevel)
                                        scoreItem1.sortStr="(${index1+1})"

                                    if (hasChild(scoreItem1)) {
                                        scoreItem1.childScores.forEachIndexed { index2, scoreItem2 ->
                                            scoreItem2.parentItem=scoreItem1
                                            if (scoreItem1.isChildLevel)
                                                scoreItem2.sortStr= ChineseNumberConverter.toCircleNumber(index2+1)
                                        }
                                    }
                                }
                            }
                        }
                        baseIndex+=rootItem.childScores.size
                    }
                }
            }
        }
    }

    private fun hasChild(item:ScoreItem):Boolean{
        return !item.childScores.isNullOrEmpty()
    }

    /**
     * 给数据节点赋分、以及统计对错
     */
    private fun setInitListScore(list:List<ScoreItem>){
        list.forEach { item ->
            // 处理子节点（递归：先确保子节点的result已计算，避免父节点依赖未初始化的子节点数据）
            if (!item.childScores.isNullOrEmpty()) {
                setInitListScore(item.childScores) // 递归处理子节点，保证子节点result/score/label已就绪
            }
            //处理当前节点的result（子节点/父节点通用）
            item.result = getItemScoreResult(item)
            //父节点逻辑：仅当score/label为空时，才通过子节点汇总赋值
            if (item.childScores.isNullOrEmpty()) {
                return@forEach
            }
            // 父节点：判断score/label是否为空
            if (item.score == 0.0) {
                item.score = getItemScoreTotal(item.childScores)
            }
            if (item.label == 0.0) {
                item.label = getItemLabelTotal(item.childScores)
            }
        }
    }

    /**
     * 计算父节点总score（子节点score汇总）
     */
    fun getItemScoreTotal(list: MutableList<ScoreItem>): Double {
        return list.sumOf { it.score }
    }

    /**
     * 轮询赋值父节点总label
     */
    private fun getItemLabelTotal(list: MutableList<ScoreItem>): Double {
        return list.sumOf { it.label }
    }

    /**
     * 获取小题结果
     */
    fun getItemScoreResult(item:ScoreItem):Int{
        //当对错时 返回result对错
        if (item.label==0.0)
        {
            return item.result
        }
        return if (item.score<item.label) 0 else 1
    }
}