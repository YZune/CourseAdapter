package main.java.test

import main.java.parser.YGUParser

fun main(args: Array<String>) {
    val cookie = "90********39"
    val ygu = YGUParser(cookie, "202301")
    ygu.saveCourse(true)
}
