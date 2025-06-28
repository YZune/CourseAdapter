package main.java.test

import main.java.parser.LNGDParser
import java.io.File

fun main() {
    val source = File("/Users/guozongyu/CourseAdapter/src/main/java/test/课表查询.html").readText()
    LNGDParser(source).apply {
        generateCourseList()
        saveCourse()
    }
}

