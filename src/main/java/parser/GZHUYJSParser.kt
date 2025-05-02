package main.java.parser

import bean.Course
import org.jsoup.Jsoup
import parser.Parser

class GZHUYJSParser(source: String) : Parser() {
    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()
        val doc = Jsoup.parse(source)
        val kbtable = doc.select("table[class=tab_0]").first() //课表table
        val kcb = kbtable.getElementsByTag("tbody").first()
        var i = 0
        for (tr in kcb.getElementsByTag("tr")) {
            i = i + 1
            if (i == 5 || i == 6 || i == 7) {
                var j = 0
                for (td in tr.getElementsByTag("td")) {
                    j = j + 1
                    if (j > 1 && j < 9) {
                        for (div in td.getElementsByTag("div")) {
                            var courseName = ""
                            var startWeek = 1
                            var endWeek = 16
                            var startNode = 1
                            var endNode = 2
                            val type = 0
                            val day = j - 1
                            var room = ""
                            var teacher = ""
                            val courseInfo = div.html().split("<br>")
                            if (courseInfo.isEmpty()) continue
                            courseName = courseInfo[0].trim()
                            teacher = courseInfo[3].trim()
                            room = courseInfo[5].trim()
                            startWeek = courseInfo[4].substringBefore('-').trim().toInt()
                            endWeek = courseInfo[4].substringAfter('-').substringBefore('周').trim().toInt()
                            startNode = courseInfo[4].substringAfter("周 ").substringBefore('到').trim().toInt()
                            endNode = courseInfo[4].substringAfter('到').substringBefore('小').trim().toInt()
                            courseList.add(
                                Course(
                                    name = courseName, day = day, room = room,
                                    teacher = teacher, startNode = startNode,
                                    endNode = endNode, startWeek = startWeek,
                                    endWeek = endWeek, type = type,
                                )
                            )
                        }
                    }
                }
            }
        }
        return courseList;
    }
}
