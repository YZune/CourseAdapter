package main.java.parser

import bean.Course
import org.jsoup.Jsoup
import parser.Parser

class ECNUParser(source: String) : Parser(source) {
    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()
        val document = Jsoup.parse(source)

        val table = document.select("table.table")[1]
        val tableData = table.select("thead")[1]

        val rows = tableData.select("tr")
        for (i in 1 until rows.size) {
            val row = rows[i]
            val tds = row.select("td")

            if (tds.size < 7) continue

            val name = tds[0].text()
            val courseCode = tds[1].text()
            val className = tds[2].text()
            val classCode = tds[3].text()
            val teacher = tds[4].text()
            val timeLocation = tds[5].text()
            val comment = tds[6].text()

            val timeLocationList = timeLocation.split("；").dropLast(1)

            for (tl in timeLocationList) {
                val course = parseCourse(name, teacher, tl)
                courseList.addAll(course)
            }
        }

        return courseList
    }

    private fun parseCourse(name: String, teacher: String, timeLocation: String): List<Course> {
        val courses = mutableListOf<Course>()
        val parts = timeLocation.split("，")

        var weeks = listOf<Int>()
        var days = listOf<Int>()
        var sections = listOf<Int>()
        var position = ""

        for (part in parts) {
            when {
                part.contains("星期") -> {
                    days = parseDays(part)
                }
                part.contains("周") && !part.contains("星期") -> {
                    weeks = parseWeeks(part)
                }
                part.contains("节") -> {
                    sections = parseSections(part)
                }
                else -> {
                    position += "$part,"
                }
            }
        }

        position = position.trimEnd(',')

        for (day in days) {
            for (week in weeks) {
                for (section in sections) {
                    courses.add(Course(
                        name = name,
                        day = day,
                        room = position,
                        teacher = teacher,
                        startNode = section,
                        endNode = section,
                        startWeek = week,
                        endWeek = week,
                        type = 0,
                        credit = 0f,
                        note = "",
                    ))
                }
            }
        }

        return courses
    }

    private fun parseDays(part: String): List<Int> {
        val dayMap = mapOf(
            "星期一" to 1, "星期二" to 2, "星期三" to 3, "星期四" to 4,
            "星期五" to 5, "星期六" to 6, "星期日" to 7, "星期天" to 7
        )
        return part.split("每周").flatMap { dayPart ->
            dayMap.filter { (key, _) -> dayPart.contains(key) }.values
        }
    }

    private fun parseWeeks(part: String): List<Int> {
        val weekRanges = part.split(",")
        return weekRanges.flatMap { range ->
            val weeks = range.split("-").map { it.replace(Regex("[^0-9]"), "").toInt() }
            if (weeks.size == 1) listOf(weeks[0]) else (weeks[0]..weeks[1]).toList()
        }
    }

    private fun parseSections(part: String): List<Int> {
        val sectionRange = part.replace(Regex("[^0-9-]"), "").split("-")
        return if (sectionRange.size == 1) {
            listOf(sectionRange[0].toInt())
        } else {
            (sectionRange[0].toInt()..sectionRange[1].toInt()).toList()
        }
    }
}