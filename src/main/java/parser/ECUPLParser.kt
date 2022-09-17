package main.java.parser

import bean.Course
import main.java.bean.TimeDetail
import main.java.bean.TimeTable
import main.java.parser.supwisdom.SupwisdomParser
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.Calendar
import kotlin.math.min

class ECUPLParser(source: String) : SupwisdomParser(source) {
    private var finalWeekFits = false
    private var firstWeek = 0
    private var maxWeek = 20

    private val doc = Jsoup.parse(source)

    data class CourseDetails(
        val name: String,
        val teacher: String,
        val credit: Float,
        val note: String
    )

    private fun parseCourseDetailsTable(table: Element): Map<String, CourseDetails> {
        var courseNumberIndex = 2
        var courseTeacherIndex = 3
        var courseNameIndex = 4
        var courseCreditIndex = 7
        var courseNoteIndex = 12

        val courseDetailsMap = HashMap<String, CourseDetails>()

        for ((i, tr) in table.select("tr").withIndex()) {
            if (i == 0) {
                for ((j, td) in tr.select("td").withIndex()) {
                    when (td.text()) {
                        "课程序号" -> courseNumberIndex = j
                        "教师" -> courseTeacherIndex = j
                        "课程名称" -> courseNameIndex = j
                        "学分" -> courseCreditIndex = j
                        "备注" -> courseNoteIndex = j
                    }
                }
            } else {
                val tds = tr.select("td")
                courseDetailsMap[tds[courseNumberIndex].text()] = CourseDetails(
                    name = tds[courseNameIndex].text(),
                    teacher = tds[courseTeacherIndex].text(),
                    credit = tds[courseCreditIndex].text().toFloat(),
                    note = tds[courseNoteIndex].text()
                )
            }
        }

        return courseDetailsMap
    }

    override fun generateTimeTable(): TimeTable {
        val timeList = doc
            .selectFirst(".listTable")
            .selectFirst("tr")
            .select("td")
            .drop(1)
            .mapIndexed { i, td ->
                val text = td.text()
                TimeDetail(
                    node = i + 1,
                    startTime = text.substringBefore('-'),
                    endTime = text.substringAfter('-')
                )
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
        val cal = Calendar.getInstance()

        val yearMatchResult = Regex("""new CourseTable\((\d{4}),\d+\)""").find(source)
        val year = yearMatchResult?.run { groupValues[1].toInt() } ?: cal.get(Calendar.YEAR)
        cal.set(year, 11, 31)
        finalWeekFits = cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY

        val weekMatchResult = Regex("""table0\.marshalTable\((.+?),(.+?),(.+?)\);""").find(source)
            ?: throw Exception("未找到本学期的起始周！")
        run {
            val (firstWeekStr, _, maxWeekStr) = weekMatchResult.destructured
            firstWeek = firstWeekStr.toInt()
            maxWeek = maxWeekStr.toInt()
        }

        val superCourseList = super.generateCourseList()
        val courseDetailsMap = parseCourseDetailsTable(doc.select(".listTable")[1])

        return superCourseList.map {
            val dataCourseName = it.name.substringBeforeLast('(')
            val courseNumber = it.name.substringAfterLast('(').substringBefore(')')
            val courseDetails = courseDetailsMap[courseNumber]
            if (courseDetails == null) {
                println("未找到课程 $courseNumber 的详细信息")
                it
            } else {
                if (courseDetails.name != dataCourseName) {
                    println("课程 $courseNumber 名称不一致：表格中为“$dataCourseName”，数据中为“${courseDetails.name}”")
                }
                if (it.teacher != "(无教师数据)" && courseDetails.teacher.indexOf(it.teacher) < 0) {
                    println("课程 $courseNumber 教师信息不一致：表格中为“${courseDetails.teacher}”，数据中为“${it.teacher}”")
                }
                it.copy(
                    name = courseDetails.name, // assert: courseDetails.name == dataCourseName
                    teacher = courseDetails.teacher,
                    credit = courseDetails.credit,
                    note = courseDetails.note
                )
            }
        }
    }
}
