package test

import parser.HNUSTParser
import java.io.File

fun main() {
    val source = File("/Users/yzune/YZune_Git/database/python/html/湖南科技大学.html").readText()
    HNUSTParser(source, 0).apply {
        generateCourseList()
        saveCourse()
    }
}