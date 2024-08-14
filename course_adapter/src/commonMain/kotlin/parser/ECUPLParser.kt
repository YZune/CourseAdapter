package parser

import bean.Course
import bean.TimeDetail
import bean.TimeTable
import bean.WeekBean
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toInstant
import utils.Common

/** A modified version of [SupwisdomParser]. */
class ECUPLParser(source: String) : Parser(source) {

    private val doc = Ksoup.parse(source)

    private val script = doc.selectFirst("script[language=JavaScript]")?.data()
        ?: throw Exception("未找到数据")
    private val semesterStart: Long
    private val timeTable: List<TimePeriod>

    class TimePeriod(val start: Int, val end: Int)

    private fun Int.formatTime(): String {
        return "${(this / 100).toString().padStart(2, '0')}:${(this % 100).toString().padStart(2, '0')}"
    }

    init {
        val regex = Regex("""new CourseTable\('([-\d]+?)',\[([\d\[\],]+)\]\)""")
        val match = regex.find(script) ?: throw Exception("未找到起始日和时间表信息")
        semesterStart = LocalDateTime(LocalDate.parse(match.groupValues[1]), LocalTime(0, 0, 0)).toInstant(kotlinx.datetime.TimeZone.UTC).epochSeconds
        timeTable = Regex("""\[(\d+),(\d+)\]""").findAll(match.groupValues[2]).asIterable().map {
            TimePeriod(it.groupValues[1].toInt(), it.groupValues[2].toInt())
        }
    }

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
        val timeList = timeTable
            .mapIndexed { i, period ->
                TimeDetail(
                    node = i + 1,
                    startTime = period.start.formatTime(),
                    endTime = period.end.formatTime()
                )
            }
        return TimeTable(name = "华东政法大学", timeList = timeList)
    }

    private fun parseWeekBeans(yearStartDate: String, rawWeekBits: Long): List<WeekBean> {
        val yearStart = LocalDateTime(LocalDate.parse(yearStartDate), LocalTime(0, 0, 0)).toInstant(kotlinx.datetime.TimeZone.UTC).epochSeconds

        val offsetMillis = yearStart - semesterStart
        val secondsInWeek = 86400L * 7

        val weekOffset = offsetMillis.floorDiv(secondsInWeek).toInt()

        val weeks = ArrayList<Int>(16)
        val trailingZeros = rawWeekBits.countTrailingZeroBits()

        var weekBits = rawWeekBits ushr trailingZeros
        var i = weekOffset + trailingZeros
        while (weekBits != 0L) {
            if (weekBits and 1L != 0L) {
                weeks.add(i)
            }
            weekBits = weekBits ushr 1
            i++
        }
        return Common.weekIntList2WeekBeanList(weeks)
    }

    override suspend fun generateCourseList(): MutableList<Course> {
        val regex = Regex(
            """newActivity\(".*?","(.*?)",".+?","(.+?)",".*?","(.*?)","([-\d]+)",(\d+)\);[\n\s\S]+?addActivityByTime\(activity,(\d),(\d+),(\d+)\);"""
        )
        val courseDetailsMap = parseCourseDetailsTable(doc.select(".listTable")[1])

        return regex.findAll(script).asIterable().flatMap { match ->
            val groupValues = match.groupValues

            val nameWithNumber = groupValues[2]
            val splitIndex = nameWithNumber.lastIndexOf('(')
            val number = nameWithNumber.substring(splitIndex + 1, nameWithNumber.length - 1)

            var name = nameWithNumber.substring(0, splitIndex)
            var teacher = groupValues[1]
            var location = groupValues[3]
            var credit = 0f
            var note = ""

            val details = courseDetailsMap[number]
            if (details != null) {
                name = details.name
                credit = details.credit

                if (teacher.isEmpty()) {
                    teacher = details.teacher
                } else if (teacher != details.teacher) {
                    teacher = "$teacher (${details.teacher})"
                }

                if ("地点" in details.note) {
                    location = "$location (${details.note})"
                } else {
                    note = details.note
                }
            }

            val startTime = groupValues[7].toInt()
            val startPeriod = timeTable.indexOfFirst { it.start == startTime } + 1
            val endTime = groupValues[8].toInt()
            val endPeriod = timeTable.indexOfFirst { it.end == endTime } + 1

            parseWeekBeans(groupValues[4], groupValues[5].toLong()).map { weekBean ->
                Course(
                    name = name,
                    day = groupValues[6].toInt(),
                    room = location,
                    teacher = teacher,
                    startNode = startPeriod,
                    endNode = endPeriod,
                    startWeek = weekBean.start,
                    endWeek = weekBean.end,
                    type = weekBean.type,
                    credit = credit,
                    note = note,
                )
            }
        }.toCollection(ArrayList())
    }
}
