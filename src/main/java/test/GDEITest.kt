package main.java.test

import main.java.parser.GDEIParser
import java.io.File
//广东第二师范学院
fun main(){
    val source = File("C:\\Users\\Chen1\\Downloads\\Documents\\教务系统.html").readText()
    GDEIParser(source).apply{
        generateCourseList()
        //saveCourse()
    }
}