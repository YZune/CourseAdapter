package test

import main.java.parser.ECNUParser
import parser.AHNUParser
import java.io.File

fun main() {
    val source = File("/Users/user/Desktop/test.html")
        .readText()
    ECNUParser(source).apply {
        generateCourseList()
        saveCourse()
    }
}