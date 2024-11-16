package main.java.test

import main.java.parser.CTGUParser
import java.io.File

fun main() {
    val file = File("C:/Ddata/test/test_files/home.html")
    val parser = CTGUParser(file.readText())
    parser.saveCourse()
}