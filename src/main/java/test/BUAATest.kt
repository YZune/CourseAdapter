package main.java.test

import main.java.parser.BUAAParser
import java.io.File

fun main() {
    val source = File("C:/Users/R7000P/OneDrive/桌面/课表.html")
        .readText()
    BUAAParser(source).apply {
        generateCourseList()
        saveCourse()
        println(generateTimeTable())
    }
}