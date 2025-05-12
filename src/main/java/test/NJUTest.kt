package main.java.test

import main.java.parser.NJUParser
import java.io.File

fun main() {
    val path = "/Users/aritxonly/Downloads/1.html"
    val file = File(path)
    println("open file ${file.absolutePath}")
    val parser = NJUParser(file.readText())
    parser.saveCourse()
}