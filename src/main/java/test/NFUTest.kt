package main.java.test

import main.java.parser.NFUParser
import java.io.File


    fun main() {
        val path = "C:\\Users\\bqliang\\Desktop\\parseTest\\数字校园 中山大学南方学院.html"
        val file = File(path)
        NFUParser(file.readText()).saveCourse()

    }