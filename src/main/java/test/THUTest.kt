package main.java.test

import parser.THUParser
import java.io.File

fun main() {
    val source = File("../THU/本科生选课系统_mod.html").readText()
    THUParser(source).apply {
        generateCourseList().forEach { println(it) }
        saveCourse()
        println(generateTimeTable())
    }
}
