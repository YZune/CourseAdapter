package test

import parser.BNUZParser
import java.io.File

fun main() {
    val source = File("/Users/yzune/YZune_Git/database/python/schools/北京师范大学珠海分校/34821.北京师范大学珠海分校.html")
        .readText()
    BNUZParser(source).apply {
        generateCourseList()
        saveCourse()
    }
}