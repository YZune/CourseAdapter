package test

import parser.HLJUParser
import java.io.File

fun main() {
    val source = File("F:\\1.html").readText()
    HLJUParser(source).apply {
        generateCourseList()
        saveCourse()
    }
}