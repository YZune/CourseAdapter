package main.java.test

import main.java.parser.CTGUParser

// 三峡大学研究生课表适配

fun main() {
    // 一个网页保存后有xxx.html文件和其资源文件夹xxx_files，课表在网页xxx.html中是iframe内嵌形式，因此课表文件实际是在资源文件夹中的home.html
    // 此处传入保存下来的html文件，parser内部会自动寻找资源文件夹下的 home.html 并生成课表
    val parser = CTGUParser("C:/Ddata/test/test_files/test.html")
    parser.saveCourse()
}