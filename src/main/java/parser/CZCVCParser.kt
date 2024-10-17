package main.java.parser

import Common
import bean.Course
import org.jsoup.Jsoup
import parser.Parser
import java.io.File


// 青果正选结果解析
class CZCVCParser(source: String) : Parser(source) {

    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()

        source.split("<head>", "</head>").forEach forHTML@{ html ->
            val doc = Jsoup.parse(html)
            doc.getElementById("pageRpt")?.getElementsByTag("table")?.forEach forTable@{ table ->
                if (!table.text().trimStart().startsWith("选定")) return@forTable
                table.select("tr[style]").forEach { tr ->
                    val courseName = tr.child(1).text().trim().substringAfterLast(']')
                    if (courseName.isEmpty()) return@forEach
                    val teacher = tr.child(4).text().substringBefore('[').trim()
                    val infos = tr.child(10).html().split("<br>")
                    val credit = try {
                        tr.child(2).text().trim().toFloat()
                    } catch (e: Exception) {
                        0f
                    }
                    infos.forEach forInfo@{ info ->
                        if (info.isBlank()) return@forInfo
                        val infoSplit = info.split('/')
                        var room = ""
                        if (infoSplit.size > 1) {
                            room = infoSplit[1].trim()
                        }
                        if (courseName.startsWith('m') && room.isEmpty()) {
                            room = "在线"
                        }
                        val timeText = infoSplit[0]
                        val weekText = timeText.substringBefore("星期")
                        val weekdayAndSectionText = timeText.substringAfter("星期")

                        var type = 0
                        if (weekText.contains('单')) {
                            type = 1
                        } else if (weekText.contains('双')) {
                            type = 2
                        }

                        val day = Common.getWeekFromChinese("周${weekdayAndSectionText[0]}")
                        val nodes = weekdayAndSectionText.substringAfter('[').substringBefore('节').split('-')
                        val startNode = nodes.first().toInt()
                        val endNode = nodes.last().toInt()
                        val weeks = weekText.substringAfterLast('[').substringBefore('周')
                            .removeSuffix("单").removeSuffix("双").split('-')
                        val startWeek = weeks.first().toInt()
                        val endWeek = weeks.last().toInt()

                        courseList.add(
                            Course(
                                name = courseName, day = day, room = room, teacher = teacher,
                                startNode = startNode, endNode = endNode, startWeek = startWeek, endWeek = endWeek,
                                type = type, credit = credit
                            )
                        )
                    }
                }
            }
        }

        return courseList
    }

}

fun main() {
    KingosoftZXParser(File("C:\\Users\\15459\\Desktop\\KINGOSOFT高校教学综合管理服务平台.html").readText()).saveCourse()
}
