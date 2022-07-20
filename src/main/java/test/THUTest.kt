package main.java.test

import parser.THUParser
import java.io.File
import java.nio.charset.Charset

fun main() {
    val source = File("../本科生选课系统.html").readText(Charset.forName("gb2312"))
    THUParser(source).apply {
        generateCourseList().forEach { println(it) }
        saveCourse()
        println(generateTimeTable())
    }
}
