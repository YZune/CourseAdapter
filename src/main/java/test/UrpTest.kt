package test

import parser.UrpParser
import java.io.File

fun main() {
    val file = File("/Users/yzune/Downloads/天津商业大学.html")
    UrpParser(file.readText()).apply {
        generateCourseList()
        saveCourse()
    }
}