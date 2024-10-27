package main.java.test

import main.java.parser.SHUParser2024
import java.io.File


fun main() {
    val file = File("D:\\r.html")
    val parser = SHUParser2024(file.readText())
    parser.saveCourse()
}
