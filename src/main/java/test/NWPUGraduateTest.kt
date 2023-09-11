package main.java.test

import main.java.parser.NWPUGraduateParser
import java.io.File

/**
 *   Date: 2023/09/11
 * Author: @ludoux
 */

fun main() {
    //NWPUGraduateParser 里面有维护小提示，希望可以帮助到你。
    val file = File("path/to/your/webfile.htm")
    val parser = NWPUGraduateParser(file.readText())
    parser.saveCourse(true)
}
