package main.java.test

import main.java.parser.ECUPLParser
import java.io.File

fun main() {
    val source = File("../ecupl.html").readText()
    ECUPLParser(source).apply {
        generateCourseList().forEach { println(it) }
        saveCourse()
    }
}
