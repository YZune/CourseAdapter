package main.java.parser

import bean.Course
import main.java.bean.TimeDetail
import main.java.bean.TimeTable
import org.jsoup.Jsoup
import main.java.parser.supwisdom.SupwisdomParser
import java.util.Calendar
import kotlin.math.min

class ECUPLParser(source: String) : SupwisdomParser(source) {
    private val cal = Calendar.getInstance()
    private var year = cal.get(Calendar.YEAR)
    private var finalWeekFits = false
    private var firstWeek = 0
    private var maxWeek = 20

    data class CourseDetails(
        val name: String,
        val teacher: String,
        val credit: Float,
        val note: String
    )

    private fun parseCourseDetailsTable(table: org.jsoup.nodes.Element): Map<String, CourseDetails> {
        var courseNumberIndex = 2
        var courseTeacherIndex = 3
        var courseNameIndex = 4
        var courseCreditIndex = 7
        var courseNoteIndex = 12

        val courseDetailsMap = mutableMapOf<String, CourseDetails>()

        table.select("tr").forEachIndexed { i, tr ->
            if (i == 0) {
                tr.select("td").forEachIndexed { j, td ->
                    when (td.text().trim()) {
                        "课程序号" -> courseNumberIndex = j
                        "教师" -> courseTeacherIndex = j
                        "课程名称" -> courseNameIndex = j
                        "学分" -> courseCreditIndex = j
                        "备注" -> courseNoteIndex = j
                    }
                }
            } else {
                val tds = tr.select("td")
                courseDetailsMap[tds[courseNumberIndex].text().trim()] = CourseDetails(
                    name = tds[courseNameIndex].text().trim(),
                    teacher = tds[courseTeacherIndex].text().trim(),
                    credit = tds[courseCreditIndex].text().toFloat(),
                    note = tds[courseNoteIndex].text().trim()
                )
            }
        }

        return courseDetailsMap
    }

    override fun generateTimeTable(): TimeTable {
        val timeList = mutableListOf<TimeDetail>()
        val doc = Jsoup.parse(source)
        doc.select(".listTable")[0].select("tr")[0].select("td").forEachIndexed { i, td ->
            if (i > 0) {
                timeList.add(
                    TimeDetail(
                        node = i,
                        startTime = td.text().substringBefore("-"),
                        endTime = td.text().substringAfter("-")
                    )
                )
            }
        }
        return TimeTable(name = "华东政法大学", timeList = timeList)
    }

    override fun getGroup(a: List<String>): String {
        return ""
    }

    override fun getCourseName(a: List<String>, groupName: String): String {
        return a[3].trim()
    }

    override fun getTeacher(a: List<String>): String {
        return if (a[1].isBlank()) {
            "(无教师数据)"
        } else {
            a[1].trim()
        }
    }

    override fun getRoom(a: List<String>): String {
        return a[5].trim()
    }

    override fun getWeekStr(a: List<String>): String {
        val weekStr = a[6]
        return if (weekStr.substring(0, firstWeek - 1).indexOf('1') < 0) {
            weekStr.substring(startIndex = firstWeek - 2, endIndex = min(firstWeek - 2 + maxWeek + 1, weekStr.length))
        } else if (finalWeekFits) {
            ("0".repeat(53 - firstWeek + 2) + weekStr).substring(0, maxWeek + 1)
        } else {
            ("0".repeat(53 - firstWeek + 1) + weekStr).substring(0, maxWeek + 1)
        }
    }

    override fun generateCourseList(): List<Course> {
        val doc = Jsoup.parse(source)

        /*
        var script = ""
        doc.select("script:not([src])").forEach {
            if (it.html().indexOf("var table0 = new CourseTable") >= 0) {
                script = it.html()
                return@forEach
            }
        }
        */

        val yearMatchResult = Regex("""new CourseTable\((\d{4}),\d+\)""").find(source)
        if (yearMatchResult != null) {
            val (yearStr) = yearMatchResult.destructured
            year = yearStr.toInt()
        }
        cal.set(year, 11, 31)
        finalWeekFits = cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY

        val weekMatchResult = Regex("""table0\.marshalTable\((.+?),(.+?),(.+?)\);""").find(source)
        if (weekMatchResult != null) {
            val (firstWeekStr, _, maxWeekStr) = weekMatchResult.destructured
            firstWeek = firstWeekStr.toInt()
            maxWeek = maxWeekStr.toInt()
        } else {
            throw Exception("未找到本学期的起始周！")
        }

        val superCourseList = super.generateCourseList()
        val courseList = mutableListOf<Course>()
        val courseDetailsMap = parseCourseDetailsTable(doc.select(".listTable")[1])

        superCourseList.forEach {
            val dataCourseName = it.name.substringBeforeLast('(')
            val courseNumber = it.name.substringAfterLast('(').removeSurrounding("", ")")
            val courseDetails = courseDetailsMap[courseNumber]
            if (courseDetails == null) {
                println("未找到课程 $courseNumber 的详细信息")
                courseList.add(it)
            } else {
                if (courseDetails.name != dataCourseName) {
                    println("课程 $courseNumber 名称不一致：表格中为“$dataCourseName”，数据中为“${courseDetails.name}”")
                }
                if (it.teacher != "(无教师数据)" && courseDetails.teacher.indexOf(it.teacher) < 0) {
                    println("课程 $courseNumber 教师信息不一致：表格中为“${courseDetails.teacher}”，数据中为“${it.teacher}”")
                }
                val c = it.copy(
                    name = courseDetails.name, // assert: courseDetails.name == dataCourseName
                    teacher = courseDetails.teacher,
                    credit = courseDetails.credit,
                    note = courseDetails.note
                )
                courseList.add(c)
            }
        }

        return courseList
    }
}
