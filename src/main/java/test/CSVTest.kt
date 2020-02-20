package test

import parser.CSVParser
import java.io.File
import java.nio.charset.Charset

fun main() {
    val file = File("/Users/yzune/Downloads/万水千山7277.csv")
    CSVParser(file.readText(Charset.forName("gbk"))).apply {
        saveCourse()
    }
}