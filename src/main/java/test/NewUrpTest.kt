package test

import parser.NewUrpParser
import java.io.File

fun main() {
    arrayOf(
        "上海海洋大学", "山西工程技术学院", "中国农业大学", "安徽财经大学",
        "四川大学", "中国石油大学（北京）", "内蒙古民族大学", "天津科技大学",
        "内蒙古科技大学", "河北工程大学", "山西农业大学", "内蒙古大学",
        "天津职业技术师范大学", "河北工程大学", "河北工业大学", "山西农业大学信息学院",
        "齐鲁师范学院"
    )
    val file = File("/Users/yzune/Downloads/NewUrpJson2.json")
    NewUrpParser(file.readText()).apply {
        generateCourseList()
        saveCourse()
    }
}