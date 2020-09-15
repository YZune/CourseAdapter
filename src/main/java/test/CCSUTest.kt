package main.java.test

import parser.CSXYParser
import java.io.File

fun main() {

    val file = File("C:\\Users\\mingz\\Desktop\\2019-2020-2.html")
    val parser = CSXYParser(file.readText())
    parser.saveCourse()
}