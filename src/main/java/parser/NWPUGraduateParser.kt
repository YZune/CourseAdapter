package main.java.parser

import bean.Course
import main.java.bean.TimeDetail
import main.java.bean.TimeTable
import org.jsoup.nodes.Element
import parser.Parser
import java.time.LocalDate

/**
 *   Date: 2023/09/11
 * Author: @ludoux
 */

/**
 * 由于无法直接登录研究生教务系统，需要先从翱翔门户（https://ecampus.nwpu.edu.cn/main.html#/Index）登录
 * 后进入【研究生教育】应用，自动打开研究生教务系统主页（https://yjsjy.nwpu.edu.cn/pyxx/home/index）
 * 之后再依次选择【课程与成绩】->【选课结果查询】，跳转至最终页面 https://yjsjy.nwpu.edu.cn/pyxx/pygl/xkjg/index
 * source 即为上述【选课结果查询】最终页面的网页源代码
 */
class NWPUGraduateParser(source: String) : Parser(source) {
    var changanCount = 0
    var youyiCount = 0
    var currentSemesterText = "1970-1971秋"

    override fun getNodes(): Int = 13

    override fun getMaxWeek(): Int = 22

    override fun getTableName(): String = "西工大研 $currentSemesterText"

    override fun generateTimeTable(): TimeTable {
        val changanTimeList = TimeTable(
            name = "西工大长安", timeList = listOf(
                TimeDetail(1, "08:30", "09:15"),
                TimeDetail(2, "09:25", "10:10"),
                TimeDetail(3, "10:30", "11:15"),
                TimeDetail(4, "11:25", "12:10"),
                TimeDetail(5, "12:20", "13:05"),
                TimeDetail(6, "13:05", "13:50"),
                TimeDetail(7, "14:00", "14:45"),
                TimeDetail(8, "14:55", "15:40"),
                TimeDetail(9, "16:00", "16:45"),
                TimeDetail(10, "16:55", "17:40"),
                TimeDetail(11, "19:00", "19:45"),
                TimeDetail(12, "19:55", "20:40"),
                TimeDetail(13, "20:40", "21:25")
            )
        )
        val youyiWinterTimeList = TimeTable(
            name = "西工大友谊冬(10.1-4.30)", timeList = listOf(
                TimeDetail(1, "08:00", "08:50"),
                TimeDetail(2, "09:00", "09:50"),
                TimeDetail(3, "10:10", "11:00"),
                TimeDetail(4, "11:10", "12:00"),
                TimeDetail(5, "12:10", "13:00"),//友谊本无中午，为了通用而已
                TimeDetail(6, "13:10", "14:00"),//友谊本无中午，为了通用而已
                TimeDetail(7, "14:00", "14:50"),
                TimeDetail(8, "15:00", "15:50"),
                TimeDetail(9, "16:10", "17:00"),
                TimeDetail(10, "17:10", "18:00"),
                TimeDetail(11, "19:00", "19:50"),
                TimeDetail(12, "20:00", "20:50"),
                TimeDetail(13, "20:50", "21:25")//友谊本无13节课，为了通用而已
            )
        )
        val youyiSummerTimeList = TimeTable(
            name = "西工大友谊夏(5.1-9.30)", timeList = listOf(
                TimeDetail(1, "08:00", "08:50"),
                TimeDetail(2, "09:00", "09:50"),
                TimeDetail(3, "10:10", "11:00"),
                TimeDetail(4, "11:10", "12:00"),
                TimeDetail(5, "12:10", "13:10"),//友谊本无中午，为了通用而已
                TimeDetail(6, "13:20", "14:20"),//友谊本无中午，为了通用而已
                TimeDetail(7, "14:30", "15:20"),
                TimeDetail(8, "15:30", "16:20"),
                TimeDetail(9, "16:40", "17:30"),
                TimeDetail(10, "17:40", "18:30"),
                TimeDetail(11, "19:30", "20:20"),
                TimeDetail(12, "20:30", "21:20"),
                TimeDetail(13, "20:40", "21:25")//友谊本无13节课，为了通用而已
            )
        )
        val date = LocalDate.now()
        return if (changanCount >= youyiCount) {
            changanTimeList
        } else if (date.monthValue >= 5 && date.monthValue <= 9) {
            youyiSummerTimeList
        } else {
            youyiWinterTimeList
        }
    }

    override fun generateCourseList(): List<Course> {
        val rt = arrayListOf<Course>()

        val doc = org.jsoup.Jsoup.parse(source)
        doc.outputSettings().indentAmount(0).prettyPrint(false);
        currentSemesterText = doc.getElementById("xq").getElementsByTag("option").filter { it.hasAttr("selected") }[0].text()
        val table = doc.getElementById("sample-table-1")
        val headThs = table.getElementsByTag("thead")[0].getElementsByTag("tr")[0].getElementsByTag("th")
        val bodyTrs = table.getElementsByTag("tbody")[0].getElementsByTag("tr")
        for (tr in bodyTrs) {
            var noteCollege = ""
            var noteCourseId = ""
            var noteDesc = ""
            var nameCourseName = ""
            var nameClassName = ""
            var teacher = ""
            var credit = 0.0f

            val tds = tr.getElementsByTag("td")
            var detailedTimeText = ""
            for ((i,td) in tds.withIndex()) {
                // 使用以下方式，使得在导出时候，br作为换行符
                val brs = td.select("br")
                for (br in brs) {
                    br.replaceWith(Element("br").text("\n"))
                }

                val curText = td.wholeText().trim()
                val curHeadTxt = headThs[i].wholeText().trim()
                if (curHeadTxt.contains("院系")) {
                    noteCollege = curText
                } else if (curHeadTxt.contains("课程编号")) {
                    noteCourseId = curText
                } else if (curHeadTxt.contains("课程名称")) {
                    nameCourseName = curText
                } else if (curHeadTxt.contains("班级名称")) {
                    nameClassName = curText
                } else if (curHeadTxt.contains("主讲教师")) {
                    teacher = curText
                } else if (curHeadTxt.contains("学分")) {
                    credit = curText.toFloat()
                } else if (curHeadTxt.contains("班级说明")) {
                    noteDesc = curText
                } else if (curHeadTxt.contains("上课时间")) {
                    // 在其它列都判断完后，最后再分析此列
                    detailedTimeText = curText
                }
            }
            //开始分析
            if (detailedTimeText.contains("长安")) {
                changanCount++
            } else if (detailedTimeText.contains("友谊")) {
                youyiCount++
            }
            /**
             * 目前来看只有单周和连续周，且时间一定是在括号内。可能有多行，同行也可能有多个星期
             * (第6-6周 单周 星期日 上1,上2,上3,上4,下1,下2,下3,下4)
             * (第6-6周 连续周 星期日 晚1,晚2,晚3)
             * (第1-3周 单周 星期日 上1,上2,上3,上4,下1,下2,下3,下4)
             *
             * (第4-4周 单周 星期二 晚1,晚2,晚3)
             *
             * (第3-3周 单周 星期六 上1,上2,上3,上4,下1,下2,下3,下4,晚1,晚2,晚3 星期日 上1,上2,上3,上4,下1,下2,下3,下4,晚1,晚2,晚3)
             *
             * 如下是有地点的。也有没有地点的，比如上面的那些。也需要注意同行可能有多个星期
             * 长安-教学东楼D座-教东D1-203(第2-13周 连续周 星期三上3,上4<10:30-12:10> 星期五下1,下2<14:00-15:40>)
             *
             * 长安-教学西楼D座-D101(第3-12周 连续周 星期五晚1,晚2,晚3<19:00-21:25>)
             * 长安-教学西楼D座-D101(第13-13周 单周 星期五晚1,晚2<19:00-20:40>)
             */
            for (line in detailedTimeText.split('\n')) {
                val lineTrimmed = line.trim()

                var room = "无地点"
                var startWeek: Int
                var endWeek: Int
                var type: Int
                if (!lineTrimmed.startsWith("(")) {
                    //通常情况，有地点，如 长安-教学东楼D座-教东D1-203(第2-13周 连续周 星期三上3,上4<10:30-12:10> 星期五下1,下2<14:00-15:40>)
                    room = lineTrimmed.split("(")[0].replace("-"," ")
                }
                // 括号内的文本
                val timeText = lineTrimmed.substringAfter("(").substringBefore(")")
                val weekRegex = """第(\d+)-(\d+)周""".toRegex()
                val weekFound = weekRegex.find(timeText)
                startWeek = weekFound?.groupValues?.get(1)?.toInt() ?: 0
                endWeek = weekFound?.groupValues?.get(2)?.toInt() ?: 0
                type = if (startWeek == endWeek) {/*防止wakeup处理异常，强制把仅一周的课设定为连续周*/
                    0
                } else if (timeText.contains("单周")) {
                    1
                } else if (timeText.contains("双周")) {
                    2
                } else {/*默认为连续周*/
                    0
                }
                // 考虑多 “星期”
                val dayNodeRegex = """星期.+?(?=星期|${'$'})""".toRegex()
                val dayNodeFound = dayNodeRegex.findAll(timeText)
                dayNodeFound.forEach {
                    val dayNodeText = it.value.trim() // 例如 星期六 上1,上2,上3,上4,下1,下2,下3,下4,晚1,晚2,晚3
                    val courseDay = Common.otherHeader.indexOf(dayNodeText.substring(0,3))
                    val nodePrefixToNode = mapOf("上" to 0, "中" to 4, "下" to 6, "晚" to 10)
                    val nodeTextRegex = """[上中下晚]\d+""".toRegex()
                    val nodeTextFound = nodeTextRegex.findAll(dayNodeText)

                    val nodesList = mutableListOf<Int>()
                    nodeTextFound.forEach {itt ->
                        val nodeText = itt.value.trim()
                        val curNode = nodePrefixToNode[nodeText.substring(0, 1)]!!.toInt() + (nodeText.substring(1).toInt())
                        nodesList.add(curNode)
                    }
                    // 原则上，课的Node应该是连续的。但是为了以防万一，还是做了不连续的话就截断，添加多个同名课程的处理
                    nodesList.sort()
                    var startNode = -1
                    var endNode = -1
                    var lastNode = -1
                    for ((i, node) in nodesList.withIndex()) {
                        if (i == 0) {
                            startNode = node
                            endNode = node
                        } else if (lastNode + 1 == node) {
                            // 依旧连续，继续
                            endNode = node
                        } else if (lastNode + 1 != node) {
                            // 断开了，把前面连续的添加
                            rt.add(
                                Course(
                                    name = "$nameCourseName($nameClassName)".trim(),
                                    day = courseDay,
                                    room = room.trim(),
                                    teacher = teacher.trim(),
                                    startNode = startNode,
                                    endNode = endNode,
                                    startWeek = startWeek,
                                    endWeek = endWeek,
                                    type = type,
                                    credit = credit,
                                    note = "$noteCollege $noteCourseId $noteDesc".trim()
                                )
                            )
                            // 从当前开始重新计算连续
                            startNode = node
                            endNode = node
                        }
                        if (i == nodesList.count() - 1) {
                            //末尾了，也需要添加
                            rt.add(
                                Course(
                                    name = "$nameCourseName($nameClassName)".trim(),
                                    day = courseDay,
                                    room = room.trim(),
                                    teacher = teacher.trim(),
                                    startNode = startNode,
                                    endNode = endNode,
                                    startWeek = startWeek,
                                    endWeek = endWeek,
                                    type = type,
                                    credit = credit,
                                    note = "$noteCollege $noteCourseId $noteDesc".trim()
                                )
                            )
                        }
                        lastNode = node
                    }
                }
            }
        }
        return rt
    }
}