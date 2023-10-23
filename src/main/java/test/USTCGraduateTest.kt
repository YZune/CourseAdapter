package main.java.test

import main.java.parser.USTCGraduateParser
import java.io.File

fun main() {
    // 示例中用了相对路径，Windows 下可能需要修改
    // 建议从项目外引用 html 文件
    // 提交时一定不要上传 html 文件，涉及隐私问题
    /*
    请使用FireFox的SingleFile扩展，而不是Ctrl+S，去保存完整的HTML.
    将filePath修改为HTML文件的路径.
    */
    val filePath = "${System.getProperty("user.home")}/Downloads/yjs1.html"
    val parser = USTCGraduateParser(File(filePath).readText())
    parser.saveCourse()
}