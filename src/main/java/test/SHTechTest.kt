package main.java.test

import main.java.parser.SHTechParser
import java.io.File


fun main() {

    val file = File("/home/trace/Documents/cour.html")
    val parser = SHTechParser(file.readText())
    parser.saveCourse()

}
