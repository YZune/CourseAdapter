package main.java.parser

import bean.Course
import parser.Parser

//哈尔滨工程大学研究生院
class HrbeuGraduateParser(source: String) : Parser(source) {
    private val brTrim = Regex("(^<br>)|(<br>$)")
    private val numRange = Regex("(\\d+)(-(\\d+))?")
    private val teach = Regex("[(（](.*)[)）]")

    override fun generateCourseList(): List<Course> {
        val doc = org.jsoup.Jsoup.parse(source)
        val table1 = doc.getElementById("StuCul_TimetableQry_TimeTable").getElementsByClass("WtbodyZlistS")[0]
        val trs = table1.getElementsByTag("tr")
        val courseList = ArrayList<Course>()
        for (tr in trs) {
            val tds = tr.getElementsByTag("td")
            var countDay = 1
            for (i in 1 until tds.size) {
                val courseSource = tds[i].html().replace(brTrim, "")
                if (courseSource.length <= 1) {
                    countDay++
                    continue
                }

                for (course in courseSource.split("<br><br>")) {
                    val split = course.split("<br>")
                    if (split.isEmpty() || split.size < 5) continue
                    courseList.addAll(parseCourseInfo(split, countDay))
                }

                countDay++
            }
        }
        return courseList
    }

    private fun parseCourseInfo(split: List<String>, countDay: Int): List<Course> {
        val resCourseList = ArrayList<Course>()

        //去除类型词
        val splitEnd = ArrayList<String>()
        for (it in split) {
            splitEnd.add(it.substringAfter(':'))
        }

        //获取信息
        val macher = numRange.find(splitEnd[2])
        val startNode: Int = macher!!.groupValues[1].toInt()
        val endNode: Int = macher.groupValues[3].toInt()
        if ("一班多师" in splitEnd[1]) {
            for (item in splitEnd[3].split(";")) {
                numRange.findAll(item).forEach {
                    val values = it.groupValues
                    val startWeek: String = values[1].trim()
                    val endWeek: String = if (values[3].trim() == "") values[1] else values[3].trim()

                    val teacher = teach.find(item)!!.groupValues[1]
                    resCourseList.add(
                        Course(
                            splitEnd[0],
                            countDay,
                            splitEnd[4],
                            teacher,
                            startNode,
                            endNode,
                            startWeek.toInt(),
                            endWeek.toInt(),
                            0
                        )
                    )
                }
            }
        } else {
            val teacher = splitEnd[1].substringBeforeLast("(").trim()
            numRange.findAll(splitEnd[3]).forEach {
                val values = it.groupValues
                val startWeek: String = values[1].trim()
                val endWeek: String = if (values[3].trim() == "") values[1] else values[3].trim()
                resCourseList.add(
                    Course(
                        splitEnd[0],
                        countDay,
                        splitEnd[4],
                        teacher,
                        startNode,
                        endNode,
                        startWeek.toInt(),
                        endWeek.toInt(),
                        0
                    )
                )
            }
        }

        return resCourseList
    }
}