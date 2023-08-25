package main.java.test

import main.java.parser.JLUParser
import java.io.File

fun main() {
    val file = File("/home/lifly/temp/test.json")
    JLUParser(file.readText()).apply {
        generateCourseList()
        saveCourse()
    }
}