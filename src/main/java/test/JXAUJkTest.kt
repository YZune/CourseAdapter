package main.java.test

import main.java.parser.JXAUJkParser

fun main() {
    val username = ""
    val password = ""
    val source = "$username,$password"
    val parser = JXAUJkParser(source)
    parser.saveCourse()
}