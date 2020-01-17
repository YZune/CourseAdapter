package test

import parser.qz.QzWithNodeParser
import java.io.File

fun main() {
    val source = File("/Users/yzune/YZune_Git/database/python/schools/广东外语外贸大学/38323.广东外语外贸大学.html")
        .readText()
    QzWithNodeParser(source).apply {
        generateCourseList()
        saveCourse()
    }
}