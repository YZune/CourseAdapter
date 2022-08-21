package main.java.test

import main.java.parser.SHTechParser
import java.io.File


fun main() {

    val file = File("./inputSlst.html")
    val str = file.readText()
    //println(str)
    val parser = SHTechParser(str)
    parser.saveCourse()
}
