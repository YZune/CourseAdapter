package main.java.parser

import bean.Course
import org.jsoup.Jsoup
import parser.Parser
import java.io.File
import java.util.regex.Pattern
import kotlin.io.path.readText


class CTGUParser(source:String) : Parser(source) {
    private val chineseDayMap = HashMap<String, Int>()
    private val courseTypeMap = HashMap<String, Int>()
    private val courseClockStartMap = HashMap<String, String>()
    private val courseClockEndMap = HashMap<String, String>()

    private fun iniChineseDayMap(){
        chineseDayMap["星期一"] = 1
        chineseDayMap["星期二"] = 2
        chineseDayMap["星期三"] = 3
        chineseDayMap["星期四"] = 4
        chineseDayMap["星期五"] = 5
        chineseDayMap["星期六"] = 6
        chineseDayMap["星期日"] = 7
    }

    private fun iniCourseTypeMap(){
        courseTypeMap["连续周"] = 0
        courseTypeMap["单周"] = 1
        courseTypeMap["双周"] = 2
    }

    private fun iniCourseClockStartMap(){
        // 上午
        courseClockStartMap["1"] = "08:00"
        courseClockStartMap["2"] = "08:50"
        courseClockStartMap["3"] = "09:55"
        courseClockStartMap["4"] = "10:45"
        courseClockStartMap["5"] = "11:35"
        // 下午
        courseClockStartMap["6"] = "14:20"
        courseClockStartMap["7"] = "15:10"
        courseClockStartMap["8"] = "16:15"
        courseClockStartMap["9"] = "17:05"
        // 晚上
        courseClockStartMap["10"] = "19:00"
        courseClockStartMap["11"] = "19:50"
        courseClockStartMap["12"] = "20:40"
    }

    private fun iniCourseClockEndMap(){
        // 上午
        courseClockEndMap["1"] = "08:45"
        courseClockEndMap["2"] = "09:35"
        courseClockEndMap["3"] = "10:40"
        courseClockEndMap["4"] = "11:30"
        courseClockEndMap["5"] = "12:20"
        // 下午
        courseClockEndMap["6"] = "15:05"
        courseClockEndMap["7"] = "15:55"
        courseClockEndMap["8"] = "17:00"
        courseClockEndMap["9"] = "17:50"
        // 晚上
        courseClockEndMap["10"] = "19:45"
        courseClockEndMap["11"] = "20:35"
        courseClockEndMap["12"] = "21:25"
    }

    init {
        iniChineseDayMap()
        iniCourseTypeMap()
        iniCourseClockStartMap()
        iniCourseClockEndMap()
    }

    override fun generateCourseList(): List<Course> {
        // 获取文件所在目录 Path
        val file = File(source)
        val directory = file.getParentFile().toPath()
        // 查找课表文件
        val courseFileName = "home.html"
        val courseFilePath = directory.resolve(courseFileName)
        // 新建课程列表
        val courseList = arrayListOf<Course>()
        // 转换课程源文件
        val doc = Jsoup.parse(courseFilePath.readText())
        // 页面共有四个tbody标签，第四个是包含课程条目的课程表主体部分
        val courseTable = doc.select("tbody")[3]

        val rows = courseTable.select("tr")
        for(courseRow in rows) {
            // 共计11个数据，其中 bjid 不知道是什么
            // kcbf:课程编号 kcmc:课程名称 bjmc:班级名称 kcxf:课程学分 jdid:阶段id（比如一周两次课，第一次就是阶段1，第二次就是阶段2）
            // rkjsxm:任课教师姓名 ksz:课程阶段 sjms:课程时间 dz:地址 xkrs:选课人数
            val courseName = courseRow.select("td[field=kcmc]")[0].children().first().text()

            // courseTime 的形式类似 “连续周 星期二 晚上第10节-晚上第11节”
            val courseTime = courseRow.select("td[field=sjms]")[0].children().first().text()
            val timeDetails = courseTime.split(' ')
            // 获取单双周
            val courseType = courseTypeMap[timeDetails[0]]
            // 获取上课的星期几
            val courseDay = chineseDayMap[timeDetails[1]]
            // 获取上课的节数
            // 正则表达式，只匹配数字
            val pattern = Pattern.compile("\\d+")
            val matcher = pattern.matcher(timeDetails[2])
            matcher.find()
            val startIndex = matcher.group()
            matcher.find()
            val endIndex = matcher.group()
            val startNode = startIndex.toInt()
            val endNode = endIndex.toInt()
            val startTime = courseClockStartMap[startIndex]
            val endTime = courseClockEndMap[endIndex]

            // 课程起始周和结束周
            val courseWeeks = courseRow.select("td[field=ksz]")[0].children().first().text().split('-')
            val startWeek = courseWeeks[0].toInt()
            val endWeek = courseWeeks[1].toInt()

            // 课程学分
            val credit = courseRow.select("td[field=kcxf]")[0].children().first().text().toFloat()

            // 老师
            val teacher = courseRow.select("td[field=rkjsxm]")[0].children().first().text()

            // 教室
            val room = courseRow.select("td[field=dz]")[0].children().first().text()

            // 添加此课程到课程列表中
            courseList.add(
                Course(
                    name = courseName,
                    day = courseDay ?: 0,
                    room = room,
                    teacher = teacher,
                    startNode = startNode,
                    endNode = endNode,
                    startWeek = startWeek,
                    endWeek = endWeek,
                    type = courseType ?: 0,
                    credit = credit,
                    startTime = "$startTime",
                    endTime = "$endTime",
                )
            )
        }

        return courseList
    }
}