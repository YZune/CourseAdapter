package main.java.parser

import bean.Course
import main.java.bean.TimeDetail
import main.java.bean.TimeTable
import parser.Parser

/**
 * Date: 2024/03/01
 * 课表地址: https://byxk.buaa.edu.cn/xsxk/web/curriculum
 * 项目地址: https://github.com/PandZz/CourseAdapter
 * 作者: PandZz
 *
 * 北京航空航天大学-新本研教务
 * 目前仅适配了本科生课表且无单双周(作者本学期没发现单双周的课且不太清楚研究生课表有什么不同, 若有问题欢迎对作者提issue)
 * 进入课表的方法: 进入byxk.buaa.edu.cn, 登录后点击悬浮按钮->课表
 */

class BUAAParser(source: String) : Parser(source) {
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
        val doc = org.jsoup.Jsoup.parse(source)
        val curriculumTable = doc.getElementsByClass("curriculum-table")
        curriculumTable[0].getElementsByTag("tr").forEach {
            // 该html元素是课程表的格子, 可为空也可有一或多个课程信息
            val itemTds = it.getElementsByClass("itemTd")
            // 用0-6表示周一到周日, 注意最终结果中的day参数是1-7
            var dayIdx = -1

            for (itemTd in itemTds) {
                dayIdx = (dayIdx + 1) % 7
                itemTd.getElementsByClass("sjp-item").forEach {
                    val children = it.children()

                    val tempName = children[0].children().get(0).text()
                    val name = tempName.substring(0, tempName.indexOf("-")).trim()

                    val time = children[1].text().split(" ") // 13-16周 3-4节
                    val week = time[0].substringBeforeLast("周").split("-")
                    val startWeek = week[0].toInt()
                    val endWeek = week[1].toInt()
                    val node = time[1].substringBeforeLast("节").split("-")
                    val startNode = node[0].toInt()
                    val endNode = node[1].toInt()

                    val room = children[2].text()

                    val teacher = children[3].text()

                    result.add(
                        Course(
                            name = name,
                            day = dayIdx + 1,
                            room = room,
                            teacher = teacher,
                            startNode = startNode,
                            endNode = endNode,
                            startWeek = startWeek,
                            endWeek = endWeek,
                            type = 0
                        )
                    )
                }
            }
        }
        return result
    }
}