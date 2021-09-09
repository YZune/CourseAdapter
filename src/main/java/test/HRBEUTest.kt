package main.java.test

import main.java.parser.HrbeuGraduateParser
import java.io.File

fun main() {
    val file = File("C:\\Users\\Galaxy\\Desktop\\new.html")
    val parser = HrbeuGraduateParser(file.readText())
    parser.saveCourse()
}
