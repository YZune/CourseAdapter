package main.java.test

import main.java.parser.CNUParser
import java.io.File


fun main() {
    val source = File("D:\\Users\\dxxupup\\IdeaProjects\\CourseAdapter\\src\\main\\java\\教学信息一体化服务平台_y.html")
        .readText()
    CNUParser(source)
//        .generateCourseList()
        .saveCourse()

}