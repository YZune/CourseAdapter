package main.java.parser

import bean.Course
import Common
import main.java.bean.TimeDetail
import main.java.bean.TimeTable
import org.jsoup.Jsoup
import parser.Parser
import java.time.LocalDate

/**
 * WISTParser
 * @author Qing90bing
 * 学校：武汉船舶职业技术学院
 * 2025-04-24为止，该学校教务系统为金智教育系统
 * 因为只是测试自己学院的，不知道其他学院的情况
 * 查询流程：
 * 1.进入教务系统登录地址（统一身份验证）：http://authserver.wspc.edu.cn/authserver/login?service=http%3A%2F%2Fehall.wspc.edu.cn%2Flogin%3Fservice%3Dhttp%3A%2F%2Fehall.wspc.edu.cn%2Fnew%2Findex.html
 * 2.然后学生课表查询网址：http://jw.wspc.edu.cn/jwapp/sys/emaphome/portal/index.do?t_s=1745508936570&amp_sec_version_=1&gid_=NE91WHZpMEZtTW1FcXBoTSt3QWpjbm81bnRSQkxGRGpnOHFnNGtTdEQxY1RNTHB6NGtPQjNJZERFbldTV2Rpa3ExVDB3L056Z2tEbHVQNjhFc3p2ckE9PQ&EMAP_LANG=zh&THEME=cherry
 * 3.进入课表会需要一段时间，请耐心等待，直到页面加载完成，然后最好点击打印，再获取课表，目前只是过这个步骤。
 **/

class WISTParser(private val source: String) : Parser() {

    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()
        val doc = Jsoup.parse(source)

        val items = doc.select("td[data-role=item]")

        for (td in items) {
            val day = td.attr("data-week").toIntOrNull() ?: continue
            val startNode = td.attr("data-begin-unit").toIntOrNull() ?: continue
            val endNode = td.attr("data-end-unit").toIntOrNull() ?: continue
            val step = endNode - startNode + 1

            val blocks = td.select("div.mtt_arrange_item")

            for (block in blocks) {
                val nameRaw = block.selectFirst(".mtt_item_kcmc")?.ownText()?.trim() ?: continue
                val name = nameRaw.replace(Regex("""\[.*?]"""), "")

                val teacher = block.selectFirst(".mtt_item_jxbmc")?.text()?.trim() ?: ""
                val roomInfoRaw = block.selectFirst(".mtt_item_room")?.text()?.trim() ?: continue

                // 拆解信息：week + 节次 + 地点
                val parts = roomInfoRaw.split(",", "，")
                if (parts.size < 3) continue

                // 自动识别周次字段
                val weekParts = parts.filter { it.contains("周") }
                val room = parts.last()

                val weeks = parseWeeks(weekParts.joinToString(","))
                if (weeks.isEmpty()) continue

                val weekBeans = Common.weekIntList2WeekBeanList(weeks.toMutableList())

                for (week in weekBeans) {
                    val course = Course(
                        name = name,
                        teacher = teacher,
                        room = room,
                        day = day,
                        startNode = startNode,
                        endNode = endNode,
                        step = step,
                        startWeek = week.start,
                        endWeek = week.end,
                        type = week.type
                    )
                    courseList.add(course)
                }
            }
        }

        val finalList = arrayListOf<Course>()
        Common.mergeWeekCourse(courseList, finalList)

        // ✅ 自动补全 startTime 和 endTime 字段
        Common.generateTimeTable(finalList, generateTimeTable())

        return finalList
    }

    override fun getTableName(): String = "${LocalDate.now()}课表"

    // 每天课程节数
    override fun getNodes(): Int = 12

    // 学期课程周数
    override fun getMaxWeek(): Int = 20

    // 课程时间表
    override fun generateTimeTable(): TimeTable {
        return TimeTable(
            name = "武汉船舶技术学院", timeList = listOf(
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
    }

    private fun parseWeeks(weekStr: String): List<Int> {
        val weekList = mutableListOf<Int>()
        val cleaned = weekStr.replace("周", "").replace("，", ",")
        val parts = cleaned.split(",")

        for (part in parts) {
            if (part.contains("-")) {
                val (startStr, endStr) = part.split("-")
                val start = startStr.filter { it.isDigit() }.toIntOrNull() ?: continue
                val end = endStr.filter { it.isDigit() }.toIntOrNull() ?: continue
                weekList.addAll(start..end)
            } else {
                val single = part.filter { it.isDigit() }.toIntOrNull()
                if (single != null) weekList.add(single)
            }
        }

        return weekList.distinct().sorted()
    }
}
