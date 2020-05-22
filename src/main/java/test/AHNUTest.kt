package test

import parser.AHNUParser
import java.io.File

fun main() {
    val source = File("D:\\Work\\WakeUp\\test2_files\\main.html")
        .readText()
    AHNUParser(source).apply {
        generateCourseList()
        saveCourse()
    }
}