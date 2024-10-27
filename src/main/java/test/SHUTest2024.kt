package main.java.test

import main.java.parser.SHUParser2024
import java.io.File


fun main() {
    val file = File("your raw html file here")
    val parser = SHUParser2024(file.readText())
    parser.saveCourse()
}
