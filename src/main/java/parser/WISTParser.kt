package main.java.parser

import bean.Course
import Common
import main.java.bean.TimeDetail
import main.java.bean.TimeTable
import org.jsoup.Jsoup
import parser.Parser
/**
 * WISTParser
 * @author Qing90bing
 * 学校：武汉船舶职业技术学院
 * 2025-05-02为止，该学校教务系统为金智教育系统
 * 因为只是测试自己学院的，不知道其他学院的情况
 * 查询流程：
 * 1.进入教务系统登录地址（统一身份验证）：http://authserver.wspc.edu.cn/authserver/login?service=http%3A%2F%2Fehall.wspc.edu.cn%2Flogin%3Fservice%3Dhttp%3A%2F%2Fehall.wspc.edu.cn%2Fnew%2Findex.html
 * 2.然后学生课表查询网址：http://jw.wspc.edu.cn/jwapp/sys/emaphome/portal/index.do
 * 3.进入课表会需要一段时间，请耐心等待，直到页面加载完成，再获取课表。
 **/
class WISTParser(private val source: String) : Parser() {

    override fun generateCourseList(): List<Course> {
        val courseList = mutableListOf<Course>()
        val doc = Jsoup.parse(source)

        // 遍历每个带课程的单元格
        doc.select("td[data-role=item]").forEach { td ->
            val day = td.attr("data-week").toIntOrNull() ?: return@forEach

            // 尝试从 td 属性读取节次范围
            val beginNodeAttr = td.attr("data-begin-unit").toIntOrNull()
            val endNodeAttr = td.attr("data-end-unit").toIntOrNull()

            td.select("div.mtt_arrange_item").forEach { block ->
                val name = block.selectFirst(".mtt_item_kcmc")?.ownText()?.trim() ?: return@forEach
                val teacher = block.selectFirst(".mtt_item_jxbmc")?.text()?.trim().orEmpty()
                val roomInfoRaw = block.selectFirst(".mtt_item_room")?.text()?.trim().orEmpty()

                val rawParts = roomInfoRaw.split(",", "，").map(String::trim)

                // 提取周数字段
                val weekParts = rawParts.filter { it.contains("周") }
                if (weekParts.isEmpty()) return@forEach
                val rawWeekStr = weekParts.joinToString(",") { it.replace("周", "") }
                val weekStr = rawWeekStr.replace(Regex("[^0-9\\-,]"), "")

                // 节次提取：优先使用属性，失败才回退
                val (beginNode, endNode) = if (beginNodeAttr != null && endNodeAttr != null) {
                    Pair(beginNodeAttr, endNodeAttr)
                } else {
                    val nodePart = rawParts.firstOrNull {
                        it.matches(Regex("^(中?[1-9]\\d?)(-(中?[1-9]\\d?))?$"))
                    } ?: return@forEach
                    val nodes = nodePart.split("-")
                    Pair(parseNode(nodes.first()), parseNode(nodes.last()))
                }

                val step = endNode - beginNode + 1

                // 提取地点字段
                val roomCandidates = rawParts.filter { !it.contains("周") && !Regex("^(中?[1-9]\\d?)(-(中?[1-9]\\d?))?$").matches(it) }
                val room = roomCandidates.find {
                    it.contains("实验室") || it.contains("教室") || it.contains("机房")
                } ?: roomCandidates.firstOrNull().orEmpty()

                // 周次遍历生成课程
                parseWeeks(weekStr).forEach { week ->
                    courseList += Course(
                        name = name,
                        teacher = teacher,
                        room = room,
                        day = day,
                        startNode = beginNode,
                        endNode = endNode,
                        step = step,
                        startWeek = week,
                        endWeek = week,
                        type = 0,
                        note = ""
                    )
                }
            }
        }

        // 合并周课程
        val merged = mutableListOf<Course>()
        Common.mergeWeekCourse(courseList as ArrayList<Course>, merged as ArrayList<Course>)
        Common.generateTimeTable(merged, generateTimeTable())

        // 合并相邻节次课程
        merged.sortWith(compareBy(
            { it.name }, { it.teacher }, { it.room }, { it.day },
            { it.startWeek }, { it.endWeek }, { it.startNode }
        ))

        val optimized = mutableListOf<Course>()
        var i = 0
        while (i < merged.size) {
            var current = merged[i]
            var j = i + 1
            while (j < merged.size) {
                val next = merged[j]
                if (
                    current.name == next.name &&
                    current.teacher == next.teacher &&
                    current.room == next.room &&
                    current.day == next.day &&
                    current.startWeek == next.startWeek &&
                    current.endWeek == next.endWeek &&
                    current.type == next.type &&
                    current.endNode + 1 == next.startNode
                ) {
                    // 合并节次
                    current = current.copy(
                        endNode = next.endNode,
                        step = next.endNode - current.startNode + 1
                    )
                    j++
                } else {
                    break
                }
            }
            optimized.add(current)
            i = j
        }

        return optimized
    }

    override fun getTableName(): String = "武船课表"
    override fun getNodes(): Int = 12
    override fun getMaxWeek(): Int = 20

    override fun generateTimeTable(): TimeTable = TimeTable(
        name = "武汉船舶职业技术学院",
        timeList = listOf(
            TimeDetail(1, "08:10", "08:55"),
            TimeDetail(2, "09:05", "09:50"),
            TimeDetail(3, "10:10", "10:55"),
            TimeDetail(4, "11:05", "11:50"),
            TimeDetail(5, "12:30", "13:10"),
            TimeDetail(6, "13:20", "14:00"),
            TimeDetail(7, "14:00", "14:45"),
            TimeDetail(8, "14:55", "15:40"),
            TimeDetail(9, "16:00", "16:45"),
            TimeDetail(10, "16:55", "17:40"),
            TimeDetail(11, "19:00", "19:45"),
            TimeDetail(12, "19:55", "20:40")
        )
    )

    /**
     * 解析周次字符串，支持：单周、范围、逗号分隔、混合
     * 示例输入："2-6,8,10-13"
     */
    private fun parseWeeks(weekStr: String): List<Int> {
        return weekStr
            .split(",")
            .map { it.trim() }
            .filter { it.matches(Regex("^\\d+(?:-\\d+)?$")) }
            .flatMap { part ->
                part.split("-").let { range ->
                    if (range.size == 1) {
                        listOfNotNull(range[0].toIntOrNull())
                    } else {
                        val start = range[0].toIntOrNull() ?: return@flatMap emptyList()
                        val end = range[1].toIntOrNull() ?: return@flatMap emptyList()
                        (start..end).toList()
                    }
                }
            }
            .toSortedSet()  // 去重并排序
            .toList()
    }

    /**
     * 解析节次，支持“中1”->5、“中2”->6，以及数字
     */
    private fun parseNode(str: String): Int = when {
        str.contains("中1") -> 5
        str.contains("中2") -> 6
        else -> str.filter { it.isDigit() }.toIntOrNull() ?: -1
    }

    companion object {
        /**
         * 从完整 HTML 中提取出课表表格 HTML
         */
        fun extractTableHtml(fullHtml: String): String? {
            val doc = Jsoup.parse(fullHtml)
            val table = doc.selectFirst("table.wut_table") ?: return null
            return table.outerHtml()
        }
    }
}
