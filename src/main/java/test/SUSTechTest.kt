package main.java.test

import main.java.parser.SUSTechParser

fun main() {
    val parser = SUSTechParser("00000000", "password", "2020", "2")
    parser.saveCourse()
}
