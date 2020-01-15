package test

import parser.NewUrpParser
import java.io.File

fun main() {
    val file = File("/Users/yzune/Downloads/NewUrpJson.json")
    NewUrpParser(file.readText()).apply {
        generateCourseList()
        saveCourse()
    }
}