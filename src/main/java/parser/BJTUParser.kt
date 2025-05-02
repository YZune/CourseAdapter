package main.java.parser

import bean.Course
import parser.Parser
import java.io.File

/*
北京交通大学本科
研究生可能不通用
 */

class BJTUParser(source: String) : Parser() {

    private val regexCourseLine = Regex("\\w{7}\\s\\S+\\s[^\\[]+\\[.]\\s[^A-Z]+\\w{5}")
    private val regexClassName = Regex("]\\s.+\\s\\[")
    private val regexClassRoom = Regex("[A-Z]{2}\\d\\d\\d")
    private val regexClassTeacher = Regex("\\d\\d周\\s\\S+")
    private val regexStartWeek = Regex("]\\s第\\d\\d")
    private val regexEndWeek = Regex("\\d\\d周")

    override fun generateCourseList(): List<Course> {
        //使用jsoup解析源码
        val doc = org.jsoup.Jsoup.parse(source)
        //获取课程表的table
        val table = doc.getElementsByClass("table table-bordered")
        //因为byclass拿到的是一个数组所以[0],获取列trs
        val trs = table[0].getElementsByTag("tr")
        val result = arrayListOf<Course>()
        var weekdayNo: Int//代表星期几
        var isFirstLine: Boolean//因为第一个显示的是课程时间段，得筛掉
        for ((timeNo, tr) in trs.withIndex()) {
            val tds = tr.getElementsByTag("td")
            isFirstLine = true
            weekdayNo = 1
            for (td in tds) {
                val courseSource = td.text().trim()
                if (courseSource != "") {
                    if (isFirstLine) {
                        isFirstLine = false
                        continue
                    }

                    val courseLine = regexCourseLine.findAll(courseSource).toList()
                    for (each in courseLine) {

                        val tempEach = each.value
                        var startWeek: Int
                        var endWeek: Int
                        var className: String
                        var classRoom: String
                        var classTeacher: String

                        className = regexClassName.find(tempEach)?.value.toString()
                        className = className.slice(2..className.length - 3)

//                        val tempClassName = tempEach.slice(13..tempEach.indexOf("[", 13) - 2)
//                        if (tempClassName.length > 6) {
//                            className = tempClassName.slice(0..5)
//                            className += tempClassName.slice(6 until tempClassName.length)
//                        } else {
//                            className = tempClassName
//                        }

                        classRoom = regexClassRoom.find(tempEach)?.value.toString()
                        classTeacher = regexClassTeacher.find(tempEach)?.value?.split(" ")?.get(1).toString()
                        startWeek = regexStartWeek.find(tempEach)?.value?.slice(3..4)?.toInt() ?: 1
                        endWeek = regexEndWeek.find(tempEach)?.value?.slice(0..1)?.toInt() ?: 16
                        val type: Int = if (tempEach.contains(",")) {
                            if (endWeek % 2 == 0) 2 else 1
                        } else {
                            0
                        }
                        if (className != "")
                            result.add(
                                    Course(
                                        name = className,
                                        day = weekdayNo,
                                        room = classRoom,
                                        teacher = classTeacher,
                                        startNode = timeNo,
                                        endNode = timeNo,
                                        startWeek = startWeek,
                                        endWeek = endWeek,
                                        type = type,
                                    )
                            )

                    }

                }
                weekdayNo++
            }
        }
        return result
    }

}

fun main() {
    val file = File("C:\\Users\\14223\\Desktop\\北京交通大学教学服务管理平台.html")
    val parser = BJTUParser(file.readText())
    parser.saveCourse()
}