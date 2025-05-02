package parser.qz

import bean.Course
import org.jsoup.Jsoup

class QzWithNodeParser(source: String) : QzParser(source) {

    override fun convert(day: Int, nodeCount: Int, infoStr: String, courseList: MutableList<Course>) {
        val courseHtml = Jsoup.parse(infoStr)
        val courseName = Jsoup.parse(infoStr.substringBefore("<br>").substringBefore("<font")).text().trim()
        val teacher = courseHtml.getElementsByAttributeValue("title", "老师").text().trim()
        val room = courseHtml.getElementsByAttributeValue(
            "title",
            "教室"
        ).text().trim() + courseHtml.getElementsByAttributeValue("title", "分组").text().trim()
        val tempStr = courseHtml.getElementsByAttributeValue("title", "周次(节次)").text()
        val weekStr = when {
            tempStr.contains(' ') -> courseHtml.getElementsByAttributeValue("title", "周次(节次)").text().split(' ')[0]
            tempStr.isBlank() -> courseHtml.getElementsByAttributeValue("title", "周次").text()
            else -> courseHtml.getElementsByAttributeValue("title", "周次(节次)").text().substringBefore(')')
        }
        val nodeList = when {
            tempStr.contains(' ') -> courseHtml.getElementsByAttributeValue(
                "title",
                "周次(节次)"
            ).text().split(' ')[1].removeSurrounding("[", "]").split('-')
            tempStr.isBlank() -> courseHtml.getElementsByAttributeValue(
                "title",
                "节次"
            ).text().substringAfter(')').removeSurrounding("[", "]").split('-')
            else -> courseHtml.getElementsByAttributeValue(
                "title",
                "周次(节次)"
            ).text().substringAfter(')').removeSurrounding("[", "]").split('-')
        }
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
            var startNode = nodeList.first().substringBefore('节').toInt()
            var endNode = nodeList.last().substringBefore('节').toInt()
            if (startNode > 99) {
                val str = nodeList[0].substringBefore('节').trim()
                startNode = str.substring(0, 2).toInt()
                endNode = str.substring(str.length - 2, str.length).toInt()
            }
            courseList.add(
                Course(
                    name = courseName, day = day,
                    room = room, teacher = teacher,
                    startNode = startNode,
                    endNode = endNode,
                    startWeek = startWeek, endWeek = endWeek,
                    type = type,
                )
            )
        }
    }

}