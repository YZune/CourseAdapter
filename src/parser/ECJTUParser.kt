package parser

import bean.Course
import org.jsoup.Jsoup

// 华东交大
class ECJTUParser(source: String) : Parser(source) {
    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()
        val doc = Jsoup.parse(source)
        val kbtable = doc.select("table[class=table_border]").first()
        var teacher = ""
        for (tr in kbtable.getElementsByTag("tr")) {
            val tds = tr.getElementsByTag("td")
            if (tds.size == 8) {
                if (tds[0].text().trim() == "节次")
                    continue
                val timeInfos = tr.html().split("\n")
                var courseName = ""
                var startWeek = 1
                var endWeek = 16
                var startNode = 1
                var endNode = 2
                var type = 0
                var day = -1
                var room = ""
                timeInfos.forEach {
                    day++
                    //val courseInfo = Jsoup.parse(it).text().trim().split("<br>")
                    val step1 = Jsoup.parse(it).html().replace("<br>", "br")
                    val step2 = Jsoup.parse(step1).text().dropLast(2)
                    val courseInfo = step2.trim().split("br")

                    if (courseInfo.size == 2) {
                        courseName = courseInfo[0].trim()
                        teacher = courseInfo[1].substringBefore(' ').trim()
                        type = when {
                            courseInfo[1].contains('单') -> 1
                            courseInfo[1].contains('双') -> 2
                            else -> 0
                        }
                        startWeek = courseInfo[1].substringAfter(' ').substringBefore('-').toInt()
                        endWeek = if (type == 0) {
                            courseInfo[1].substringAfter('-').substringBefore(' ').toInt()
                        } else {
                            courseInfo[1].substringAfter('-').substringBefore('(').toInt()
                        }
                        startNode = courseInfo[1].substringAfter('-').substringAfter(' ').substringBefore(',').toInt()
                        endNode = courseInfo[1].substringAfterLast(',').toInt()
                        room = " "
                        courseList.add(
                            Course(
                                name = courseName, day = day, room = room,
                                teacher = teacher, startNode = startNode,
                                endNode = endNode, startWeek = startWeek,
                                endWeek = endWeek, type = type
                            )
                        )
                    }

                    if (courseInfo.size == 3) {
                        if (courseInfo[1].contains('@')) {
                            teacher = courseInfo[1].substringBefore('@').trim()
                            room = courseInfo[1].substringAfter('@')
                        }
                        courseName = courseInfo[0].trim()
                        type = when {
                            courseInfo[2].contains('单') -> 1
                            courseInfo[2].contains('双') -> 2
                            else -> 0
                        }

                        if (courseInfo[2].contains('-')) {
                            startWeek = courseInfo[2].substringBefore('-').toInt()
                            endWeek = if (type == 0) {
                                courseInfo[2].substringAfter('-').substringBefore(' ').toInt()
                            } else {
                                courseInfo[2].substringAfter('-').substringBefore('(').toInt()
                            }
                        }

                        startNode = courseInfo[2].substringAfter(' ').substringBefore(',').toInt()
                        endNode = courseInfo[2].substringAfterLast(',').toInt()
                        courseList.add(
                            Course(
                                name = courseName, day = day, room = room,
                                teacher = teacher, startNode = startNode,
                                endNode = endNode, startWeek = startWeek,
                                endWeek = endWeek, type = type
                            )
                        )
                    }
                }
            }
        }
        return courseList
    }
}