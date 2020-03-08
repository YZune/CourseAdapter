package main.java.test

import main.java.parser.JNUParser
import java.io.File

fun main() {
    val file = File("/Users/yzune/Downloads/暨南大学.html")
    val parser = JNUParser(file.readText())
    parser.saveCourse()
}