package test

import parser.CSVParser
import java.io.File
import java.nio.charset.Charset

fun main() {
    val file = File("/Users/yzune/Downloads/123456(1).csv")
    CSVParser(file.readText(Charset.forName("utf-8"))).apply {
        saveCourse()
    }
}