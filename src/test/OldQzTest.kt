package test

import parser.OldQzParser
import java.io.File

fun main() {
    val source = File("/Users/yzune/YZune_Git/database/python/html/湖南工学院.html").readText()
    OldQzParser(source).apply {
        generateCourseList()
        saveCourse()
    }
}