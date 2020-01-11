package test

import parser.PekingParser
import java.io.File

fun main() {
    val path = "/Users/yzune/YZune_Git/database/python/schools/北京大学"
    val file = File(path)
    val fs = file.listFiles()
    fs?.forEach {
        if (it.isDirectory) return
        val content = it.readText()
        try {
            PekingParser(content).saveCourse()
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