package com.bll.lnkcommon.utils

object ChineseNumberConverter {
    private val BASIC_CHINESE: Array<String> =
        arrayOf("零", "一", "二", "三", "四", "五", "六", "七", "八", "九")
    private val STANDARD_TEN_TO_TWENTY: Array<String> = arrayOf(
        "十", "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十"
    )

    private val CIRCLE_NUM_MAP = mapOf(
        1 to "①", 2 to "②", 3 to "③", 4 to "④", 5 to "⑤",
        6 to "⑥", 7 to "⑦", 8 to "⑧", 9 to "⑨", 10 to "⑩",
        11 to "⑪", 12 to "⑫", 13 to "⑬", 14 to "⑭", 15 to "⑮",
        16 to "⑯", 17 to "⑰", 18 to "⑱", 19 to "⑲", 20 to "⑳"
    )

    /**
     * 阿拉伯数字转混合规则中文数字（核心方法）
     * @param number 待转换阿拉伯数字（≥0，支持0-999，可扩展）
     * @return 符合规则的中文数字，非法数字返回提示信息
     */
    fun toMixedChineseNumber(number: Int): String {
        if (number < 0) {
            return "不支持负数转换"
        }
        if (number == 0) {
            return BASIC_CHINESE[0]
        }
        if (number in 1..9) {
            return BASIC_CHINESE[number]
        }
        if (number in 10..20) {
            return STANDARD_TEN_TO_TWENTY[number - 10]
        }
        return convertToSplicedChinese(number)
    }

    /**
     * 处理 21 及以上数字，返回简洁拼接中文
     * @param number 待转换数字（≥21）
     * @return 拼接结果（如25→二五、109→一零九、999→九九九）
     */
    private fun convertToSplicedChinese(number: Int): String {
        val splicedSb = StringBuilder()
        val numberStr = number.toString()
        for (element in numberStr) {
            val digit = element.code - '0'.code
            splicedSb.append(BASIC_CHINESE[digit])
        }
        return splicedSb.toString()
    }

    /**
     * 单个数字转带圈序号
     * @param number 待转换数字（1-20）
     * @return 带圈序号，超出范围返回原数字的字符串形式
     */
    fun toCircleNumber(number: Int): String {
        return CIRCLE_NUM_MAP[number] ?: number.toString()
    }

}