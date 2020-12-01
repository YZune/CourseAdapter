package test

import parser.NewZFParser
import parser.Parser
import java.io.File

fun main() {
    val path = "/Users/yzune/YZune_Git/database/python/schools/东北大学秦皇岛分校/38916.东北大学秦皇岛分校.html"
    val file = File(path)
    val fs = file.listFiles()
    (NewZFParser(file.readText()) as Parser).saveCourse()
    println()
    fs?.forEach {
        if (it.isDirectory) return
        val content = it.readText()
        try {
            (NewZFParser(content) as Parser).saveCourse()
            println("↑" + it.name)
            println()
        } catch (e: Exception) {
            if (content.contains("星期一") && content.contains("星期二") && content.contains("节次")) {
                e.printStackTrace()
                println("↑" + it.name)
                println()
            }
        }
    }
}