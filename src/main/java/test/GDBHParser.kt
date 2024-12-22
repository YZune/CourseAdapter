package main.java.test

import main.java.parser.GDBHParser
import java.io.File

fun main() {
    val file = File("D:/2322/test.json")
    val cookie = "ASP.NET_SessionId=*********; .ASPXAUTH=*******"
    GDBHParser(file.readText(),cookie).apply {
    generateCourseList()
    saveCourse()
    }
}