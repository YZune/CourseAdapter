package main.java.parser.qz

import Common
import bean.Course
import org.jsoup.Jsoup
import parser.Parser
import java.io.File

// 华南农业大学
class Qz2017Parser(source: String) : Parser(source) {

    private val sundayFirstDayMap = arrayOf(7, 1, 2, 3, 4, 5, 6)

    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()
        val doc = Jsoup.parse(source)
        val header = doc.getElementsByClass("el-table__header").first().getElementsByTag("div")
        val sundayFirst =
            header.indexOfFirst { it.text().contains("星期日") } < header.indexOfFirst { it.text().contains("星期一") }
        val trs = doc.getElementsByClass("el-table__body").first()
            .getElementsByTag("tbody").first()
            .getElementsByTag("tr")
        var courseName: String
        var teacher: String
        var room: String
        var timeInfo: String
        var start: Int
        var end: Int
        var type = 0
        var startWeek = 1
        var endWeek = 20
        trs.forEachIndexed { nodeIndex, tr ->
            tr.select(".cell[style=text-align: center;]").forEachIndexed { dayIndex, cell ->
                cell.children().forEach {
                    val children = it.children()
                    val weekIndex = children.indexOfLast { item ->
                        Common.weekPattern2.containsMatchIn(item.text())
                    }
                    courseName = it.child(weekIndex - 3).text().trim()
                    teacher = it.child(weekIndex - 2).text().trim()
                    room = it.child(weekIndex + 1).text().trim()
                    timeInfo = it.child(weekIndex).text().trim()
                    start = nodeIndex * 2 + 1
                    end = if (start != ((trs.size - 1) * 2 + 1)) start + 1 else start + 2
                    Common.nodePattern2.find(timeInfo)?.let { eNode ->
                        eNode.groups[1]?.value?.let { str ->
                            if (str.contains('-')) {
                                val lst = str.split('-')
                                try {
                                    start = lst[0].toInt()
                                    end = lst[1].toInt()
                                } catch (e: Exception) {
                                }
                            } else {
                                try {
                                    start = str.toInt()
                                    end = str.toInt()
                                } catch (e: Exception) {
                                }
                            }
                            timeInfo = timeInfo.substringAfter('节')
                        }
                    }
                    type = when {
                        timeInfo.contains('单') -> 1
                        timeInfo.contains('双') -> 2
                        timeInfo.contains("单双") -> 0
                        else -> 0
                    }
                    timeInfo.substringAfter('(').substringBefore('周').split(',').forEach { week ->
                        if (week.contains('-')) {
                            val weeks = week.trim().split('-')
                            startWeek = weeks[0].trim().toInt()
                            endWeek = weeks[1].trim().toInt()
                        } else {
                            try {
                                startWeek = week.trim().toInt()
                                endWeek = startWeek
                            } catch (e: Exception) {
                                startWeek = 1
                                endWeek = 20
                            }
                        }
                        courseList.add(
                            Course(
                                name = courseName,
                                day = if (sundayFirst) sundayFirstDayMap[dayIndex] else dayIndex + 1,
                                room = room,
                                teacher = teacher, startNode = start, endNode = end,
                                startWeek = startWeek, endWeek = endWeek, type = type
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
    File("/Users/yzune/Downloads/强智2017.txt").readLines().forEach {
        println(it)
        val source = File("/Users/yzune/YZune_Git/database/python/$it").readText()
        Qz2017Parser(source).saveCourse()
        println()
    }
}