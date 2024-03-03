package main.java.parser

import bean.Course
import main.java.bean.TimeDetail
import main.java.bean.TimeTable
import main.java.parser.TimeTableBuilder.Companion.buildTimeTable
import org.jsoup.Jsoup
import parser.qz.QzParser

class FSPTParser(source: String) : QzParser(source) {

    private val sundayFirstDayMap = arrayOf(0, 1, 2, 3, 4, 5, 6, 7)
    private var sundayFirst = false

    override fun generateTimeTable(): TimeTable {
        return buildTimeTable("FSPT") {
            add("08:40", "09:20")
            add("09:25", "10:05")
            add("10:25", "11:05")
            add("11:10", "11:50")

            add("14:00", "14:40")
            add("14:45", "15:25")
            add("15:45", "16:25")
            add("16:30", "17:10")

            add("19:00", "19:40")
            add("19:45", "20:25")
            add("20:35", "21:15")
            add("21:20", "22:00")
        }
    }

    override fun convert(day: Int, nodeCount: Int, infoStr: String, courseList: MutableList<Course>) {
        val node = nodeCount * 2 - 1
        println(infoStr)
        val courseHtml = Jsoup.parse(infoStr)
        val courseName = parseCourseName(infoStr)
        val teacher = courseHtml.getElementsByAttributeValue("title", "老师").text().trim()
        val room = courseHtml.getElementsByAttributeValue(
            "title",
            "教室"
        ).text().trim() + courseHtml.getElementsByAttributeValue("title", "分组").text().trim()
        val weekStr = courseHtml.getElementsByAttributeValue("title", "周次(节次)").text().run {
            when {
                contains("(周)") -> substringBefore("(周)")
                else -> substringBefore("周")
            }
        }
        val weekList = weekStr.split(',')
        var startWeek = 0
        var endWeek = 0
        var type = 0
        println(weekList.joinToString(", "))
        weekList.forEach {
            if (it.contains('-')) {
                val weeks = it.split('-')
                if (weeks.isNotEmpty()) {
                    startWeek = weeks[0].toInt()
                }
                if (weeks.size > 1) {
                    type = when {
                        weeks[1].contains('单') -> 1
                        weeks[1].contains('双') -> 2
                        else -> 0
                    }
                    endWeek = weeks[1].substringBefore('(').toInt()
                }
            } else {
                startWeek = it.substringBefore('(').toInt()
                endWeek = it.substringBefore('(').toInt()
            }
            courseList.add(
                Course(
                    name = courseName, room = room,
                    teacher = teacher, day = day,
                    startNode = node, endNode = node + 1,
                    startWeek = startWeek, endWeek = endWeek,
                    type = type
                )
            )
        }
    }

    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()
        val doc = Jsoup.parse(source)
        val kbtable = doc.getElementById("kbtable")
        val trs = kbtable.getElementsByTag("tr")
        try {
            val ths = kbtable.getElementsByTag("th")
            sundayFirst =
                ths.indexOfFirst { it.text().contains("星期日") } < ths.indexOfFirst { it.text().contains("星期一") }
        } catch (e: Exception) {

        }
        var nodeCount = 0
        for (tr in trs) {
            val tds = tr.getElementsByTag("td")
            if (tds.isEmpty()) {
                continue
            }
            nodeCount++

            var day = 0

            for (td in tds) {
                day++
                val divs = td.getElementsByTag("div")
                for (div in divs) {
                    val courseElements = div.getElementsByClass(webTableName)
                    if (courseElements.text().isBlank()) {
                        continue
                    }
                    val courseHtml = courseElements.html()
                    println(courseHtml)
                    var startIndex = 0
                    var splitIndex = courseHtml.indexOf("---------------------")
                    while (splitIndex != -1) {
                        convert(
                            sundayFirstDayMap[day],
                            nodeCount,
                            courseHtml.substring(startIndex, splitIndex),
                            courseList
                        )
                        startIndex = courseHtml.indexOf("<br>", splitIndex) + 4
                        splitIndex = courseHtml.indexOf("---------------------", startIndex)
                    }
                    convert(
                        sundayFirstDayMap[day],
                        nodeCount,
                        courseHtml.substring(startIndex, courseHtml.length),
                        courseList
                    )
                }
            }
        }
        return courseList
    }

}

class TimeTableBuilder(
    val name: String
) {
    private val timeDetials = mutableListOf<TimeDetail>()
    private var curNode = 1
    fun add(start: String, end: String) {
        add(curNode++, start, end)
    }
    fun add(node: Int, start: String, end: String) {
        timeDetials.add(TimeDetail(node, start, end))
    }
    fun build(): TimeTable {
        return TimeTable(name, timeDetials)
    }
    companion object {
        fun buildTimeTable(name: String, block: TimeTableBuilder.() -> Unit): TimeTable {
            return TimeTableBuilder(name).also(block).build()
        }
    }
}
