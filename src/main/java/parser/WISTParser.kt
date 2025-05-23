package main.java.parser

import bean.Course
import Common
import main.java.bean.TimeDetail
import main.java.bean.TimeTable
import org.jsoup.Jsoup
import parser.Parser

/**
 * WISTParser 教务课表解析器
 * 适配：武汉船舶职业技术学院 金智教务系统
 * 日期：2025-05-22
 * 作者：Qing90bing
 * 注意：2025-05-22为止，该学校教务系统为金智教育系统
 * 因为是测试自己学院的，不知道其他学院情况，有能力的话可以在issue中留言，把课表文件发给我
 * 查询流程：
 * 1.进入教务系统登录地址（统一身份验证）：http://authserver.wspc.edu.cn/authserver/login?service=http%3A%2F%2Fehall.wspc.edu.cn%2Flogin%3Fservice%3Dhttp%3A%2F%2Fehall.wspc.edu.cn%2Fnew%2Findex.html
 * 2.然后学生课表查询网址：http://jw.wspc.edu.cn/jwapp/sys/emaphome/portal/index.do
 * 3.进入课表会需要一段时间，请耐心等待，直到页面加载完成，然后最好点击打印，再获取课表，目前只是过这个步骤。
 *
 * 特性：
 * ✅ 自动清洗周次字符串（支持"单周""双周""X-X周"等格式）
 * ✅ 智能合并同一课程的不同周次记录
 * ✅ 自动识别连续节次课程并合并（如3-4节连排合并为一个条目）
 * ✅ 生成结构化课表数据（支持12节/天，20周学制）
 * ✅ 包含详细的调试日志输出（可通过注释控制开关）
 */

class WISTParser(private val source: String) : Parser() {

    //生成课程列表的核心逻辑从 HTML 中提取课程信息，转换为标准 Course 列表，合并课表时间与周次信息
    override fun generateCourseList(): List<Course> {
        val courseList = ArrayList<Course>() // 原始课程列表
        val doc = Jsoup.parse(source) // 解析输入的HTML内容
        val classNameSet = HashSet<String>() // 班级名称集合（自动去重）

        // 获取每个单元格（td）作为一天中的课表格子
        val itemCells = doc.select("td[data-role=item]")
        for (td in itemCells) {
            // 解析星期几（1-7对应周一到周日）
            val day = td.attr("data-week").toIntOrNull() ?: continue

            // 解析节次范围（数据属性或文本内容）
            val beginNodeAttr = td.attr("data-begin-unit").toIntOrNull()
            val endNodeAttr = td.attr("data-end-unit").toIntOrNull()

            // 提取单元格内的所有课程块
            val courseDivs = td.select("div.mtt_arrange_item")
            for (block in courseDivs) {
                // 解析课程基础信息
                val name = block.selectFirst(".mtt_item_kcmc")?.ownText()?.trim() ?: continue
                val teacher = block.selectFirst(".mtt_item_jxbmc")?.text()?.trim().orEmpty()
                val roomInfoRaw = block.selectFirst(".mtt_item_room")?.text()?.trim().orEmpty()

                // 提取班级名（可用于课表命名）
                block.selectFirst(".mtt_item_bjmc")?.text()?.trim()?.takeIf { it.isNotEmpty() }?.let {
                    classNameSet += it
                }

                // 解析周次信息
                val rawParts = roomInfoRaw.split(Regex("[,，]")).map(String::trim)
                val weekParts = rawParts.filter { it.contains("周") }
                if (weekParts.isEmpty()) continue

                val fullWeekStr = weekParts.joinToString(",")
                val isOdd = fullWeekStr.contains("单")
                val isEven = fullWeekStr.contains("双")

                // 仅保留数字部分，去除“周”等非数字干扰
                val weekStrCleaned = weekParts.joinToString(",") {
                    it.replace("周", "")
                }.replace(" ", "")

                // 提取节次范围（优先使用 data- 属性）
                val (beginNode, endNode) = extractNodeRange(beginNodeAttr, endNodeAttr, rawParts)
                if (beginNode <= 0 || endNode <= 0) continue
                val step = endNode - beginNode + 1
                val room = extractRoom(rawParts)

                // DEBUG: 以下为解析调试日志，在数据进入WISTTest.kt文件前，可用于排查解析失败或异常周次
                // val parsedWeeks = parseWeeks(weekStrCleaned, isOdd, isEven)
                // println("══════════════════════════════════════════════")
                // println("📚 课程名称       : $name")
                // println("🗓️ 上课星期       : 星期$day")
                // println("⏰ 上课节次       : 第 $beginNode 节 ～ 第 $endNode 节")
                // println("👨‍🏫 任课教师       : $teacher")
                // println("🏫 原始周次字符串  : $fullWeekStr")
                // println("🧼 清洗后周次     : $weekStrCleaned")
                // println("🔢 单/双周判断    : 单周 = $isOdd ，双周 = $isEven")
                // println("📅 解析结果周次   : $parsedWeeks")
                // println("══════════════════════════════════════════════\n")

                // 按每周生成一个 Course 对象（适配课表结构）
                for (week in parseWeeks(weekStrCleaned, isOdd, isEven)) {
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

        // 合并周次相同、节次相同的课程
        val merged = ArrayList<Course>()
        Common.mergeWeekCourse(courseList, merged)

        // 生成课程时间表（用于后续显示/提醒）
        Common.generateTimeTable(merged, generateTimeTable())

        // 合并相邻节次（上下节连排）
        val optimized = mergeAdjacentNodes(merged)
        this.classNames = classNameSet.toList().sorted()
        return optimized
    }

    private var classNames: List<String> = emptyList()

    //返回学校课表名称（包含班级信息）
    override fun getTableName(): String {
        return if (classNames.isNotEmpty()) {
            "武船" + classNames.joinToString(",") + "课表"
        } else {
            "武船课表"
        }
    }

    //获取每日最大节次（固定为12节）
    override fun getNodes(): Int = 12

    //学校最大周数
    override fun getMaxWeek(): Int = 20

    //定义标准时间表（武汉船舶职业技术学院专用）
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
    //解析周次字段（如 1-3,5-7(单),10-14(双)）
    private fun parseWeeks(rawText: String, ignored1: Boolean = false, ignored2: Boolean = false): List<Int> {
        val weekList = mutableSetOf<Int>()
        val parts = rawText.split(",")

        for (part in parts) {
            val isOdd = part.contains("单")
            val isEven = part.contains("双")

            // 清洗周次数字部分（如 12-14(双) -> 12-14）
            val clean = part.replace(Regex("[^0-9\\-]"), "")
            val weekRange = if ("-" in clean) {
                val (start, end) = clean.split("-").mapNotNull { it.toIntOrNull() }
                (start..end).toList()
            } else {
                listOfNotNull(clean.toIntOrNull())
            }

            val filtered = when {
                isOdd -> weekRange.filter { it % 2 == 1 }
                isEven -> weekRange.filter { it % 2 == 0 }
                else -> weekRange
            }

            weekList += filtered
        }

        return weekList.toList().sorted()
    }

    //判断某一周是否是有效的（单/双周筛选）
    private fun isValidWeek(week: Int, isOdd: Boolean, isEven: Boolean): Boolean {
        return if (isOdd) week % 2 == 1 else if (isEven) week % 2 == 0 else true
    }

    //解析节次字符串（支持“中1”、“中2”等中午节次）
    private fun parseNode(str: String): Int = when {
        str.contains("中1") -> 5  // 中午第1节对应第5节
        str.contains("中2") -> 6  // 中午第2节对应第6节
        else -> str.filter(Char::isDigit).toIntOrNull() ?: -1
    }

    //提取开始和结束节次
    private fun extractNodeRange(beginAttr: Int?, endAttr: Int?, parts: List<String>): Pair<Int, Int> {
        return if (beginAttr != null && endAttr != null) {
            Pair(beginAttr, endAttr)
        } else {
            val nodePart = parts.firstOrNull { nodePattern.matches(it) } ?: return Pair(-1, -1)
            val nodes = nodePart.split("-")
            Pair(parseNode(nodes.first()), parseNode(nodes.last()))
        }
    }

    //提取教室信息
    private fun extractRoom(parts: List<String>): String {
        val roomRegex = Regex("实验室|教室|机房")
        return parts.firstOrNull {
            !it.contains("周") && !it.matches(Regex("^(中?[1-9]\\d?)(-(中?[1-9]\\d?))?$")) && roomRegex.containsMatchIn(it)
        } ?: parts.firstOrNull {
            !it.contains("周") && !it.matches(Regex("^(中?[1-9]\\d?)(-(中?[1-9]\\d?))?$"))
        }.orEmpty()
    }

    //合并相邻节次的课程（如第1-2节与3-4节相邻且内容一致）
    private fun mergeAdjacentNodes(courses: List<Course>): List<Course> {
        val sorted = courses.sortedWith(
            compareBy<Course> { it.name }
                .thenBy { it.teacher }
                .thenBy { it.room }
                .thenBy { it.day }
                .thenBy { it.startWeek }
                .thenBy { it.endWeek }
                .thenBy { it.startNode }
        )
        val result = ArrayList<Course>()
        var current = sorted[0]

        for (i in 1 until sorted.size) {
            val next = sorted[i]
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
                current = current.copy(
                    endNode = next.endNode,
                    step = next.endNode - current.startNode + 1
                )
            } else {
                result += current
                current = next
            }
        }
        result += current
        return result
    }

    //从完整 HTML 中提取课表表格部分
    companion object {
        private val weekPattern = Regex("[^0-9\\-,(单双)]")

        private val nodePattern = Regex("^(中?[1-9]\\d?)(-(中?[1-9]\\d?))?$")

        fun extractTableHtml(fullHtml: String): String? {
            return Jsoup.parse(fullHtml).selectFirst("table.wut_table")?.outerHtml()
        }
    }
}
