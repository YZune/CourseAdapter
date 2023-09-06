package main.java.test

import main.java.parser.SWJTUGraduateParser
import java.io.File
import java.nio.file.Paths


/*
适用于西南交通大学研究生
应该不适用于本科
  Date: 2023/09/05
Author: Zorua

由于本人不是很懂Kotlin，因此代码很大程度参考了已有的代码文件
 */

fun main(){
    val path = Paths.get("").toAbsolutePath().toString()
    println(path)
    val file = File("./a.html")
    val parser = SWJTUGraduateParser(file.readText())
    parser.saveCourse()
}