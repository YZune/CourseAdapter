package parser

import Common
import bean.Course
import org.jsoup.Jsoup

class OldQzParser(source: String) : Parser() {

    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()
        val doc = Jsoup.parse(source)
        val kbtable = doc.getElementById("kbtable")
        val trs = kbtable.getElementsByTag("tr")

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
                        val courseName = Jsoup.parse(split[preIndex - 3]).text().trim()
                        val room = Jsoup.parse(split[preIndex + 1]).text().trim()
                        val teacher = Jsoup.parse(split[preIndex - 1]).text().trim()
                        val timeInfo = Jsoup.parse(split[preIndex]).text().trim().split("周[")
                        val startWeek =
                            if (timeInfo[0].contains('-')) timeInfo[0].split('-')[0].toInt() else timeInfo[0].toInt()
                        val endWeek =
                            if (timeInfo[0].contains('-')) timeInfo[0].split('-')[1].toInt() else timeInfo[0].toInt()
                        val startNode = timeInfo[1].split('-')[0].toInt()
                        val endNode = timeInfo[1].split('-')[1].substringBefore('节').toInt()

                        courseList.add(
                            Course(
                                name = courseName, day = day,
                                room = room, teacher = teacher,
                                startNode = startNode, endNode = endNode,
                                startWeek = startWeek, endWeek = endWeek,
                                type = 0,
                            )
                        )
                    }

                    for (i in split.indices) {
                        if (split[i].contains('[') && split[i].contains(']') && split[i].contains('节') && split[i].contains(
                                '周'
                            )
                        ) {
                            if (preIndex != -1) {
                                toCourse()
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