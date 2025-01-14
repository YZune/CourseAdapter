package parser.wakeup

import bean.Course
import main.java.bean.TimeDetail 
import main.java.bean.TimeTable  
import org.jsoup.Jsoup
import parser.Parser

/**
 * 广西工业职业技术学院教务系统（仅支持内网访问）
 * 内网先登录地址：http://jw.gxic.net
 * 登录完成后进入地址然后执行数据采集：http://jw.gxic.net/JWXS/pkgl/XsKB_List.aspx
 *
 * @author JiuXia2025
 * @version 1.0
 * @date 2025.01.14
 * 如有BUG问题请联系我：https://github.com/JiuXia2025
 */
class WakeupParser(source: String) : Parser(source) {

    private val sundayFirstDayMap = arrayOf(0, 7, 1, 2, 3, 4, 5, 6)
    private var sundayFirst = false

    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()
        val doc = Jsoup.parse(source)
        val kbtable = doc.select("table[width='98%']").first() 
        val trs = kbtable.select("tr") 

        
        try {
            val ths = kbtable.select("th")
            sundayFirst = ths.indexOfFirst { it.text().contains("星期天") } < ths.indexOfFirst { it.text().contains("星期一") }
        } catch (e: Exception) {
            
        }

        
        val nodeMap = mapOf(
            "第0102节" to 1, // 1-2 节
            "第0304节" to 3, // 3-4 节
            "第0506节" to 5, // 5-6 节
            "第0708节" to 7, // 7-8 节
            "第0910节" to 9  // 9-10 节
        )

        for ((rowIndex, tr) in trs.withIndex()) {
            val tds = tr.select("td") 
            if (tds.isEmpty()) continue 

            
            val nodeText = tds[0].text().trim()
            val startNode = nodeMap[nodeText] ?: continue 

            for ((colIndex, td) in tds.withIndex()) {
                if (colIndex == 0) continue 

                val courseElements = td.select("a") 
                if (courseElements.isEmpty()) continue 

                for (courseElement in courseElements) {
                    val courseHtml = courseElement.attr("title") 
                    if (courseHtml.isBlank()) continue

                    val courseName = courseElement.select("br").first()?.previousSibling()?.toString()?.trim() ?: ""
                    val teacher = courseHtml.substringAfter("授课教师：").substringBefore("\n").trim()
                    val room = courseHtml.substringAfter("开课地点：").substringBefore("\n").trim()
                    val weekStr = courseHtml.substringAfter("上课周次：").substringBefore("\n").trim()

                    val weekList = weekStr.split(',')
                    var startWeek = 0
                    var endWeek = 0
                    var type = 0
                    weekList.forEach {
                        if (it.contains('-')) {
                            val weeks = it.split('-')
                            if (weeks.isNotEmpty()) {
                                startWeek = weeks[0].toInt()
                            }
                            if (weeks.size > 1) {
                                type = when {
                                    weeks[1].contains('单') -> 1
                                    weeks[1].contains('双') -> 2
                                    else -> 0
                                }
                                endWeek = weeks[1].substringBefore('(').toInt()
                            }
                        } else {
                            startWeek = it.substringBefore('(').toInt()
                            endWeek = it.substringBefore('(').toInt()
                        }
                        courseList.add(
                            Course(
                                name = courseName,
                                room = room,
                                teacher = teacher,
                                day = colIndex, 
                                startNode = startNode,
                                endNode = startNode + 1, 
                                startWeek = startWeek,
                                endWeek = endWeek,
                                type = type
                            )
                        )
                    }
                }
            }
        }
        return courseList
    }

    override fun generateTimeTable(): TimeTable {
        return buildTimeTable("广西工职院武鸣三校区") {
            //默认武鸣三校区
            //早上
            add("08:40", "09:20")
            add("09:30", "10:10")
            add("10:30", "11:10")
            add("11:20", "12:00")
            //下午
            add("14:30", "15:10")
            add("15:20", "16:00")
            add("16:10", "16:50")
            add("17:00", "17:40")
            //晚自习
            add("19:40", "20:20")
            add("20:30", "21:10")
        }
    }

    
    private fun buildTimeTable(name: String, block: TimeTableBuilder.() -> Unit): TimeTable {
        val builder = TimeTableBuilder(name)
        builder.block()
        return builder.build()
    }

    
    private class TimeTableBuilder(private val name: String) {
        private val timeList = mutableListOf<TimeDetail>()
        private var nodeCounter = 1

        fun add(startTime: String, endTime: String) {
            timeList.add(TimeDetail(nodeCounter++, startTime, endTime))
        }

        fun build(): TimeTable {
            return TimeTable(name, timeList)
        }
    }
}