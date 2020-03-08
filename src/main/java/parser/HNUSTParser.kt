package parser

import Common
import bean.Course
import org.jsoup.Jsoup

// 湖南科技大学
// 湖南科技大学潇湘学院
class HNUSTParser(source: String) : Parser(source) {

    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()
        val doc = Jsoup.parse(source)
        val kbtable = doc.getElementById("kbtable")
        val trs = kbtable.getElementsByTag("tr")
        var courseNamePreIndex = -1

        for (tr in trs) {
            val tds = tr.getElementsByTag("td")
            if (tds.isEmpty()) {
                continue
            }

            var day = -1

            for (td in tds) {
                day++
                val divs = td.getElementsByTag("div")
                for (div in divs) {
                    if (div.text().isBlank() || !Common.weekPattern2.containsMatchIn(div.text())) continue
                    val split = div.html().split("<br>")
                    var preIndex = -1

                    fun toCourse() {
                        if (preIndex == -1) return
                        val courseName = Jsoup.parse(split[preIndex - courseNamePreIndex]).text().trim()
                        val room = Jsoup.parse(split[preIndex + 1]).text().trim()
                        val teacher = Jsoup.parse(split[preIndex - 1]).text().trim()

                        val timeInfo = Jsoup.parse(split[preIndex]).text().trim().split(",")
                        timeInfo.forEach {
                            val weekStr = it.trim().substringBefore('周')
                            val startWeek =
                                if (weekStr.contains('-')) weekStr.split('-')[0].toInt() else weekStr.toInt()
                            val endWeek = if (weekStr.contains('-')) weekStr.split('-')[1].toInt() else weekStr.toInt()
                            val startNode = div.attr("id").split('-')[0].toInt() * 2 - 1
                            courseList.add(
                                Course(
                                    name = courseName, teacher = teacher,
                                    room = room, day = day,
                                    startNode = startNode, endNode = startNode + 1,
                                    startWeek = startWeek, endWeek = endWeek,
                                    type = 0
                                )
                            )
                        }
                    }

                    for (i in split.indices) {
                        if (Common.weekPattern2.containsMatchIn(split[i])) {
                            if (preIndex != -1) {
                                toCourse()
                            }
                            if (courseNamePreIndex == -1 && preIndex == -1) {
                                courseNamePreIndex = i
                            }
                            preIndex = i
                        }
                        if (i == split.size - 1) {
                            toCourse()
                        }
                    }
                }
            }
        }
        return courseList
    }

}