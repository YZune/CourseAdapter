package main.java.parser

import parser.Parser
import bean.Course
import main.java.bean.TimeDetail
import main.java.bean.TimeTable
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * @author Jonathan523
 * @date 20241025
 * @email jonathon.zhang52306@gmail.com
 * 上海大学新选课系统导入
 */

// 课程表网页链接 `https://jwxk.shu.edu.cn/xsxk/elective/grablessons?{学期代码}`
// 登录链接：`https://jwxk.shu.edu.cn/`

class SHUParser2024(source: String) : Parser(source) {
    private val nodeNum = 12
    private val maxWeek = 12

    override fun generateCourseList(): List<Course> {
        val courseList = mutableListOf<Course>()

        val doc: Document = Jsoup.parse(source)

        // 解析课程详细信息表格
        val cards = doc.select("div.arranged-content div.el-card.arranged-course-card")

        // 用于将索引字母映射到课程名称
        val indexCourseMap = mutableMapOf<String, String>()
        for (card in cards) {
            val items = card.select("div.card-item.cv-clearfix")
            val dataMap = mutableMapOf<String, String>()

            for (item in items) {
                val label = item.selectFirst("div.label.cv-pull-left")?.text()?.trim()?.removeSuffix(":")
                val value = item.selectFirst("div.value.cv-pull-left")?.text()?.trim() ?: ""

                if (label != null) {
                    dataMap[label] = value
                }
            }
            val index = dataMap["#"] ?: ""
            val courseName = dataMap["课程名"] ?: ""
            val credit = dataMap["学分"]?.toFloatOrNull() ?: 0f
            val teacher = dataMap["上课教师"] ?: ""
            val classTime = dataMap["上课时间"] ?: ""
            val classRoom = dataMap["上课地点"] ?: ""

            // 保存索引和课程名称的映射
            indexCourseMap[index] = courseName

            // 解析上课时间，生成课程条目
            val scheduleEntries = parseClassTime(classTime)
            for (entry in scheduleEntries) {
                if (entry.weekType == 3) {
                    for (week in entry.weeks) {
                        val course = Course(
                            name = courseName,
                            room = classRoom,
                            teacher = teacher,
                            day = entry.day,
                            startNode = entry.startNode,
                            endNode = entry.endNode,
                            startWeek = week,
                            endWeek = week,
                            type = 0,
                            credit = credit,
                            note = ""
                        )
                        courseList.add(course)
                    }
                } else {
                    val course = Course(
                        name = courseName,
                        room = classRoom,
                        teacher = teacher,
                        day = entry.day,
                        startNode = entry.startNode,
                        endNode = entry.endNode,
                        startWeek = entry.weeks.first(),
                        endWeek = entry.weeks.last(),
                        type = entry.weekType,
                        credit = credit,
                        note = ""
                    )
                    courseList.add(course)
                }

            }
        }
        if (courseList.isEmpty()) { // 课程表格为空，尝试解析另一种电脑版UI格式
            println("课程表格为空，尝试解析另一种电脑版UI格式")
            val rows = doc.select("div.arranged-content table.el-table__body tbody tr.el-table__row")
            for (row in rows) {
                val cells = row.select("td")
                if (cells.size >= 11) {
                    val courseName = cells[1].text().trim()
                    val credit = cells[3].text().trim().toFloatOrNull() ?: 0f
                    val teacher = cells[5].text().trim()
                    val classTime = cells[6].text().trim()
                    val classRoom = cells[7].text().trim()
                    val scheduleEntries = parseClassTime(classTime)
                    for (entry in scheduleEntries) {
                        if (entry.weekType == 3) {
                            for (week in entry.weeks) {
                                val course = Course(
                                    name = courseName,
                                    room = classRoom,
                                    teacher = teacher,
                                    day = entry.day,
                                    startNode = entry.startNode,
                                    endNode = entry.endNode,
                                    startWeek = week,
                                    endWeek = week,
                                    type = 0,
                                    credit = credit,
                                    note = ""
                                )
                                courseList.add(course)
                            }
                        } else {
                            val course = Course(
                                name = courseName,
                                room = classRoom,
                                teacher = teacher,
                                day = entry.day,
                                startNode = entry.startNode,
                                endNode = entry.endNode,
                                startWeek = entry.weeks.first(),
                                endWeek = entry.weeks.last(),
                                type = entry.weekType,
                                credit = credit,
                                note = ""
                            )
                            courseList.add(course)
                        }

                    }
                }
            }
        }
        return courseList
    }

    override fun generateTimeTable(): TimeTable = timeTable
    override fun getNodes(): Int = nodeNum
    override fun getMaxWeek(): Int = maxWeek
    override fun getTableName(): String? = parseTermName(source)


    private fun parseTermName(source: String): String? {
        val doc: Document = Jsoup.parse(source)
        val termElement: Element? = doc.selectFirst("a.gologin-btn span.el-link--inner")
        val currentTerm = termElement?.ownText()?.trim()
        var formattedTerm: String? = null
        if (currentTerm != null) {
            // 使用正则表达式提取年份和季节信息
            val regex = Regex("(\\d{4})-(\\d{4})学年(\\S+)季学期")
            val matchResult = regex.find(currentTerm)

            if (matchResult != null) {
                val startYear = matchResult.groupValues[1].takeLast(2) // 提取开始年份的最后两位
                val endYear = matchResult.groupValues[2].takeLast(2) // 提取结束年份的最后两位
                val season = matchResult.groupValues[3] // 提取季节

                formattedTerm = "$startYear-$endYear$season"
            }

        }
        return formattedTerm
    }

    private fun parseClassTime(classTime: String): List<ScheduleEntry> {
        val entries = mutableListOf<ScheduleEntry>()

        val timeParts = classTime.split(" ")

        for (part in timeParts) {
            if (part.isBlank()) continue

            val weekPattern = "\\(([^)]*)\\)".toRegex()
            val weekMatch = weekPattern.find(part)
            val weekString = weekMatch?.groupValues?.get(1) ?: ""

            var weeks: List<Int>
            var weekType = 0 // 0: 全部周，1: 单周，2: 双周

            if (weekString.isNotBlank()) {
                weeks = parseWeeks(weekString)
            } else {
                weeks = (1..10).toList()
            }

            var timePart = part.replace(weekPattern, "")

            if (timePart.endsWith("单")) {
                weekType = 1
                timePart = timePart.substring(0, timePart.length - 1)
            } else if (timePart.endsWith("双")) {
                weekType = 2
                timePart = timePart.substring(0, timePart.length - 1)
            } else if (weeks.size != weeks.last() - weeks.first() + 1) {
                weekType = 3
            }


            val dayMap = mapOf(
                "一" to 1,
                "二" to 2,
                "三" to 3,
                "四" to 4,
                "五" to 5,
                "六" to 6,
                "日" to 7,
            )

            val dayPattern = "([一二三四五六日])(\\d+)-(\\d+)".toRegex()
            val dayMatch = dayPattern.find(timePart)
            if (dayMatch != null) {
                val day = dayMap[dayMatch.groupValues[1]] ?: 0
                val startNode = dayMatch.groupValues[2].toInt()
                val endNode = dayMatch.groupValues[3].toInt()

                // 根据weekType过滤周数
                weeks = when (weekType) {
                    1 -> weeks.filter { it % 2 == 1 }
                    2 -> weeks.filter { it % 2 == 0 }
                    else -> weeks
                }

                entries.add(
                    ScheduleEntry(
                        day = day, startNode = startNode, endNode = endNode, weeks = weeks, weekType = weekType
                    )
                )
            }
        }

        return entries
    }

    private fun parseWeeks(weekString: String): List<Int> {
        val weeks = mutableListOf<Int>()

        val weekParts = weekString.split(",")

        for (part in weekParts) {
            val rangePattern = "(\\d+)-(\\d+)周".toRegex()
            val singlePattern = "(\\d+)周".toRegex()
            val numberPattern = "(\\d+)".toRegex()

            val rangeMatch = rangePattern.find(part)
            val singleMatch = singlePattern.find(part)
            val numberMatch = numberPattern.find(part)

            if (rangeMatch != null) {
                val startWeek = rangeMatch.groupValues[1].toInt()
                val endWeek = rangeMatch.groupValues[2].toInt()
                weeks.addAll((startWeek..endWeek).toList())
            } else if (singleMatch != null) {
                val week = singleMatch.groupValues[1].toInt()
                weeks.add(week)
            } else if (numberMatch != null) {
                val week = numberMatch.groupValues[1].toInt()
                weeks.add(week)
            }
        }

        if (weeks.isEmpty()) {
            weeks.addAll(1..10)
        }

        return weeks
    }

    private val timeTable: TimeTable = TimeTable(
        "上海大学", listOf(
            TimeDetail(1, "08:00", "08:45"),
            TimeDetail(2, "08:55", "09:40"),
            TimeDetail(3, "10:00", "10:45"),
            TimeDetail(4, "10:55", "11:40"),
            TimeDetail(5, "13:00", "13:45"),
            TimeDetail(6, "13:55", "14:40"),
            TimeDetail(7, "15:00", "15:45"),
            TimeDetail(8, "15:55", "16:40"),
            TimeDetail(9, "18:00", "18:45"),
            TimeDetail(10, "18:55", "19:40"),
            TimeDetail(11, "20:00", "20:45"),
            TimeDetail(12, "20:55", "21:40"),
        )
    )

    data class ScheduleEntry(
        val day: Int,
        val startNode: Int,
        val endNode: Int,
        var weeks: List<Int>,
        val weekType: Int // 0: 全部周，1: 单周，2: 双周
    )

}