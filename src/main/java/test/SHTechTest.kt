package main.java.test

import main.java.parser.SHTechParser
import java.io.File


fun main() {

    val file = File("./inputsist.html")
    val parser = SHTechParser(file.readText())
    parser.saveCourse()
}
