package com.bll.lnkcommon.utils

import com.bll.lnkcommon.mvp.model.teaching.ScoreItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.regex.Pattern

object ScoreItemUtils {

    /**
     * 格式序列化  题目分数转行list集合
     */
    fun questionToList(json: String,correctModule: Int): MutableList<ScoreItem> {
        if (json.isEmpty()){
            return mutableListOf()
        }
        val list=Gson().fromJson(json, object : TypeToken<MutableList<ScoreItem>>() {}.type) as MutableList<ScoreItem>
        for (item in list){
            item.sort=list.indexOf(item)
            item.childScores?.forEach {
                it.sort=item.childScores.indexOf(it)
            }
        }
        if (correctModule==4||correctModule==7){
            var sort=0
            for (item in list){
                item.childScores?.forEach {
                    it.sort=sort
                    sort+=1
                }
            }
        }

        setInitListScore(list)

        return jsonListToModuleList(list, correctModule)
    }

    /**
     * 题目分数多级树列表转成模板级数
     */
    private fun jsonListToModuleList(list: List<ScoreItem>,correctModule: Int): MutableList<ScoreItem> {
        val items = mutableListOf<ScoreItem>()
        when (correctModule) {
            1,2 -> {
                for (item in list) {
                    item.sortStr=if (correctModule==1)ToolUtils.numbers[item.sort+1] else "${item.sort+1}"
                    items.add(item)
                }
            }
            3, 4 -> {
                for (item in list) {
                    item.sortStr=ToolUtils.numbers[item.sort+1]
                    item.childScores.forEach {
                        it.sortStr=" ${it.sort+1}"
                    }
                    items.add(item)
                }
            }
            5 -> {
                for (item in list) {
                    item.sortStr="${item.sort+1}"
                    //处理当前级数据如果有第3级则显示sortStr
                    if (isListExistChildItem(item.childScores)){
                        val childItems = mutableListOf<ScoreItem>()
                        item.childScores.forEach {
                            if (it.childScores.isNullOrEmpty()){
                                it.sortStr=" (${it.sort+1})"
                                childItems.add(it)
                            }
                            else{
                                //超过两级的去掉父节点拿到所有子节点
                                childItems.addAll(getRecursionChildItems(correctModule,it.childScores, it))
                            }
                        }
                        item.childScores=childItems
                    }
                    items.add(item)
                }
            }
            6,7->{
                for (item in list) {
                    item.sortStr=ToolUtils.numbers[item.sort+1]
                    val parentItems = mutableListOf<ScoreItem>()
                    for (parentItem in item.childScores) {
                        parentItem.sortStr=" ${parentItem.sort+1}"
                        //处理当前级数据如果有第4级则显示sortStr
                        if (isListExistChildItem(parentItem.childScores)){
                            val childItems = mutableListOf<ScoreItem>()
                            parentItem.childScores.forEach { childItem->
                                if (childItem.childScores.isNullOrEmpty()) {
                                    childItem.sortStr=" (${childItem.sort+1})"
                                    childItems.add(childItem)
                                } else {
                                    childItems.addAll(getRecursionChildItems(correctModule,childItem.childScores, childItem))
                                }
                            }
                            parentItem.childScores=childItems
                        }
                        parentItems.add(parentItem)
                    }
                    item.childScores=parentItems
                    items.add(item)
                }
            }
        }
        return items
    }
    /**
     * 给数据节点赋分、以及统计对错
     */
    private fun setInitListScore(list:List<ScoreItem>){
        list.forEach { item ->
            // 1. 处理子节点（递归：先确保子节点的result已计算，避免父节点依赖未初始化的子节点数据）
            if (!item.childScores.isNullOrEmpty()) {
                setInitListScore(item.childScores) // 递归处理子节点，保证子节点result/score/label已就绪
            }
            // 2. 处理当前节点的result（子节点/父节点通用）
            item.result = getItemScoreResult(item)
            // 3. 父节点逻辑：仅当score/label为空时，才通过子节点汇总赋值（核心优化点）
            if (item.childScores.isNullOrEmpty()) {
                return@forEach
            }
            // 父节点：判断score/label是否为空
            if (item.score == 0.0) {
                item.score = calculateParentTotalScore(item.childScores)
            }
            if (item.label == 0.0) {
                item.label = calculateParentTotalLabel(item.childScores)
            }
        }
    }

    /**
     * 计算父节点总score（子节点score汇总）
     */
    private fun calculateParentTotalScore(childList: MutableList<ScoreItem>): Double {
        return childList.sumOf { child ->
            child.score
        }
    }

    /**
     * 轮询赋值父节点总label
     */
    private fun calculateParentTotalLabel(childList: MutableList<ScoreItem>): Double {
        return childList.sumOf { child ->
            child.label
        }
    }

    /**
     * 判断当前层级是否有子集
     */
    private fun isListExistChildItem(list: MutableList<ScoreItem>):Boolean{
        var isShowSortStr=false
        for (childItem in list){
            if (!childItem.childScores.isNullOrEmpty()){
                isShowSortStr=true
            }
        }
        return isShowSortStr
    }

    /**
     * 递归拿到所有子节点，同时给超出子节点的第一个sortStr赋值
     */
    private fun getRecursionChildItems(correctModule: Int, list: MutableList<ScoreItem>, parentItem: ScoreItem,isShowTag: Boolean=true): MutableList<ScoreItem> {
        val items = mutableListOf<ScoreItem>()
        for (item in list) {
            item.sortStr=if (list.indexOf(item)==0&&isShowTag) "(${parentItem.sort+1})" else " "
            if (item.childScores.isNullOrEmpty()) {
                items.add(item)
            } else {
                items.addAll(getRecursionChildItems(correctModule,item.childScores, item,false))
            }
        }
        return items
    }

    /**
     * 获取小题结果
     */
    private fun getItemScoreResult(item:ScoreItem):Int{
        //当对错时 返回result对错
        if (item.label==0.0)
        {
            return item.result
        }
        return if (item.score<item.label) 0 else 1
    }
}