package main.java.test

import main.java.parser.JXAUParser
import java.io.File

fun main() {
    val file = File("/home/mrwoowoo/文档/classtable.html")
    var parser = JXAUParser(file.readText())
    parser.saveCourse()
}