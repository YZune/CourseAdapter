package main.java.parser

import Common.TimeHM
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
编写了FindNode，因为有的课程安排跟时间表并不对齐. 如果开源版parser允许
startNode, startTime 二选一，烦请随之简化对应代码.

中国科学技术大学新研究生系统，在左侧 培养-课表查询应用-学生课表查询 中看课表.
看上去并不那么直观，但降低了机器处理的难度.

给两个脱敏后的例子.
课堂号,课堂名称,起止周,教师,上课时间地点
E***2	***)	2~6(双),7~15;3~5(单)	***	G***1: *(11,12,13);G***1: *(11,12,13)
P***5	***理	2~3,6~10;6	***	G***8: 5(18:40~21:55);G***8: *(18:40~21:55)
 */


class USTCGraduateParser(source: String) : Parser(source) {
    override fun getNodes(): Int = generateTimeTable().timeList.size

    override fun getTableName(): String = "中国科学技术大学研究生"

    override fun generateTimeTable(): TimeTable {
        val ttbName = "中国科学技术大学研究生"
        val ttbList = arrayListOf<TimeDetail>()
        var seq = 0
        listOf("7:50","8:40","9:45","10:35","11:25",
            "14:00","14:50","15:55","16:45","17:35",
            "19:30","20:20","21:10").forEach {
                var h = TimeHM(it)
                ttbList.add(TimeDetail(node = ++seq, startTime = h.toString(),
                    endTime = (h+45).toString()))
        }
        return TimeTable(name = ttbName, timeList = ttbList.toList())
    }

    override fun generateCourseList(): List<Course> {
        val timeList = generateTimeTable().timeList.map { listOf(it.startTime, it.endTime) }.toList()
        var retList = arrayListOf<Int>()

        fun FindNode(_t1: String, _t2: String): List<Int> {
            val t1 = TimeHM(_t1)
            val t2 = TimeHM(_t2)
            var retList = arrayListOf<Int>()
            for (i in timeList.indices) {
                if (retList.isEmpty() && t1.timeCmp(timeList[i][1]) < 0) retList.add(i + 1)
                else if (t2.timeCmp(timeList[i][0]) < 0) retList.add(i)
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
            if (tds.lastIndex<8) {
                System.err.println("W: 下列行 单元格数不足 (${tds.size}/9)，若对应了请报告bug\n${tr.outerHtml()}\n")
                continue
            }
            // 1,5,6,7,8
            // 课堂号,课堂名称,起止周,教师,上课时间地点
            val _tds = tds.map<Element, String> {
                try {
                    it.select("span").first().text()
                } catch (e: java.lang.NullPointerException) {
                    it.html()
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
                    // 上课时间与上课铃不对齐
                    val time0 = items[2].split("~").toTypedArray<String>()
                    time0.forEachIndexed { ind, it -> time0[ind] = TimeHM(it).toString() }
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
                        // 区分单双周
                        val weeks2 = weeks1[j].split("(")
                        endWeek = weeks2[0].toInt()
                        val isSingle = weeks2[1].startsWith("单")
                        if ((startWeek and 1) != (if (isSingle) 1 else 0))
                            startWeek += 1
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