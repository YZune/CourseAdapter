package main.java.parser

import Common.getDayInt
import bean.Course
import org.jsoup.Jsoup
import parser.Parser
import java.io.IOException

/**
 * Created by [Xeu](https://github.com/ThankRain) at 2021/8/12 14:50
 *
 * Fixed by [GoForceX](https://github.com/GoForceX) at 2024/12/1 17:30
 *
 * 华中科技大学微校园
 * @link https://mhub.hust.edu.cn/kbPageController/by-course
 */
class HUSTParser(source: String) : Parser() {
    override fun generateCourseList(): List<Course> {
        val doc = Jsoup.parse(source)
        val list = doc.getElementsByClass("main-page-block").first() // Get Course List
        val courses: MutableList<Course> = mutableListOf()
        list.children().toList().forEach { it ->
            val raws = it.getElementsByTag("p")
            if (raws.size < 3) {
                throw IOException("课表格式有误:段落不足")
            }
            val courseName = raws[0].text()

            val courseObject = raws[2].text()// 开课对象
            val timeArranges = it.getElementsByClass("search-details-body")
            timeArranges.forEach { timeArrange ->
                timeArrange.children().forEach { it ->
                    val texts = it.getElementsByClass("text-box")
                    val time = texts[0].text().run {
                        val start = "时间:"
                        substring(indexOf(start) + start.length).split(" ")
                    }
                    val weeks = time[0]// 周次
                    val day = time[1]// 星期
                    val times = time[2]// 节次

                    val teacher = texts[1].text().run {
                        val start = "教师:"
                        substring(indexOf(start) + start.length)
                    }

                    val place = texts[2].text().run {
                        val start = "教室:"
                        substring(indexOf(start) + start.length)
                    }

                    val startWeek = weeks.run { substring(0, indexOf("-")) }.toInt()
                    val endWeek = weeks.run { substring(indexOf("-") + 1, indexOf("周")) }.toInt()
                    val startNode = times.run { substring(0, indexOf("-")) }.toInt()
                    val endNode = times.run { substring(indexOf("-") + 1, indexOf("节")) }.toInt()
                    courses.add(
                        Course(
                            name = courseName,
                            day = getDayInt(day),
                            room = place,
                            teacher = teacher,
                            startNode = startNode,
                            endNode = endNode,
                            startWeek = startWeek,
                            endWeek = endWeek,
                            type = 0,
                            note = "开课对象：$courseObject",
                        )
                    )
                }
            }
        }
        return courses
    }
}