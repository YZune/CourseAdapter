package main.java.parser.supwisdom

import Common
import bean.Course
import bean.WeekBean
import com.google.gson.JsonParser
import parser.Parser
import java.io.File

// 西安工业大学
// 西北政法大学
// 大连海事大学
// 天津农学院
// 辽宁工程技术大学
// 重庆医科大学
// 山东商业职业技术学院
open class SupwisdomParser(source: String) : Parser(source) {

    open fun getGroup(a: List<String>): String {
        return if (a.size < 7) {
            ""
        } else if (a.size == 11) {
            a[8]
        } else {
            a[6]
        }
    }

    open fun getCourseName(a: List<String>, groupName: String): String {
        return if (groupName.isNotBlank()) {
            a[1].substringBeforeLast('(').substringBeforeLast('[') + "(${groupName.removeSuffix("组")}组)"
        } else {
            a[1].substringBeforeLast('(').substringBeforeLast('[')
        }
    }

    open fun getTeacher(a: List<String>): String {
        return ""
    }

    open fun getRoom(a: List<String>): String {
        return a[3]
    }

    open fun getWeekStr(a: List<String>): String {
        return a[4]
    }

    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()
        var res: String = Regex(pattern = "var activity=null;[\\w\\W]*(?=table0.marshalTable)").find(source)!!.value
        res = Regex(pattern = "\\n\\s*").replace(res, "\n")
        res = Regex(pattern = ",\\r*\\n").replace(res, ",")
        res = Regex(pattern = "\\(\\r*\\n").replace(res, "(")
        val foundResults = Regex("^.+?;\\W*\$", RegexOption.MULTILINE).findAll(res)

        var courseName = ""
        var preCourseName = ""
        var teacher = ""
        var room = ""
        var weekList = mutableListOf<WeekBean>()
        val pattern = Regex("\"(.*?)\"")

        for (findText in foundResults) {
            val line = findText.value
            if (line.contains("courseName += ")) {
                courseName += line.substringAfter("\"").substringBeforeLast("\"")
            }
            if (line.contains("var teachers =")) {
                val teacherList = arrayListOf<String>()
                JsonParser.parseString(
                    line.substringAfter("var teachers =").substringBeforeLast(";").trim()
                ).asJsonArray.forEach {
                    teacherList.add(it.asJsonObject["name"].asString)
                }
                teacher = teacherList.joinToString(", ")
            }
            if (line.contains("new TaskActivity(")) {
                val a = pattern.findAll(line).map { it.value.removeSurrounding("\"") }.toList()
                println(a)
                val groupName = getGroup(a)
                val tmp = getCourseName(a, groupName)
                if (tmp.isNotBlank()) courseName = tmp
                room = getRoom(a)
                val newTeacher = getTeacher(a)
                if (newTeacher.isNotBlank()) {
                    teacher = newTeacher
                }
                val weekStr = getWeekStr(a)
                val weekIntList = arrayListOf<Int>()
                weekStr.forEachIndexed { index, c ->
                    if (c == '1') {
                        weekIntList.add(index)
                    }
                }
                weekList = Common.weekIntList2WeekBeanList(weekIntList)
            }
            if (line.contains("index =") && line.contains("*unitCount+")) {
                val timeInfo =
                    line.substringAfter("index =").substringBefore(";").split("*unitCount+").map { it.toInt() }
                if (courseName.isBlank()) {
                    courseName = preCourseName
                } else {
                    preCourseName = courseName
                }
                weekList.forEach { week ->
                    val c = Course(
                        name = courseName, teacher = teacher, room = room, startNode = timeInfo[1] + 1,
                        endNode = timeInfo[1] + 1, startWeek = week.start, endWeek = week.end, type = week.type,
                        day = timeInfo[0] + 1
                    )
                    courseList.add(c)
                }
                courseName = ""
            }
        }
        return courseList
    }

}

fun main() {
    SupwisdomParser(File("/Users/yzune/Downloads/商工 课程_files/商工 课程.html").readText()).saveCourse()
}