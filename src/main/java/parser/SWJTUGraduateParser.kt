package main.java.parser

import bean.Course
import main.java.bean.TimeDetail
import main.java.bean.TimeTable
import parser.Parser

/*
   Date: 2023/09/05
 Author: Zorua
Contact: zoruasama@qq.com
    URL: https://one.swjtu.edu.cn/new/swjtu/indexswjtu.html

欢迎使用西南交通大学研究生课表导入工具，应该只适用于研究生？
登录后,请打开培养服务的我的课表查询进行导入。
应西南交通大学的同学要求，帮助适配了一下这个

代码写得比较草率……

如有BUG可以发送邮件至zoruasama@qq.com反馈。
 */


class SWJTUGraduateParser(source: String) : Parser() {
    override fun getNodes(): Int = 11

    override fun getTableName(): String = "西南交通大学研究生"

    override fun generateTimeTable(): TimeTable {
        return TimeTable(
            name = "西南交通大学", timeList = listOf(
                TimeDetail(1, "08:00", "08:45"),
                TimeDetail(2, "09:50", "09:35"),
                TimeDetail(3, "09:50", "10:35"),
                TimeDetail(4, "10:40", "11:25"),
                TimeDetail(5, "11:30", "12:15"),
                TimeDetail(6, "14:00", "14:45"),
                TimeDetail(7, "14:50", "15:35"),
                TimeDetail(8, "15:50", "16:35"),
                TimeDetail(9, "16:40", "17:25"),
                TimeDetail(10, "17:30", "18:15"),
                TimeDetail(11, "19:30", "20:15"),
                TimeDetail(12, "20:20", "21:05"),
                TimeDetail(13, "21:10", "21:55"),
                TimeDetail(14, "22:00", "22:45")
            )
        )
    }
    override fun generateCourseList(): List<Course> {
        //使用jsoup解析源码
        val doc = org.jsoup.Jsoup.parse(source)
        //获取课程表的tabl
        val table = doc.getElementsByClass("mtt_table1")
        //
        val trs = table[0].getElementsByTag("tr")
        val result = arrayListOf<Course>()
        var weekdayNo: Int//代表星期几

        for (tr in trs) {
            val tds = tr.getElementsByTag("td")
            for (td in tds) {
                val courseSource = td.html().trim()
                if(!courseSource.contains("讲授")){
                    continue;
                }
                if (courseSource != "") {
                        val items = td.getElementsByClass("arrage")[0].getElementsByTag("div")
                        var startWeek: Int
                        var endWeek: Int
                        val startTime: Int
                        val endTime: Int
                        var className: String
                        var classRoom: String
                        var classTeacher: String


                        weekdayNo = Integer.parseInt(td.attr("xq"))
                        className = items[2].text().substringAfter("(").substringBefore(")").trim()
                        classTeacher = items[3].text()
                        classRoom = items[4].text()
                        println(className + classTeacher + classRoom)
                        //节
                        startTime = Integer.parseInt(td.attr("jc"))
                        endTime = startTime
                        //周
                        println("items[0].text() " + items[1].text())
                        val weekinfo = items[1].text().replace("[第周]".toRegex(), "")
                        if(weekinfo.contains("-")){
                            startWeek = Integer.parseInt(weekinfo.substringBefore("-").trim())
                            endWeek = Integer.parseInt(weekinfo.substringAfter("-").trim())
                        }else{
                            startWeek = Integer.parseInt(weekinfo.trim())
                            endWeek = startWeek
                        }

                        val type: Int = 0

                        if (className != "")
                            result.add(
                                Course(
                                    name = className,
                                    day = weekdayNo,
                                    room = classRoom,
                                    teacher = classTeacher,
                                    startNode = startTime,
                                    endNode = endTime,
                                    startWeek = startWeek,
                                    endWeek = endWeek,
                                    type = type,
                                )
                            )

                }

            }
        }
        return result
    }


}
