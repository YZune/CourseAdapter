package main.java.test

import main.java.parser.JLUParser
import java.io.File

fun main() {
    val file = File("D:/temp/dan.txt")
    JLUParser(file.readText()).apply {
        generateCourseList()
        saveCourse()
    }
}