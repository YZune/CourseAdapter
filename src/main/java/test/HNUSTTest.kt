package test

import parser.HNUSTParser
import java.io.File

fun main() {
    val source = File("/Users/yzune/Downloads/新建文本文档 (2).html").readText()
    HNUSTParser(source, 0).apply {
        saveCourse()
    }
}