package main.java.test

import main.java.parser.JNUParser
import java.io.File

fun main() {
    val file = File("C:/Users/Echo/Desktop/Reserved.ReportViewerWebControl(10).html")
    val parser = JNUParser(file.readText(charset("gbk")))
    parser.saveCourse()
}