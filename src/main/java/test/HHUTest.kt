package main.java.test

import main.java.parser.HHUParser

/**
 * 河海大学
 */
fun main() {
    val usr = "000000"
    val pwd = "000000"
    val parser = HHUParser(usr, pwd)
    parser.saveCourse()
}