package main.java.parser

import bean.Course
import org.jsoup.Jsoup
import parser.Parser
/**
 *
 * 适配长春师范大学本科教务系统
 * 在主页-->全校课表-->选择自己的专业-->查询本学期课表-->导入
 *
 * @author xiangxiang
 * @version 1.0
 * @date 2023-08-28
 */
class CNUParser(source: String) : Parser(source) {

    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()
        val doc = Jsoup.parse(source)
        val trs = doc.getElementsByTag("tbody")[0].getElementsByTag("tr")
        trs.forEach { tr ->
            val tds = tr.getElementsByTag("td")
            var count = if (tds.size == 8) -1 else -2
            tds.forEach { td ->
                count++
                if (!td.getElementsByClass("curriculum-item").isEmpty()) {
                    val courseName = td.getElementsByTag("span").text().split(" 查看更多")[0]
                    val data = td.getElementsByTag("div")
                    val time = data[3].text()
                    val weekNode = time.split('周')
                    val startWeek= weekNode[0].split('-')[0].toInt()
                    val endWeek = weekNode[0].split('-')[1].toInt()
//                  val nodeTimeString = weekNode[1].substring(1, weekNode[1].length-2).split('-')
                    val nodeTimeString = weekNode[1].split('节')[0]
                    val nodeTime = nodeTimeString.substring(1, nodeTimeString.length).split('-')
                    val startNode = nodeTime[0].toInt()
                    val endNode = nodeTime[1].toInt()

                    courseList.add(
                        Course(
                            name = courseName,
                            day = count,
                            room = data[4].text(),
                            teacher = data[2].text(),
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
        return courseList
    }
}