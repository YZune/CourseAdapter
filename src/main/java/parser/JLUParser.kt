package main.java.parser

import Common
import bean.Course
import com.google.gson.Gson
import main.java.bean.JLUCourseInfo
import parser.Parser

/**
 * 吉林大学研究生教务系统
 */
class JLUParser(source:String):Parser(source) {
    override fun generateCourseList(): List<Course> {
        val result = arrayListOf<Course>()
        val gson = Gson()
        val json = gson.fromJson(source, JLUCourseInfo::class.java)
        val rows = json.datas.xsjxrwcx.rows
        val multiWeekPattern = """(\d+)(?:-(\d+))?([单双]?)周""".toRegex()
        val dayOfWeekPattern = """(星期[一二三四五六日七])""".toRegex()
        val nodePattern = """(\d+)(?:-(\d+))?节""".toRegex()
        val locationPattern = """节](.+)""".toRegex()
        for (row in rows) {
            if (row.PKSJDD==null)
                continue
        val courseStrings = row.PKSJDD.split(";")
        // 遍历每个课程字符串并提取信息 courseStrings [3-4周 星期一[1-4节]新民-第一教学楼-101, 5周 星期一[1-4节]新民-第一教学楼-101]
        for (courseString in courseStrings) {
                 // ”3-5单周,7-12周 星期二[1-4节]“ 应对此类情况
                val multiWeekResult = multiWeekPattern.findAll(courseString)
                val dayOfWeekMatchResult = dayOfWeekPattern.find(courseString)
                val nodeMatchResult = nodePattern.find(courseString)
                val locationMatchResult = locationPattern.find(courseString)

                if (dayOfWeekMatchResult != null && nodeMatchResult!=null){
                    val dayOfWeek = dayOfWeekMatchResult.value
                    val (startSection, endSection) = nodeMatchResult.destructured
                    var location = ""
                    if (locationMatchResult != null) {
                        location = locationMatchResult.value.replace("节]", "")
                    }
                    multiWeekResult.forEach {
                        val (startWeek, endWeek, oddEvenWeek) = it.destructured
                        val course = createCourse(row, dayOfWeek, location, startSection, endSection, startWeek, endWeek, oddEvenWeek)
                        result.add(course)
                    }
                }
            }
        }
        return result
    }


    private fun createCourse(
        row: JLUCourseInfo.Datas.Xsjxrwcx.Row, // 请替换为正确的行类型
        dayOfWeek: String,
        location: String,
        startSection: String,
        endSection: String,
        startWeek: String,
        endWeek: String,
        oddEvenWeek: String
    ): Course {
        return  Course(
            name = row.KCMC,
            day = Common.getDayInt(dayOfWeek),
            room = location,
            teacher = row.RKJS,
            startNode = startSection.toInt(),
            endNode = if (endSection.isEmpty()) startSection.toInt() else endSection.toInt(),
            startWeek = startWeek.toInt(),
            endWeek = if (endWeek.isEmpty()) startWeek.toInt() else endWeek.toInt(),
            type = when(oddEvenWeek){
                "" -> 0
                "单" -> 1
                "双" -> 2
                else -> 0
            }
        )
    }
}