package test

import parser.AHNUParser
import java.io.File

fun main() {
    val source = File("D:\\Work\\WakeUp\\安徽师范大学教务系统_files\\main.html")
        .readText()
    AHNUParser(source).apply {
        generateCourseList()
        saveCourse()
    }
}