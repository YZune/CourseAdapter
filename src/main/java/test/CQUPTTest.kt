package main.java.test

import main.java.parser.CQUPTParser
import org.jsoup.Jsoup
import java.io.File

fun main() {
    val file = File("C:\\Users\\wrzg8\\Desktop\\kebiao.html")
    val doc = Jsoup.parse(file.readText())

    // [XXX, XXX-计算机科学导论, 地点：XXX, 1-12周, XXX, 必修, 4.0学分, 选课学生名单]
    // 这些用来测试上面这种列表的size是不是8的倍数
    val classesLine = doc.getElementById("stuPanel").select("tr[style=text-align:center]")[0]
    val coursesLine = classesLine.select("td")
    println(coursesLine.size)
    for (courseLine in coursesLine) {
        val classInfo = courseLine.text().replace(" -", "-").split(" ")
        println(classInfo)
        println(classInfo.size)
    }
    println()
    println()
    println()

    val parser = CQUPTParser(file.readText())
    parser.saveCourse()
}