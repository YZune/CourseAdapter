package main.java.test

import main.java.parser.HUSTParser
import java.io.File

fun main() {
    val file = File("E:\\Temple\\hust.course.html")
    val parser = HUSTParser(file.readText())
    parser.saveCourse()

}