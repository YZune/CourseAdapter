package main.java.test

import main.java.parser.ZPTCParser
import java.io.File

fun main(){
    val file = File("search_grkb.htm")
    val parser = ZPTCParser(file.readText())
    parser.saveCourse()
}