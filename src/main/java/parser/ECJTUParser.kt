package main.java.parser

import bean.Course
import org.jsoup.Jsoup
import parser.Parser

// 华东交大
class ECJTUParser(source: String) : Parser() {
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
                var courseName = ""
                var startWeek = 1
                var endWeek = 16
                var startNode = 1
                var endNode = 2
                var type = 0
                var day = -1
                var room = ""
                tds.forEach {
                    day++
                    if (day == 0) return@forEach
                    //val courseInfo = Jsoup.parse(it).text().trim().split("<br>")
                    val courseInfo = it.html().split("<br>").dropLast(1)
                    if (courseInfo.isEmpty()) return@forEach
                    var lastIndex = -1
                    for (i in courseInfo.indices) {
                        if (!courseInfo[i].contains('@') && courseInfo[i].trim().last().isDigit()) {
                            val detail = courseInfo[i].trim().split(" ")
                            courseName = if (lastIndex == -1) {
                                courseInfo[0].trim()
                            } else {
                                courseInfo[lastIndex + 1].trim()
                            }
                            if (detail.size == 2) {
                                if (courseInfo[i - 1].contains('@')) {
                                    teacher = courseInfo[i - 1].substringBefore('@').trim()
                                    room = courseInfo[i - 1].substringAfter('@').trim()
                                } else {
                                    teacher = ""
                                    room = ""
                                }
                            } else if (detail.size == 3) {
                                teacher = detail[0]
                                room = ""
                            } else {
                                teacher = ""
                                room = ""
                            }
                            type = when {
                                detail[detail.size - 2].contains('单') -> 1
                                detail[detail.size - 2].contains('双') -> 2
                                else -> 0
                            }
                            startWeek = detail[detail.size - 2].substringBefore('-').trim().toInt()
                            endWeek = if (type == 0) {
                                detail[detail.size - 2].substringAfter('-').trim().toInt()
                            } else {
                                detail[detail.size - 2].substringAfter('-').substringBefore('(').trim().toInt()
                            }
                            startNode = detail.last().trim().substringBefore(',').toInt()
                            endNode = detail.last().trim().substringAfterLast(',').toInt()

                            courseList.add(
                                Course(
                                    name = courseName, day = day, room = room,
                                    teacher = teacher, startNode = startNode,
                                    endNode = endNode, startWeek = startWeek,
                                    endWeek = endWeek, type = type,
                                )
                            )

                            lastIndex = i
                        }
                    }
                }
            }
        }
        return courseList
    }
}