package test

import parser.qz.QzCrazyParser
import java.io.File

fun main() {
    val source = File("/Users/yzune/Downloads/新建文本文档 (2).html")
        .readText()
    QzCrazyParser(source).apply {
        saveCourse()
    }
}