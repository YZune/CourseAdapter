package test

import parser.ECJTUParser
import java.io.File

fun main() {
    val file = File("/Users/Precious/Downloads/ECJTU.html")
    ECJTUParser(file.readText()).apply {
        generateCourseList()
        saveCourse()
    }
}