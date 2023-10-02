package main.java.parser

import org.jsoup.Jsoup
import parser.BFAforYJSParser
import java.io.File

fun main() {
    val path = "D:/我的文件/北京电影学院/选课/课表测试HTML"
    val file = File(path)
    val fs = file.listFiles()
    fs?.forEach {
        if (it.isDirectory || !it.name.endsWith(".html")) return
        val content = parseHTMLFile(it)
        try {
            BFAforYJSParser(content).getTableName()
            BFAforYJSParser(content).getNodes()
            BFAforYJSParser(content).generateTimeTable()
            BFAforYJSParser(content).saveCourse()
            BFAforYJSParser(content).getMaxWeek()
            println("↑" + it.name)
            println()
        } catch (e: Exception) {
            if (content.contains("星期一") && content.contains("星期二")) {
                e.printStackTrace()
                println("↑" + it.name)
                println()
            }
        }
    }
}

fun parseHTMLFile(file: File): String {
    val doc = Jsoup.parse(file, "UTF-8")
    return doc.html()
}

