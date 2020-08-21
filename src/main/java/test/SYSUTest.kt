package test

import parser.SYSUParser
import java.io.File

fun main() {
    val file = File("/path/to/file/1.html")
    val parser = SYSUParser(file.readText())
    parser.saveCourse()
}