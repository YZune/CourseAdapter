package main.java.parser

import Common
import bean.Course
import org.jsoup.Jsoup
import parser.Parser

// 暨南大学
class JNUParser(source: String) : Parser(source) {
    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()

        val xml = source.substringAfter("</html>")
        val doc = Jsoup.parse(xml)
        val frame = doc.getElementById("oReportCell")
        val table = frame.getElementsByClass("a8")
        val trs = table[0].getElementsByTag("tr").subList(3, 10)

        var courseName: String
        var room: String
        var type: Int
        for (i in trs.indices) {
            val tds = trs[i].getElementsByTag("td")
            if (tds.isNullOrEmpty()) continue
            for (j in tds.indices) {
                val str = tds[j].getElementsByTag("div").text()?.trim()
                if (str.isNullOrEmpty() || j == 0) continue
                type = when {
                    str.contains("单周") -> {
                        1
                    }
                    str.contains("双周") -> {
                        2
                    }
                    else -> {
                        0
                    }
                }
                room = if (type != 0) {
                    str.substringAfter('周').trim().substringBefore(' ')
                } else {
                    str.substringBefore(' ')
                }
                courseName = str.substringAfter("课程：").substringBeforeLast('(')
                val c = Course(
                    name = courseName, day = i + 1, room = room, teacher = "", startNode = j,
                    endNode = j, startWeek = 1, endWeek = 18, type = type
                )
                if (courseList.isNotEmpty() && Common.judgeContinuousCourse(courseList.last(), c)) {
                    courseList.last().endNode++
                } else {
                    courseList.add(c)
                }
            }
        }
        return courseList
    }
}