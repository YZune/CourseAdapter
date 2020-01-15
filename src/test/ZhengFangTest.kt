package test

import parser.ZhengFangParser
import java.io.File

fun main() {
    // 示例中用了相对路径，Windows 下可能需要修改
    // 建议从项目外引用 html 文件
    // 提交时一定不要上传 html 文件，涉及隐私问题
    val file = File("/Users/yzune/YZune_Git/database/python/schools/燕山大学/15886.燕山大学.html")
    val parser = ZhengFangParser(file.readText(), 0)
    parser.saveCourse()
}