package main.java.parser

import bean.Course
import org.jsoup.Jsoup
import parser.Parser
import main.java.bean.TimeTable
// 移动导入语句到文件开头
import org.jsoup.nodes.Document

// 假设 TimeDetail 数据类定义如下
data class TimeDetail(val startTime: String, val endTime: String)

class YZZYParser(source: String) : Parser(source) {
    private val MAX_NODES_PER_DAY = 13 // 定义每天最大课程节数

    // 存储课程时间信息，使用 TimeDetail
    private val timeDetails = listOf(
        TimeDetail("08:00", "08:40"),
        TimeDetail("08:50", "09:30"),
        TimeDetail("09:40", "10:20"),
        TimeDetail("10:30", "11:10"),
        TimeDetail("11:20", "12:00"),
        TimeDetail("12:50", "13:30"),
        TimeDetail("13:40", "14:20"),
        TimeDetail("14:30", "15:10"),
        TimeDetail("15:20", "16:00"),
        TimeDetail("16:10", "16:50"),
        TimeDetail("17:00", "17:40"),
        TimeDetail("19:30", "20:10"),
        TimeDetail("20:20", "21:00")
    )

    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()
        val doc: Document = Jsoup.parse(source)
        val table = doc.getElementById("timetable")
        val rows = table?.select("tr") ?: return emptyList()

        // 跳过表头行
        for (i in 1 until rows.size) {
            if (i > MAX_NODES_PER_DAY) break // 超过每天最大节数则停止处理
            val row = rows[i]
            val startNode = i
            val cells = row.select("td")

            for (j in cells.indices) {
                val cell = cells[j]
                val day = j + 1
                val courseDivs = cell.select(".kbcontent")

                for (courseDiv in courseDivs) {
                    if (courseDiv.isBlock) {
                        val nameElement = courseDiv.selectFirst("font[onmouseover=kbtc(this)]")
                        val name = nameElement?.text() ?: ""

                        val weekElement = courseDiv.selectFirst("font[title=\"周次(节次)\"]")
                        val weekText = weekElement?.text() ?: ""
                        val (startWeek, endWeek) = parseWeeks(weekText)

                        val roomElement = courseDiv.selectFirst("font[title=\"教室\"]")
                        val room = roomElement?.text() ?: ""

                        val teacherElement = courseDiv.selectFirst("font[title=\"教师\"]")
                        val teacher = teacherElement?.text() ?: ""

                        val course = Course(
                            name = name,
                            room = room,
                            teacher = teacher,
                            day = day,
                            startNode = startNode,
                            endNode = startNode,
                            startWeek = startWeek,
                            endWeek = endWeek,
                            type = 0,
                            credit = 0f,
                            note = ""
                        )
                        courseList.add(course)
                    }
                }
            }
        }

   

        
        return mergeAdjacentCourses(courseList)
    }  

    private fun mergeAdjacentCourses(courses: List<Course>): List<Course> {
        val sorted = courses.sortedWith(compareBy(
            { it.day },
            { it.startNode }
        ))
        val merged = mutableListOf<Course>()
        
        sorted.forEach { current ->
            val last = merged.lastOrNull()
            if (last != null && 
                last.day == current.day &&
                last.name == current.name &&
                last.endNode + 1 == current.startNode &&
                last.startWeek == current.startWeek &&
                last.endWeek == current.endWeek) {
                
                // 合并时考虑教室和教师信息是否一致
                val newRoom = if (last.room == current.room) last.room else "${last.room}, ${current.room}"
                val newTeacher = if (last.teacher == current.teacher) last.teacher else "${last.teacher}, ${current.teacher}"
                
                merged[merged.lastIndex] = last.copy(
                    endNode = current.endNode,
                    room = newRoom,
                    teacher = newTeacher
                )
            } else {
                merged.add(current)
            }
        }
        return merged
    }

    private fun parseWeeks(weekText: String): Pair<Int, Int> {
        val regex = Regex("(\\d+)-(\\d+)\\(周\\)")
        val matchResult = regex.find(weekText)
        if (matchResult != null) {
            val startWeek = matchResult.groupValues[1].toInt()
            val endWeek = matchResult.groupValues[2].toInt()
            return Pair(startWeek, endWeek)
        }
        return Pair(0, 0)
    }

    // 将顶层的 override 函数移动到类内部
    override fun generateTimeTable(): TimeTable? {
        val courseList = generateCourseList()
        val courseListString = courseList.joinToString { it.name }
        
        val beanTimeDetails = timeDetails.mapIndexed { index, it -> 
            main.java.bean.TimeDetail(
                startTime = it.startTime,  // 保持原始字符串格式
                endTime = it.endTime,      // 保持原始字符串格式
                node = index + 1           // 添加节点参数
            ) 
        }
        
        return TimeTable(courseListString, beanTimeDetails)
    }

  

    override fun getTableName(): String? {
        return "强智教务系统"
    }

    // 补全其他需要实现的抽象方法
    override fun getNodes(): Int? = super.getNodes()
    override fun getStartDate(): String? = super.getStartDate()
    override fun getMaxWeek(): Int? = super.getMaxWeek()
}

class YZZYParserInnerClass {
    // 这里的 override 报错，因为没有父类方法可以重写，需要移除或者添加合适的父类
    // 暂时移除 override
    fun someFunction() {
        // 函数内容
    }
}

