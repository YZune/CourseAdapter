package test

import parser.qz.QzCrazyParser
import parser.qz.QzWithNodeParser
import java.io.File

fun main() {
    val source = File("/Users/yzune/Downloads/新建文本文档.txt")
        .readText()
    QzWithNodeParser(source).apply {
        saveCourse()
    }
    QzCrazyParser(source).apply {
        saveCourse()
    }
//    QzParser(source).apply {
//        saveCourse()
//    }
}