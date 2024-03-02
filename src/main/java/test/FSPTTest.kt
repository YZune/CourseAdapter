package main.java.test

import main.java.parser.FSPTParser
import parser.ZhengFangParser
import parser.qz.QzBrParser
import parser.qz.QzCrazyParser
import parser.qz.QzParser
import parser.qz.QzWithNodeParser
import java.io.File

fun main() {
    val file = File("./fspt.html")
    val parser = FSPTParser(file.readText())
    parser.saveCourse()
}