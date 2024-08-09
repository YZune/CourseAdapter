package main.java.test

import main.java.parser.HFUTParser
import parser.ZhengFangParser
import java.io.File

fun main() {
    val file = File("D:\\datum.json")
    val parser = HFUTParser(file.readText())
    parser.saveCourse()
}