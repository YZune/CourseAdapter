package main.java.parser

import bean.Course
import org.jsoup.Jsoup
import parser.Parser
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class HUNNUParser(source: String?) : Parser(source!!) {
    override fun generateCourseList(): List<Course> {
        val pattern =
            Pattern.compile("([^;].+?)\\(.+?\\) \\((.+?)\\);\\(([\\u5355\\u53cc]?)(\\d+-?\\d+?)( \\d+-?\\d+?)*,(.+?)\\)")
        var matcher: Matcher
        val document = Jsoup.parse(source)
        val list = ArrayList<Course>()
        var course: Course
        var name: String?
        var day: Int
        var room: String?
        var teacher: String?
        var startNode: Int
        var endNode: Int
        var startWeek: Int
        var endWeek: Int
        var type: Int
        var i = 0
        while (i <= 90) {
            val element = document.getElementById("TD" + i + "_0")
            if (element != null) {
                val str = element.attr("title").replace(";;;", ";")
                matcher = pattern.matcher(str)
                while (matcher.find()) {
                    name = matcher.group(1)
                    teacher = matcher.group(2)
                    type = if (matcher.group(3) == "单") {
                        1
                    } else if (matcher.group(3) == "双") {
                        2
                    } else {
                        0
                    }
                    val weekInfo = matcher.group(4).split("-".toRegex()).toTypedArray()
                    startWeek = weekInfo[0].toInt()
                    endWeek = if (weekInfo.size > 1) {
                        weekInfo[1].toInt()
                    } else {
                        startWeek
                    }
                    room = matcher.group(6)
                    day = i / 13 + 1
                    startNode = i % 13 + 1
                    endNode = i % 13 + 1
                    var k = i + 1
                    while (document.getElementById("TD" + k + "_0") == null) {
                        endNode++
                        k++
                        i++
                    }
                    if (matcher.group(5) != null) {
                        course = Course(
                            name,
                            day,
                            room,
                            teacher,
                            startNode,
                            endNode,
                            matcher.group(5).split("-".toRegex()).toTypedArray()[0].replace(" ", "").toInt(),
                            matcher.group(5).split("-".toRegex()).toTypedArray()[1].toInt(),
                            type
                        )
                        list.add(course)
                    }
                    course = Course(name, day, room, teacher, startNode, endNode, startWeek, endWeek, type)
                    list.add(course)
                }
            }
            i++
        }

        return list
    }
}
