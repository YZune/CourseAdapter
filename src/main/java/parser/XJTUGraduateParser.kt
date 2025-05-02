package main.java.parser

import bean.Course
import main.java.bean.TimeDetail
import main.java.bean.TimeTable
import parser.Parser
import java.io.File

/*

   Date: 2022/09/18
 Author: Zorua
Contact: zoruasama@qq.com
    URL: http://gmis.xjtu.edu.cn/pyxx/pygl/xskbcx

欢迎使用西安交通大学研究生课表导入工具，应该只适用于研究生！登录后,请打开培养信息查询中的我的课表进行导入。
由于本人不是很懂Kotlin，因此代码很大程度参考了已有的代码文件，代码好像是能跑的……
但是如果后续这段代码失效了，希望能有学弟/学妹接力更新这段代码（居然留下这么烂的代码让你们看难为你们了……
如有BUG可以发送邮件至zoruasama@qq.com反馈。
 */


class XJTUGraduateParser(source: String) : Parser() {
    override fun getNodes(): Int = 11

    override fun getTableName(): String = "西安交通大学研究生"

    override fun generateTimeTable(): TimeTable {
        return TimeTable(
            name = "西安交通大学", timeList = listOf(
                TimeDetail(1, "08:00", "08:50"),
                TimeDetail(2, "09:00", "09:50"),
                TimeDetail(3, "10:10", "11:00"),
                TimeDetail(4, "11:10", "12:00"),
                TimeDetail(5, "14:30", "15:20"),
                TimeDetail(6, "15:30", "16:20"),
                TimeDetail(7, "16:40", "17:30"),
                TimeDetail(8, "17:40", "18:30"),
                TimeDetail(9, "19:40", "20:30"),
                TimeDetail(10, "20:40", "21:30"),
                TimeDetail(11, "21:40", "22:30")
            )
        )
    }
    override fun generateCourseList(): List<Course> {
        //使用jsoup解析源码
        val doc = org.jsoup.Jsoup.parse(source)
        //获取课程表的tabl
        val table = doc.getElementsByClass("GridViewStyle")
        //
        val trs = table[0].getElementsByTag("tr")
        val result = arrayListOf<Course>()
        var weekdayNo: Int//代表星期几

        for (tr in trs) {
            val tds = tr.getElementsByTag("td")
            for (td in tds) {
                val courseSource = td.html().trim()
                if(!courseSource.contains("课程")){
                    continue;
                }
                if (courseSource != "") {
                    val courseLine = courseSource.split("<br><br>")
                    for (each in courseLine) {
                        val items = each.split("<br>")
                        var startWeek: Int
                        var endWeek: Int
                        val startTime: Int
                        val endTime: Int
                        var className: String
                        var classRoom: String
                        var classTeacher: String


                        weekdayNo = Integer.parseInt(td.id().substring(3,4))
                        className = items[0].substringAfter("：").trim() + items[1].substringAfter("：").trim()
                        classTeacher = items[2].substringAfter("：").trim()
                        classRoom = items[3].substringAfter("：").trim()
                        //节
                        if(items[4].contains("-")){
                            val lr = items[4].substringAfter("：").trim()
                            startTime = Integer.parseInt(lr.substringBefore("-"))
                            endTime = Integer.parseInt(lr.substringAfter("-"))
                        }else{
                            startTime = Integer.parseInt(items[4].substringAfter("：").trim())
                            endTime = startTime
                        }
                        //周
                        val weekinfo = items[5].replace("[第周]".toRegex(), "")
                        if(weekinfo.contains("-")){
                            val lr = weekinfo.substringAfter("：").trim()
                            startWeek = Integer.parseInt(lr.substringBefore("-"))
                            endWeek = Integer.parseInt(lr.substringAfter("-"))
                        }else{
                            startWeek = Integer.parseInt(weekinfo.substringAfter("：").trim())
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
        }
        return result
    }


}