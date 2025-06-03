package main.java.test

import main.java.parser.YZZYParser 
import java.io.File
import java.nio.file.Paths

fun main(){
    val path = Paths.get("").toAbsolutePath().toString()
    println(path)
    val file = File("d:\\CourseAdapter\\yongzhou.html")
    val parser = YZZYParser(file.readText()) 
    parser.saveCourse()
}