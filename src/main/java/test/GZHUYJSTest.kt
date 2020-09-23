package main.java.test

import main.java.parser.GZHUYJSParser
import java.io.File

fun main() {
    val source = File("D:\\work\\cou.txt")
        .readText()
    GZHUYJSParser(source).apply {
        generateCourseList()
        saveCourse()
    }
}