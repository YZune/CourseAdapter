package parser

import bean.Course
import org.jsoup.Jsoup

class CSXYParser(source: String) : Parser() {
    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()
        val doc = Jsoup.parse(source)
        var node = 1
        var day: Int
        while (node <= 5) {
            day = 1
            while (day <= 7) {
                val s = doc.getElementById("$node-$day-2").html().split("<br>")
                if (s[0] != "&nbsp;") {
                    var i = 0
                    while (i < s.size - 1) {
                        val name = s[i++]
                        i++
                        var teacher = s[i++]
                        teacher = teacher.substring(1, teacher.length - 2)
                        var weekStr = s[i++]
                        weekStr = weekStr.substring(1, weekStr.length - 3)
                        val room = s[i++]
                        val weeks = weekStr.split(",")
                        for (week in weeks) {
                            val split = week.split("-")
                            if (split.size == 1) {
                                courseList.add(
                                    Course(
                                        name = name, day = day, room = room, teacher = teacher,
                                        startNode = node * 2 - 1, endNode = node * 2, startWeek = week.toInt(),
                                        endWeek = week.toInt(), type = 0,
                                    )
                                )
                            } else {
                                courseList.add(
                                    Course(
                                        name = name, day = day, room = room, teacher = teacher,
                                        startNode = node * 2 - 1, endNode = node * 2, startWeek = split[0].toInt(),
                                        endWeek = split[1].toInt(), type = 0,
                                    )
                                )
                            }
                        }
                    }
                }
                day++
            }
            node++
        }
        return courseList
    }
}