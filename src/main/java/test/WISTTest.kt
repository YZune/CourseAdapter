package main.java.test

import main.java.parser.WISTParser
import java.io.File

fun main() {
    // 示例中用了相对路径，Windows 下可能需要修改
    // 建议从项目外引用 html 文件
    // 提交时一定不要上传 html 文件，涉及隐私问题
    // 示例：D:\Download\Programs\WCtest.html
    val html = File("""?\WCtest.html""").readText()
    val parser = WISTParser(html)
    val courseList = parser.generateCourseList()

    courseList.forEachIndexed { index, course ->
        println("第 ${index + 1} 门课：$course")
    }

    println("✅ 总共解析出 ${courseList.size} 门课程")
}
