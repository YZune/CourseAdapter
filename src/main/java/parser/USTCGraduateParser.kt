package main.java.parser

import bean.Course
import main.java.bean.TimeDetail
import main.java.bean.TimeTable
import org.jsoup.nodes.Element
import parser.Parser

/*
   Date: 2023/10/22
    URL: https://yjs1.ustc.edu.cn
    URL: https://www.teach.ustc.edu.cn/calendar/16868.html

初代适配者第一次写Java程序，效率、corner case适配等问题在所难免，敬请后来者积极改进.
编写了FindNode，因为有的课程安排跟时间表并不对齐. 如果WakeUp课程表开发者允许
startNode, startTime 二选一，烦请随之简化对应代码.

中国科学技术大学新研究生系统，在左侧 培养-课表查询应用-学生课表查询 中看课表.
看上去并不那么直观，但降低了机器处理的难度.

给两个脱敏后的例子.
课堂号,课堂名称,起止周,教师,上课时间地点
E***2	***)	2~6(双),7~15;3~5(单)	***	G***1: *(11,12,13);G***1: *(11,12,13)
P***5	***理	2~3,6~10;6	***	G***8: 5(18:40~21:55);G***8: *(18:40~21:55)
 */


class USTCGraduateParser(source: String) : Parser(source) {
    override fun getNodes(): Int = 13

    override fun getTableName(): String = "中国科学技术大学研究生"

    override fun generateTimeTable(): TimeTable {
        return TimeTable(
            name = "中国科学技术大学研究生", timeList = listOf(
                TimeDetail(1, "07:50", "08:35"),
                TimeDetail(2, "08:40", "09:25"),
                TimeDetail(3, "09:45", "10:30"),
                TimeDetail(4, "10:35", "11:20"),
                TimeDetail(5, "11:25", "12:10"),
                TimeDetail(6, "14:00", "14:45"),
                TimeDetail(7, "14:50", "15:35"),
                TimeDetail(8, "15:55", "16:40"),
                TimeDetail(9, "16:45", "17:30"),
                TimeDetail(10, "17:35", "18:20"),
                // 部分课程并不严格对齐“上课时间表”，此时教务系统给出具体时间
                TimeDetail(11, "19:30", "20:15"),
                TimeDetail(12, "20:20", "21:05"),
                TimeDetail(13, "21:10", "21:55")
            )
        )
    }

    override fun generateCourseList(): List<Course> {
        val timeList = generateTimeTable().timeList.map { listOf(it.startTime, it.endTime) }.toList()
        var retList = arrayListOf<Int>()

        fun FindNode(t1: String, t2: String): List<Int> {
            fun TimeCmp(t1: String, t2: String): Int {
                var t_list = arrayListOf<Int>()
                t_list.addAll(t1.split(":").map { it.toInt() }.toList())
                t_list.addAll(t2.split(":").map { it.toInt() }.toList())
                if (t_list[0] > t_list[2]) return 1
                else if (t_list[0] == t_list[2]) {
                    if (t_list[1] > t_list[3]) return 1
                    else if (t_list[1] == t_list[3]) return 0
                    else return -1
                } else return -1
            }

            var retList = arrayListOf<Int>()
            for (i in timeList.indices) {
                if (retList.isEmpty() && TimeCmp(t1, timeList[i][1]) < 0) retList.add(i + 1)
                else if (TimeCmp(t2, timeList[i][0]) < 0) retList.add(i)
            }
            val i = timeList.lastIndex + 1
            while (retList.lastIndex <= 2) retList.add(i)
            return retList.toList()
        }

        //使用jsoup解析源码
        val doc = org.jsoup.Jsoup.parse(source)
        // 实际课表藏在iframe内，其具有唯一id
        val _iframe = doc.select("iframe#iframeContent_kbcxappustcxskbcx").first().attr("srcdoc")
        val table = org.jsoup.Jsoup.parse(_iframe).select("table").first()

        val trs = table.getElementsByTag("tr")
        val result = arrayListOf<Course>()

        // table没有表头，第一行就是课程
        for (tr in trs) {
            val tds = tr.getElementsByTag("td")
            // 1,5,6,7,8
            // 课堂号,课堂名称,起止周,教师,上课时间地点
            val _tds = tds.map<Element, String> {
                try {
                    it.select("span").first().text()
                } catch (e: java.lang.NullPointerException) {
                    ""
                }
            }
            val className = _tds[5]
            val note = _tds[1]      // 课堂号
            val teacher = _tds[7]

            val weeks = _tds[6].split(";")
            val periods = _tds[8].split(";")
            for (i in periods.indices) {
                val period = periods[i]
                val items = period.split(": ", "(", ",", ")")
                val classroom = items[0]
                val weekday = items[1].toInt()
                var startNode = 0
                var endNode = 0
                var startTime = ""
                var endTime = ""
                try {
                    startNode = items[2].toInt()
                    endNode = items[items.lastIndex - 1].toInt()
                } catch (e: java.lang.NumberFormatException) {
                    val time0 = items[2].split("~")
                    startTime = time0[0]
                    endTime = time0[1]
                    // 寻找最合适的 node
                    val foundNodes = FindNode(startTime, endTime)
                    startNode = foundNodes[0]
                    endNode = foundNodes[1]
                }

                val weeks0 = weeks[i].split(",")
                for (week in weeks0) {
                    var weeksArr = arrayListOf<Int>()
                    val weeks1 = week.split("~")
                    var startWeek = weeks1[0].toInt()
                    var endWeek: Int
                    var j = weeks1.lastIndex
                    if (weeks1[j] == "") {
                        j -= 1
                    }
                    try {
                        endWeek = weeks1[j].toInt()
                        weeksArr.add(startWeek)
                        weeksArr.add(endWeek)
                    } catch (e: java.lang.NumberFormatException) {
                        val weeks2 = weeks1[j].split("(")
                        endWeek = weeks2[0].toInt()
                        val isSingle = weeks2[1].startsWith("单")
                        if ((startWeek and 1) != (if (isSingle) {
                                1
                            } else {
                                2
                            })
                        ) {
                            startWeek += 1
                        }
                        for (i in startWeek..endWeek step 2) {
                            weeksArr.add(i)
                            weeksArr.add(i)
                        }
                    }

                    for (i in weeksArr.indices step 2) result.add(
                        Course(
                            name = className,
                            day = weekday,
                            room = classroom,
                            teacher = teacher,
                            startNode = startNode,
                            endNode = endNode,
                            startTime = startTime,
                            endTime = endTime,
                            startWeek = weeksArr[i],
                            endWeek = weeksArr[i + 1],
                            note = note,
                            type = 0
                        )
                    )
                }

            }
        }
        return result
    }


}