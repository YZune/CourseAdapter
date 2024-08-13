package test

import parser.SYSUParser2024
import java.io.File

fun main() {
    val file = File("./data/课表查询.htm")
    val parser = SYSUParser2024(file.readText())
    parser.saveCourse()
}