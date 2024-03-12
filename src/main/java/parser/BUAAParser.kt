package main.java.parser

import bean.Course
import com.google.gson.Gson
import main.java.bean.BUAACourseInfo
import main.java.bean.TimeDetail
import main.java.bean.TimeTable
import parser.Parser

/**
 * Date: 2024/03/02
 * 课表地址: https://byxt.buaa.edu.cn/ -> 查询 -> 课表查询 -> 我的课表
 * 项目地址: https://github.com/PandZz/CourseAdapter
 * 作者: PandZz
 *
 * 北京航空航天大学-新本研教务
 * 解析了POST(https://byxt.buaa.edu.cn/jwapp/sys/homeapp/api/home/student/getMyScheduleDetail.do)的返回结果(json)
 */

class BUAAParser(source: String) : Parser(source) {
    private val teacherAndWeekRegex = Regex("""^(.+)\[(\d+)-(\d+)周(?:\(([单双])\))?]$""")
    override fun getNodes(): Int {
        return 14
    }

    override fun generateTimeTable(): TimeTable {
        return TimeTable(
            name = "北京航空航天大学", timeList = listOf(
                TimeDetail(1, "08:00", "08:45"),
                TimeDetail(2, "08:50", "09:35"),
                TimeDetail(3, "09:50", "10:35"),
                TimeDetail(4, "10:40", "11:25"),
                TimeDetail(5, "11:30", "12:15"),

                TimeDetail(6, "14:00", "14:45"),
                TimeDetail(7, "14:50", "15:35"),
                TimeDetail(8, "15:50", "16:35"),
                TimeDetail(9, "16:40", "17:25"),
                TimeDetail(10, "17:30", "18:15"),

                TimeDetail(11, "19:00", "19:45"),
                TimeDetail(12, "19:50", "20:35"),
                TimeDetail(13, "20:40", "21:25"),
                TimeDetail(14, "21:30", "22:15")
            )
        )
    }

    override fun generateCourseList(): List<Course> {
        val result = arrayListOf<Course>()
        val response = Gson().fromJson(source, BUAACourseInfo::class.java)
        response.datas.arrangedList.forEach { courseItem ->
            parseCourseItem(courseItem).forEach {
                result.add(it)
            }
        }
        return result
    }

    data class TeacherAndWeek(
        val teacher: String,
        val beginWeek: Int,
        val endWeek: Int,
        val type: Int // 0: 每周, 1: 单周, 2: 双周
    )

    // 解析教师和周数, 例如: "张三[1-16周(单)]" -> TeacherAndWeek("张三", 1, 16, 1)
    private fun parseTeacherAndWeek(teachersAndWeeks: String): TeacherAndWeek {
        val matchResult = teacherAndWeekRegex.find(teachersAndWeeks)
        if (matchResult != null) {
            val (teacher, beginWeekStr, endWeekStr, typeStr) = matchResult.destructured
            val beginWeek = beginWeekStr.toInt()
            val endWeek = endWeekStr.toInt()
            val type = when (typeStr) {
                "单" -> 1
                "双" -> 2
                else -> 0
            }

            val teacherAndWeek = TeacherAndWeek(teacher, beginWeek, endWeek, type)
//            println(teacherAndWeek)
            return teacherAndWeek
        }
        return TeacherAndWeek("", 0, 0, 0)
    }

    private fun parseCourseItem(courseItem: BUAACourseInfo.Datas.CourseItem): List<Course> {
        val result = arrayListOf<Course>()
        val cellDetail = courseItem.cellDetail
        val name = courseItem.courseName
        val day = courseItem.dayOfWeek
        val room = courseItem.placeName
        cellDetail[1].text.split(" ").forEach { teacherAndWeeks ->
            val teacherAndWeek = parseTeacherAndWeek(teacherAndWeeks)
            val teacher = teacherAndWeek.teacher
            val beginWeek = teacherAndWeek.beginWeek
            val endWeek = teacherAndWeek.endWeek
            val type = teacherAndWeek.type
            val course = Course(
                name = name,
                day = day,
                room = room,
                teacher = teacher,
                startNode = courseItem.beginSection,
                endNode = courseItem.endSection,
                startWeek = beginWeek,
                endWeek = endWeek,
                type = type,
                credit = courseItem.credit.toFloat(),
                note = courseItem.titleDetail[8],
                startTime = courseItem.beginTime,
                endTime = courseItem.endTime
            )
            result.add(course)
        }
        return result
    }
}