package main.java.test

import main.java.parser.CNUParser
import java.io.File


fun main() {
    val source = File("教学信息一体化服务平台.html")
        .readText()
    CNUParser(source)
//        .generateCourseList()
        .saveCourse()

}