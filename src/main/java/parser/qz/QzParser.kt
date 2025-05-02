package parser.qz

import bean.Course
import org.jsoup.Jsoup
import parser.Parser

open class QzParser(source: String) : Parser() {

    private val sundayFirstDayMap = arrayOf(0, 7, 1, 2, 3, 4, 5, 6)
    private var sundayFirst = false
    open val webTableName = "kbcontent"

    open fun parseCourseName(infoStr: String): String {
        return Jsoup.parse(infoStr.substringBefore("<font").trim()).text()
    }

    open fun convert(day: Int, nodeCount: Int, infoStr: String, courseList: MutableList<Course>) {
        val node = nodeCount * 2 - 1
        val courseHtml = Jsoup.parse(infoStr)
        val courseName = parseCourseName(infoStr)
        val teacher = courseHtml.getElementsByAttributeValue("title", "老师").text().trim()
        val room = courseHtml.getElementsByAttributeValue(
            "title",
            "教室"
        ).text().trim() + courseHtml.getElementsByAttributeValue("title", "分组").text().trim()
        val weekStr = courseHtml.getElementsByAttributeValue("title", "周次(节次)").text().substringBefore("(周)")
        val weekList = weekStr.split(',')
        var startWeek = 0
        var endWeek = 0
        var type = 0
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
                    name = courseName, day = day,
                    room = room, teacher = teacher,
                    startNode = node, endNode = node + 1,
                    startWeek = startWeek, endWeek = endWeek,
                    type = type,
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
                    var startIndex = 0
                    var splitIndex = courseHtml.indexOf("-----")
                    while (splitIndex != -1) {
                        convert(
                            sundayFirstDayMap[day],
                            nodeCount,
                            courseHtml.substring(startIndex, splitIndex),
                            courseList
                        )
                        startIndex = courseHtml.indexOf("<br>", splitIndex) + 4
                        splitIndex = courseHtml.indexOf("-----", startIndex)
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