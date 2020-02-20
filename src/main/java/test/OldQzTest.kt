package test

import parser.OldQzParser
import java.io.File

fun main() {
    val source = File("/Users/yzune/Downloads/新建文本文档 (2).html").readText()
    OldQzParser(source).apply {
        saveCourse()
    }
}