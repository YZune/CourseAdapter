package main.java.test

import parser.wakeup.WakeupParser
import java.io.File

fun main() {
    val file = File("D:\\gxic.html")
    val parser = WakeupParser(file.readText())
    val courseList = parser.generateCourseList()
    courseList.forEach { course ->
        println(course)
    }

    parser.saveCourse()
}