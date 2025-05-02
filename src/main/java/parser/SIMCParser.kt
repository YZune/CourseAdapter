package main.java.parser

import bean.Course
import main.java.bean.TimeDetail
import main.java.bean.TimeTable
import org.jsoup.Jsoup
import parser.Parser
import java.time.LocalDate
import kotlin.collections.ArrayList

class SIMCParser(source: String) : Parser() {

    private val dom = Jsoup.parse(source)

    data class MyCourse(
        var name: String,
        var position: String,
        var teacher: String,
        var weeks: ArrayList<Int>,
        var day: Int,
        var sections: ArrayList<Int>,
        var note: String = "",
        var credit: Float = 0f,
        var startTime: String = "",
        var endTime: String = ""
    )

    private val courseInfos = arrayListOf<MyCourse>()

    private fun myCourse2Course(courseInfos: ArrayList<MyCourse>): ArrayList<Course> {
        val res = arrayListOf<Course>()
        courseInfos.forEach { e ->
            val sections = arrayListOf<List<Int>>()
            val s = e.sections
            if (s.isNotEmpty()) {
                var temp = arrayListOf(s[0])
                for (i in 1 until s.count()) {
                    if (s[i - 1] + 1 != s[i] || s[i-1] == 4) {
                        sections.add(temp)
                        temp = arrayListOf(s[i])
                    } else {
                        temp.add(s[i])
                    }
                }
                sections.add(temp)
            }

            sections.forEach {
                Common.weekIntList2WeekBeanList(e.weeks).forEach { week ->
                    res.add(
                        Course(
                            name = e.name.replace(Regex("""\(([0-9]{4})\)\((.*?)\)"""), ""),
                            day = e.day,
                            room = e.position,
                            teacher = e.teacher,
                            startNode = it.first(),
                            endNode = it.last(),
                            startWeek = week.start,
                            endWeek = week.end,
                            type = week.type,
                            credit = e.credit,
                            note = e.note,
                            startTime = e.startTime,
                            endTime = e.endTime,
                        )
                    )
                }
            }
        }
        return res
    }

    private fun getSection(s: Int): Int {
        if(s==13){
            return 10
        }else if(s<9){
            return s + 1
        }else{
            return s + 2
        }
    }

    private var termFrom = 0
    private var termStart = 0
    private var termEnd = 0
    private var termLength = 0

    override fun getMaxWeek(): Int? {
        val termInfo = Regex("""table\.marshalTable\((.+?),(.+?),(.+?)\);""").find(source)
        return if (termInfo != null) {
            termFrom = termInfo.groupValues[1].toInt()
            termStart = termInfo.groupValues[2].toInt()
            termEnd = termInfo.groupValues[3].toInt()
            termLength = termEnd - termStart + 1
            termLength
        } else null
    }

    private fun getWeeks(validWeeks: String): ArrayList<Int> {
        val week = arrayListOf<Int>()
        val str = validWeeks.repeat(2)
        for (i in termStart..termEnd) {
            if (str[termFrom + i - 2] == '1') {
                week.add(i)
            }
        }
        return week
    }

    override fun getTableName(): String = "${LocalDate.now()}导入的课表"

    override fun getNodes(): Int = 14

    override fun generateTimeTable(): TimeTable {
        return TimeTable(name = "SIMC", timeList = listOf(
            TimeDetail(node = 1, startTime = "08:30", endTime = "09:15"),
            TimeDetail(node = 2, startTime = "09:15", endTime = "10:00"),
            TimeDetail(node = 3, startTime = "10:15", endTime = "11:00"),
            TimeDetail(node = 4, startTime = "11:00", endTime = "11:45"),
            TimeDetail(node = 5, startTime = "13:00", endTime = "13:45"),
            TimeDetail(node = 6, startTime = "13:45", endTime = "14:30"),
            TimeDetail(node = 7, startTime = "14:45", endTime = "15:30"),
            TimeDetail(node = 8, startTime = "15:30", endTime = "16:15"),
            TimeDetail(node = 9, startTime = "16:15", endTime = "17:00"),
            TimeDetail(node = 10, startTime = "17:00", endTime = "17:45")
        ))
    }

    override fun generateCourseList(): List<Course> {
        courseInfos.clear()
        getMaxWeek()
        Regex("activity = new TaskActivity").split(source).forEach { i ->
            //教师编号、教师姓名、课程编号、课程名称、教室编号、教室名称、validWeeks字符串
            val courseData = Regex("""\("(.*?)","(.*?)","(.*?)","(.*?)","(.*?)","(.*?)","(.*?)"\);""").find(i)
            if (courseData != null) {
                val courseName = courseData.groupValues[4]
                val position = courseData.groupValues[6]
                val teacher = courseData.groupValues[2]
                val weeks = getWeeks(courseData.groupValues[7])
                val sectionData = Regex("""index =(.*?)\*unitCount\+(.*?);""").findAll(i)
                val day = sectionData.first().groupValues[1].toInt() + 1
                val sectionDays = arrayListOf<Int>()
                sectionData.forEach {
                    sectionDays.add(getSection(it.groupValues[2].toInt()))
                }
                sectionDays.sort()
                val courseList = dom.select(".grid > table")[0]
                var note = ""
                var credit = 0f
                courseList.select("tr").drop(1).forEach {
                    val cells = it.select("td")
                    val listCourseID = cells[0].text()
                    val listCourseName = cells[2].text() + if (listCourseID.isNotBlank()) "($listCourseID)" else ""
                    val listCredit = cells[3].text().toFloat()
                    if (courseName.startsWith(listCourseName)) {
                        credit = listCredit
                    }
                }
                var startTime = ""
                var endTime = ""
                if(sectionDays.first()==sectionDays.last()){
                    if (sectionDays.first()==11) {
                        startTime = "07:00"
                        endTime = "13:30"
                        sectionDays.clear()
                        sectionDays.addAll(intArrayOf(1,2,3,4,5).asList())
                    }else if (sectionDays.first()==12) {
                        startTime = "14:00"
                        endTime = "20:30"
                        sectionDays.clear()
                        sectionDays.addAll(intArrayOf(6,7,8,9,10).asList())
                    }else if (sectionDays.first()==13) {
                        startTime = "08:15"
                        endTime = "15:30"
                        sectionDays.clear()
                        sectionDays.addAll(intArrayOf(1,2,3,4,5,6,7,8).asList())
                    }else if (sectionDays.first()==14) {
                        startTime = "16:00"
                        endTime = "22:00"
                        sectionDays.clear()
                        sectionDays.add(9)
                        sectionDays.add(10)
                    }
                }

                val same = courseInfos.firstOrNull {
                    it.name == courseName &&
                            it.position == position &&
                            it.teacher == teacher &&
                            it.day == day
                }
                if (same == null) {
                    courseInfos.add(
                        MyCourse(
                            courseName,
                            position,
                            teacher,
                            weeks,
                            day,
                            sectionDays,
                            note,
                            credit,
                            startTime,
                            endTime
                        )
                    )
                } else {
                    val newWeeks = arrayListOf<Int>()
                    newWeeks.addAll(weeks)
                    newWeeks.addAll(same.weeks)
                    newWeeks.distinct()
                    newWeeks.sort()

                    newWeeks.forEach{
                        if(weeks.contains(it)&&same.weeks.contains(it)){
                            val tempSections= arrayListOf<Int>()
                            tempSections.addAll(sectionDays)
                            tempSections.addAll(same.sections)
                            tempSections.distinct()
                            tempSections.sort()
                            courseInfos.add(
                                MyCourse(
                                    courseName,
                                    position,
                                    teacher,
                                    arrayListOf(it),
                                    day,
                                    tempSections,
                                    note,
                                    credit,
                                    startTime,
                                    endTime
                                )
                            )
                        }else if(weeks.contains(it)){
                            courseInfos.add(
                                MyCourse(
                                    courseName,
                                    position,
                                    teacher,
                                    arrayListOf(it),
                                    day,
                                    sectionDays,
                                    note,
                                    credit,
                                    startTime,
                                    endTime
                                )
                            )
                        }else if(same.weeks.contains(it)){
                            courseInfos.add(
                                MyCourse(
                                    courseName,
                                    position,
                                    teacher,
                                    arrayListOf(it),
                                    day,
                                    same.sections,
                                    note,
                                    credit,
                                    startTime,
                                    endTime
                                )
                            )
                        }
                    }
                    courseInfos.remove(same)
                }
            }
        }
        return myCourse2Course(courseInfos)
    }
}
