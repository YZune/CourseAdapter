package main.java.test

import parser.AHNUParser
import parser.TCUParser
import java.io.File

fun main() {
    val source = File("C:\\Users\\suki\\Desktop\\data.html")
        .readText(Charsets.UTF_8)
    TCUParser(source).apply {
        generateCourseList()
        saveCourse()
    }
}