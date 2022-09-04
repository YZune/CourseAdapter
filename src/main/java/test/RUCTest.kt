package main.java.test

import main.java.parser.RUCParser

import java.io.File


fun main() {
    val file = File("D:\\Users\\woshi\\Downloads\\RUCParser\\test.html")
    println(file.readText(charset("GBK")))
    val parser = RUCParser(file.readText(charset("GBK")))
    parser.saveCourse()
    println(parser.generateTimeTable())
}