package main.java.parser.qz

import Common
import bean.Course
import org.jsoup.Jsoup
import parser.Parser

// 华南农业大学
class Qz2017Parser(source: String) : Parser(source) {

    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()
        val doc = Jsoup.parse(source)
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
                    courseName = it.child(3).text().trim()
                    teacher = it.child(4).text().trim()
                    room = it.child(7).text().trim()
                    timeInfo = it.child(6).text().trim()
                    start = nodeIndex * 2 + 1
                    end = if (start != 11) start + 1 else start + 2
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
                        timeInfo.contains('单') -> {
                            1
                        }
                        timeInfo.contains('双') -> {
                            2
                        }
                        else -> {
                            0
                        }
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
                                name = courseName, day = dayIndex + 1, room = room,
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
