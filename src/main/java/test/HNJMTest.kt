package test

import parser.HNJMParser
import java.io.File

fun main() {
    val source = File("/Users/fanyuyang/Desktop/wakeup/test_page/班级课表查询-智慧经贸.html")
        .readText()
    HNJMParser(source).saveCourse()
}