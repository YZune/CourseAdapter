package main.java.test

import main.java.parser.HUNNUParser
import java.io.File

fun main(){
    val source = File("hunnu.htm").readText()
    HUNNUParser(source).apply {
        generateCourseList()
        saveCourse()
    }
}