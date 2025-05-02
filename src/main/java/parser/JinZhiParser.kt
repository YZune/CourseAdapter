package main.java.parser

import bean.Course
import org.jsoup.Jsoup
import parser.Parser
import java.io.File

class JinZhiParser(source: String) : Parser() {

    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()
        val doc = Jsoup.parse(source)
        var courseName: String
        var teacher: String
        var detail: List<String>
        var day = 1
        var start = 1
        var end = 1
        var room: String
        var startWeek = 1
        var endWeek = 1
        var type = 0
        doc.getElementsByClass("wut_table").first()
            .getElementsByClass("mtt_arrange_item").forEach { c ->
                courseName = c.getElementsByClass("mtt_item_kcmc")?.first()?.ownText()?.trim()?.substringAfter(' ')
                    ?: return@forEach
                teacher = c.getElementsByClass("mtt_item_jxbmc")?.text()?.trim() ?: ""
                detail = c.getElementsByClass("mtt_item_room")?.first()?.text()?.trim()?.split(',')
                    ?: return@forEach
                val dayIndex = detail.indexOfFirst { it.trim().startsWith("星期") }
                day = try {
                    detail[dayIndex].trim().takeLast(1).toInt()
                } catch (e: Exception) {
                    1
                }
                try {
                    val nodeInfo = detail[dayIndex + 1].substringBefore('节').split('-')
                    if (nodeInfo.size > 1) {
                        start = nodeInfo[0].trim().toInt()
                        end = nodeInfo.last().trim().toInt()
                    } else {
                        start = nodeInfo[0].trim().toInt()
                        end = start
                    }
                } catch (e: Exception) {
                    start = 1
                    end = 1
                }
                room = if (detail.size - dayIndex > 3) detail[detail.size - 2] else detail.last()
                for (i in 0 until dayIndex) {
                    try {
                        type = when {
                            detail[i].contains('单') -> {
                                1
                            }
                            detail[i].contains('双') -> {
                                2
                            }
                            else -> {
                                0
                            }
                        }
                        val weekInfo = detail[i].substringBefore('周').split('-')
                        val startWeekStr = weekInfo[0].trim()
                        for (j in startWeekStr.indices.reversed()) {
                            if (!startWeekStr[j].isDigit()) {
                                startWeek = startWeekStr.substring(j + 1).toInt()
                                break
                            }
                            if (j == 0) {
                                startWeek = startWeekStr.toInt()
                            }
                        }
                        endWeek = if (weekInfo.size > 1) {
                            weekInfo.last().trim().toInt()
                        } else {
                            startWeek
                        }
                    } catch (e: Exception) {
                        startWeek = 1
                        endWeek = 1
                    }
                    courseList.add(
                        Course(
                            name = courseName, day = day, room = room,
                            teacher = teacher, startNode = start,
                            endNode = end, startWeek = startWeek, endWeek = endWeek, type = type,
                        )
                    )
                }
            }
        return courseList
    }

}

fun main() {
    File("/Users/yzune/Downloads/金智列表1.txt").readLines().forEach {
        println(it)
        JinZhiParser(File("/Users/yzune/YZune_Git/database/python/$it").readText()).saveCourse()
        println()
    }
}