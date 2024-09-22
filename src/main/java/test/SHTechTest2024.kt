package main.java.test

import main.java.parser.SHTechParser2024
import java.io.File


fun main() {

    val file = File("Replace with you local html file")
    val parser = SHTechParser2024(file.readText())
    parser.saveCourse()
}
