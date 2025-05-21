package main.java.test

import main.java.parser.WISTParser
import bean.Course
import java.io.File

fun main() {
    // 1. 读取 HTML 文件内容（请替换为你自己的 HTML 文件路径）
    // 示例中用了相对路径，Windows 下可能需要修改
    // 建议从项目外引用 html 文件
    // 提交时一定不要上传 html 文件，涉及隐私问题
    // 示例：D:/Download/Programs/WCtest.html
    val htmlFilePath = "WCtest.html"
    val htmlContent = File(htmlFilePath).readText()

    // 2. 初始化解析器并获取课程列表
    val parser = WISTParser(htmlContent)
    val courseList: List<Course> = parser.generateCourseList()

    // 3. 打印结果

    courseList.forEachIndexed { index, course ->
        println("第 ${index + 1} 门课：")
        println("  📓课程名   : ${course.name}")
        println("  🧑🏻‍🏫教师     : ${course.teacher}")
        println("  🌤️周数     : ${course.startWeek} - ${course.endWeek} (type=${course.type})")
        println("  🧵节次     : ${course.startNode} - ${course.endNode}")
        println("  ⏲️时间     : ${course.startTime} - ${course.endTime}")
        println("  📅星期     : ${course.day}")
        println("  🧭地点     : ${course.room}")
        println("  📝备注     : ${course.note}")
        println()
    }
    println("✅共解析出 ${courseList.size} 门课程：")
    println()

    // 4. 检查并列出冲突课程
    println("🔍 检查课程冲突：")
    val conflicts = mutableListOf<Pair<Course, Course>>()
    for (i in 0 until courseList.size) {
        for (j in i + 1 until courseList.size) {
            val a = courseList[i]
            val b = courseList[j]
            val sameDay = a.day == b.day
            val weekOverlap = a.startWeek <= b.endWeek && b.startWeek <= a.endWeek
            val timeOverlap = a.startNode <= b.endNode && b.startNode <= a.endNode
            if (sameDay && weekOverlap && timeOverlap) {
                conflicts += Pair(a, b)
            }
        }
    }
    if (conflicts.isEmpty()) {
        println("✅ 未发现课程冲突。")
    } else {
        println("⚠️ 共发现 ${conflicts.size} 处课程冲突：")
        conflicts.forEachIndexed { idx, (c1, c2) ->
            println("  冲突 ${idx + 1}:")
            println("  课程 A: ${c1.name} (星期${c1.day}, 周${c1.startWeek}-${c1.endWeek}, 节次 ${c1.startNode}-${c1.endNode})")
            println("  课程 B: ${c2.name} (星期${c2.day}, 周${c2.startWeek}-${c2.endWeek}, 节次 ${c2.startNode}-${c2.endNode})")
        }
    }
}
