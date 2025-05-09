package main.java.parser

import bean.Course
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import parser.Parser

/**
 * 感谢 翠翠的南哪课表 和 小许的南哪另一课表 项目代码为我提供的帮助
 *
 * 南京大学教务服务平台
 * @link https://ehallapp.nju.edu.cn/jwapp/sys/wdkb/\*default/index.do
 */
class NJUParser(source: String) : Parser(source) {
    override fun generateCourseList(): List<Course> {
        val doc: Document = Jsoup.parse(source)

        // TimeMap: [节次 -> (开始时间, 结束时间)]
        val timeMap = mutableMapOf<Int, Pair<String, String>>()

        val rows = doc.select("#kcb_container .wut_table tbody tr")

        // 抽取节次时间映射
        rows.drop(1).forEach { tr ->
            val unitCell = tr.selectFirst("td.mtt_bgcolor_grey[data-unit]") ?: return@forEach
            val unit = unitCell.attr("data-unit").toInt()
            val matcher = Regex("""\((\d{2}:\d{2})~(\d{2}:\d{2})\)""").find(unitCell.text())
            matcher?.destructured?.let { (st, et) ->
                timeMap[unit] = st to et
            }
        }

        val courses = mutableListOf<Course>()
        rows.drop(1).forEach { tr ->
            val unitCell = tr.selectFirst("td.mtt_bgcolor_grey[data-unit]") ?: return@forEach
            val unit = unitCell.attr("data-unit").toInt()
            tr.select("td[data-role=item]").forEach { td ->
                println(td)

                val item = td.selectFirst(".mtt_arrange_item") ?: return@forEach

                val name    = item.selectFirst(".mtt_item_kcmc")!!.text()
                val teacher = item.selectFirst(".mtt_item_jxbmc")!!.text()
                val room    = item.selectFirst(".mtt_item_room")!!.text()

                val courseTime = item.selectFirst(".mtt_item_sksj")!!.text()

                val day = when {
                    "周一" in courseTime -> 1
                    "周二" in courseTime -> 2
                    "周三" in courseTime -> 3
                    "周四" in courseTime -> 4
                    "周五" in courseTime -> 5
                    "周六" in courseTime -> 6
                    "周日" in courseTime -> 7
                    else -> 0
                }

                val (startNode, endNode) = Regex("""(\d+)-(\d+)节""")
                    .find(courseTime)
                    ?.destructured
                    ?.let { (a, b) -> a.toInt() to b.toInt() }
                    ?: (unit to unit)

                val (stTime, etTime) = timeMap[unit] ?: ("" to "")

                if (courseTime.contains(",")) {
                    // 离散周
                    val weeks = Regex("""(\d+)(?=周)""")
                        .findAll(courseTime)
                        .map { it.groupValues[1].toInt() }
                        .toList()
                    weeks.forEach { w ->
                        courses += Course(
                            name      = name,
                            day       = day,
                            room      = room,
                            teacher   = teacher,
                            startNode = startNode,
                            endNode   = endNode,
                            startWeek = w,
                            endWeek   = w,
                            type      = 0,
                            credit    = 0f,
                            note      = "",
                            startTime = stTime,
                            endTime   = etTime
                        )
                    }
                } else {
                    val (startWeek, endWeek) = Regex("""(\d+)-(\d+)周""")
                        .find(courseTime)
                        ?.destructured
                        ?.let { (a, b) -> a.toInt() to b.toInt() }
                        ?: (1 to 1)

                    val type = when {
                        "单" in courseTime -> 1
                        "双" in courseTime -> 2
                        else -> 0
                    }

                    courses += Course(
                        name = name,
                        day = day,
                        room = room,
                        teacher = teacher,
                        startNode = startNode,
                        endNode = endNode,
                        startWeek = startWeek,
                        endWeek = endWeek,
                        type = type,
                        credit = 0f,
                        note = "",
                        startTime = stTime,
                        endTime = etTime
                    )
                }
            }
        }

        return courses
    }
}