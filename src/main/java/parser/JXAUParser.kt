package main.java.parser

import Common.getDayInt
import bean.Course
import org.jsoup.Jsoup
import parser.Parser

class JXAUParser(source: String) : Parser() {
    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()
        val doc = Jsoup.parse(source)
        val table = doc.getElementById("KebiaoTable1")
        val trs = table.getElementsByTag("tr").subList(2,9)
        for(tr in trs) {
            val day = getDayInt(tr.getElementsByClass("left1")[0].text())
            val tds = tr.getElementsByTag("td")
            var startNode = 1
            tds.removeAt(0)
            for(i in tds.indices) {
                if(i == 4 || i == 6) {
                    continue
                }
                val dls = tds[i].getElementsByTag("dl")
                for(dl in dls) {
                    val name = dl.getElementsByTag("a")[0].text()
                    val teacher = dl.getElementsByTag("dd")[0].text()
                    val room = dl.getElementsByTag("b")[0].text()
                    val week = dl.getElementsByTag("dd")[1].text().split("(")[0].split(",")
                    for(reg in week) {
                        var startWeek : Int
                        var endWeek : Int
                        if(reg.contains("-")) {
                            val allweek = reg.split("-")
                            startWeek = allweek[0].toInt()
                            endWeek = allweek[1].toInt()
                        }else{
                            startWeek = reg.toInt()
                            endWeek = startWeek
                        }
                        val c = Course(
                            name = name, day = day, room = room, teacher = teacher, startNode = startNode,
                            endNode = startNode+1, startWeek = startWeek, endWeek = endWeek, type = 0,
                        )
                        courseList.add(c)
                    }
                }
                startNode += 2
            }
        }
        return courseList
    }
}
