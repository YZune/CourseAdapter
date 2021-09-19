package main.java.test

import main.java.parser.NYISTParser
import java.io.File

fun main() {
    val file = File("D:/project/NYISTOSUG/CourseWeb/FR/我的课表.html")
    val parser = NYISTParser(file.readText())
    parser.saveCourse()
}