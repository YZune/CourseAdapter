package main.java.parser

import bean.Course
import main.java.bean.TimeDetail
import main.java.bean.TimeTable
import org.jsoup.Jsoup
import parser.Parser
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.collections.ArrayList

class SUESParser(source: String) : Parser(source) {

    //document.getElementById("main").contentWindow.document.getElementById("contentListFrame").contentWindow
    private val dom = Jsoup.parse(source)

    //“按照时间顺序处理课程”
    var followTimeOrder = true

    //类似“小爱课程表”的自定义课程数据类
    data class MyCourse(
        var name: String,
        var position: String,
        var teacher: String,
        var weeks: ArrayList<Int>,
        var day: Int,
        var sections: ArrayList<Int>,
        //当然，小爱没有下面这几个字段
        var note: String = "",
        var credit: Float = 0f,
        var startTime: String = "",
        var endTime: String = ""
    )

    private val courseInfos = arrayListOf<MyCourse>()

    //将自定义课程类转换成需要的课程类
    private fun myCourse2Course(courseInfos: ArrayList<MyCourse>): ArrayList<Course> {
        val res = arrayListOf<Course>()

        courseInfos.forEach { e ->
            //将非连续的节次以及跨越中午的节次分段
            var splitMidday = false
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
                    if (s[i - 1] == 4 && s[i] == 5) {
                        splitMidday = true
                    }
                }
                sections.add(temp)
            }

            //按分段后的连续节次分别创建Course对象导入
            sections.forEach {
                Common.weekIntList2WeekBeanList(e.weeks).forEach { week ->
                    res.add(
                        Course(
                            name = e.name,
                            teacher = e.teacher,
                            room = e.position,
                            startNode = it.first(),
                            endNode = it.last(),
                            startWeek = week.start,
                            endWeek = week.end,
                            type = week.type,
                            day = e.day,
                            note = e.note,
                            credit = e.credit,
                            startTime = e.startTime,
                            endTime = e.endTime
                        )
                    )
                }
            }
        }
        return res
    }

    //转换课程节次
    //“按时间顺序”时将9,10,11,12,13,14节调整为11,12,13,14,9,10
    private fun getSection(s: Int): Int {
        if (followTimeOrder) {
            if (s < 8) {
                return s + 1
            } else if (s < 12) {
                return s + 3
            } else if (s < 14) {
                return s - 3
            }
        }
        return s + 1
    }

    private var termFrom = 0
    private var termStart = 0
    private var termEnd = 0
    private var termLength = 0

    //获取学期总周数，顺便保存处理validWeeks字符串所需的参数
    override fun getMaxWeek(): Int? {
        val termInfo = Regex("""table0\.marshalTable\((.+?),(.+?),(.+?)\);""").find(source)
        return if (termInfo != null) {
            termFrom = termInfo.groupValues[1].toInt() //validWeeks字符串的起始位置
            termStart = termInfo.groupValues[2].toInt() //开始周（第一周）
            termEnd = termInfo.groupValues[3].toInt() //结束周
            termLength = termEnd - termStart + 1
            termLength
        } else null
    }

    //处理validWeeks字符串，返回weekIntList
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

    //获取课程表名称，这里用日期命名
    override fun getTableName(): String = "${LocalDate.now()}导入的课表"

    //获取节点（时间表一天的课程节数？）
    //generateTimeTable().timeList.count()
    override fun getNodes(): Int = 14

    //获取起始日期
    //找到最早开始的课程。通过其“第一次上课时间”推断开学日期
    //理论上任意课程都能推出来，后面研究一下
    override fun getStartDate(): String {
        //英文浏览器环境会显示Sep 6, 2022
        //中文浏览器环境会显示2022-9-6
        val cnFormatter = DateTimeFormatter.ofPattern("yyyy-M-d")
        val enFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale("en"))

        var firstCourseName = ""
        var firstCourseDate = LocalDate.of(1970, 1, 1)
        fun getDate(str: String): LocalDate? {
            return if (str.count { it == '-' } == 2) {
                LocalDate.parse(str, cnFormatter)
            } else if (str.count { it == ',' } == 1) {
                LocalDate.parse(str, enFormatter)
            } else {
                null
            }
        }

        dom.select(".listTable")[1].select("tr").drop(1).forEach {
            val cells = it.select("td").map { i -> i.text() }
            val listCourseID = cells[5]
            val listCourseName = cells[3] + if (listCourseID.isNotBlank()) "($listCourseID)" else ""
            val listFirstDate = getDate(cells[8])
            if (listFirstDate != null) {
                if (firstCourseDate == LocalDate.of(1970, 1, 1) ||
                    firstCourseDate > listFirstDate
                ) {
                    firstCourseName = listCourseName
                    firstCourseDate = listFirstDate
                }
            }
        }

        var firstDay = 8
        var firstWeek = termLength + 1
        courseInfos.forEach {
            if (it.name == firstCourseName) {
                if (it.weeks[0] < firstWeek) {
                    firstWeek = it.weeks[0]
                    firstDay = it.day
                } else if (it.weeks[0] == firstWeek && it.day < firstDay) {
                    firstDay = it.day
                }
            }
        }

        if (firstDay < 8 && firstWeek <= termLength && firstCourseDate != LocalDate.of(1970, 1, 1)) {
            firstCourseDate = firstCourseDate.minusDays(((firstWeek - 1) * 7 + firstDay - 1).toLong())
            firstCourseDate = firstCourseDate.minusDays((firstCourseDate.dayOfWeek.value - 1).toLong())
            return cnFormatter.format(firstCourseDate)
        }
        return "2021-9-6" //找不到就摆烂了，返回Generator原来包含的日期
    }

    //生成时间表
    //被我写死了，时间安排应该一时半会儿不会有什么大变动
    override fun generateTimeTable(): TimeTable {
        //by stevenlele
        /*
        val timeList = arrayListOf<TimeDetail>()
        dom.select(".listTable")[0].select("tr")[1].select("td").drop(1).forEachIndexed { i, td ->
            val text = td.text()
            val time = Regex("""\((.*?)~(.*?)\)""").find(text)
            if (time != null) {
                timeList.add(
                    TimeDetail(
                        node = i + 1,
                        startTime = time.groupValues[1],
                        endTime = time.groupValues[2]
                    )
                )
            }
        }
        */

        //上课顺序:1,2,3,4,5,6,7,8,13,14,9,10,11,12。在课表界面显示如此，13、14就在最后。
        val timeList: List<TimeDetail>
        if (followTimeOrder) {
            timeList = listOf(
                TimeDetail(node = 1, startTime = "08:15", endTime = "09:00"),
                TimeDetail(node = 2, startTime = "09:00", endTime = "09:45"),
                TimeDetail(node = 3, startTime = "10:05", endTime = "10:50"),
                TimeDetail(node = 4, startTime = "10:50", endTime = "11:35"),
                TimeDetail(node = 5, startTime = "13:00", endTime = "13:45"),
                TimeDetail(node = 6, startTime = "13:45", endTime = "14:30"),
                TimeDetail(node = 7, startTime = "14:50", endTime = "15:35"),
                TimeDetail(node = 8, startTime = "15:35", endTime = "16:20"),
                TimeDetail(node = 9, startTime = "16:30", endTime = "17:15"),
                TimeDetail(node = 10, startTime = "17:15", endTime = "18:00"),
                TimeDetail(node = 11, startTime = "18:00", endTime = "18:45"),
                TimeDetail(node = 12, startTime = "18:45", endTime = "19:30"),
                TimeDetail(node = 13, startTime = "19:30", endTime = "20:15"),
                TimeDetail(node = 14, startTime = "20:15", endTime = "21:00")
            )
            return TimeTable(name = "调序时间表", timeList = timeList)
        } else {
            timeList = listOf(
                TimeDetail(node = 1, startTime = "08:15", endTime = "09:00"),
                TimeDetail(node = 2, startTime = "09:00", endTime = "09:45"),
                TimeDetail(node = 3, startTime = "10:05", endTime = "10:50"),
                TimeDetail(node = 4, startTime = "10:50", endTime = "11:35"),
                TimeDetail(node = 5, startTime = "13:00", endTime = "13:45"),
                TimeDetail(node = 6, startTime = "13:45", endTime = "14:30"),
                TimeDetail(node = 7, startTime = "14:50", endTime = "15:35"),
                TimeDetail(node = 8, startTime = "15:35", endTime = "16:20"),
                TimeDetail(node = 9, startTime = "18:00", endTime = "18:45"),
                TimeDetail(node = 10, startTime = "18:45", endTime = "19:30"),
                TimeDetail(node = 11, startTime = "19:30", endTime = "20:15"),
                TimeDetail(node = 12, startTime = "20:15", endTime = "21:00"),
                TimeDetail(node = 13, startTime = "16:30", endTime = "17:15"),
                TimeDetail(node = 14, startTime = "17:15", endTime = "18:00")
            )
            return TimeTable(name = "原始时间表", timeList = timeList)
        }
    }

    //生成课程列表
    override fun generateCourseList(): List<Course> {
        //把原来存在对象里的信息清空
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

                //从课程列表单元格里找备注和学分
                val courseList = dom.select(".listTable")[1]
                var note = ""
                var credit = 0f
                courseList.select("tr").drop(1).forEach {
                    val cells = it.select("td")
                    val listCourseID = cells[5].text()
                    val listCourseName = cells[3].text() + if (listCourseID.isNotBlank()) "($listCourseID)" else ""
                    val listCredit = cells[4].text().toFloat()
                    val listNote = cells[7].text()
                    if (courseName == listCourseName) {
                        note = listNote
                        credit = listCredit
                    }
                }

                //D、E、F楼的第3、4节课10:25开始，11:55下课
                //J302、J303跟随DEF楼上下课时间
                //但似乎该项目的生成流程会忽略startTime与endTime参数
                var startTime = ""
                var endTime = ""
                if (Regex("""^([DEF][0-9]{3}|J302|J303)(多|\(中外教室）)$""").matches(position) &&
                    sectionDays.first() == 3 &&
                    sectionDays.last() == 4
                ) {
                    startTime = "10:25"
                    endTime = "11:55"
                }

                //合并当天相同课程，转换时再分段拆开
                val same = courseInfos.firstOrNull {
                    it.name == courseName &&
                            it.position == position &&
                            it.teacher == teacher &&
                            it.day == day &&
                            it.weeks == weeks
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
                    same.sections.addAll(sectionDays)
                    same.sections.sort()
                }
            }
        }
        return myCourse2Course(courseInfos)
    }
}
