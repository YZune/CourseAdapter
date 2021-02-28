package main.java.test

import main.java.parser.BJTUParser
import java.io.File

//北京交通大学本科
fun main(){
    val file = File("C:\\Users\\14223\\Desktop\\北京交通大学教学服务管理平台.html")
    val parser = BJTUParser(file.readText())
    parser.saveCourse()
}