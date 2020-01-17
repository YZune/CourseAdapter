package test

import parser.HNIUParser
import java.io.File

fun main() {
    val source = File("/Users/yzune/YZune_Git/database/python/schools/湖南信息职业技术学院/33930.湖南信息职业技术学院.html")
        .readText()
    HNIUParser(source).apply {
        generateCourseList()
        saveCourse()
    }
}