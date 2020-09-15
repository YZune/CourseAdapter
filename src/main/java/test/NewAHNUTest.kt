package test

import parser.NewAHNUParser
import java.io.File

fun main() {
    val source = File("D:\\Work\\WakeUp\\课表.html")
        .readText()
    NewAHNUParser(source).apply {
        generateCourseList()
        saveCourse()
    }
}