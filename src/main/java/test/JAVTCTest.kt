package main.java.test

import main.java.parser.JAVTCParser
import java.io.File

fun main() {
    val file = File("D:\\我的文件\\大专自学\\课表.html")
    val parser = JAVTCParser()
    parser.saveCourse()
}