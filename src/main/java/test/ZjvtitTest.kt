package main.java.test

import Common
import main.java.parser.ZjvtitParser
import java.io.File

fun main() {
    val file = File("浙交院.html")
    val parser = ZjvtitParser(file.readText())
    parser.saveCourse()
}
