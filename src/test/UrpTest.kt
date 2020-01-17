package test

import parser.UrpParser
import java.io.File

fun main() {
    val file = File("/Users/yzune/YZune_Git/database/python/schools/渤海大学/38477.渤海大学.html")
    UrpParser(file.readText()).apply {
        generateCourseList()
        saveCourse()
    }
}