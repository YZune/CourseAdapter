package main.java.parser

import bean.Course
import main.java.bean.TimeDetail
import main.java.bean.TimeTable
import org.jsoup.Jsoup
import parser.Parser


/**
 * 福州软件职业技术学院
 * @author student_2333 <lgc2333@126.com>
 */
class FSTVCParser(
    phpSessionId: String,  // from cookies
) : Parser("") {
    private val cookies = mapOf("PHPSESSID" to phpSessionId)
    private val baseUrl = "http://112.111.43.241"
    private val courseTableUrl = "${baseUrl}/studentportal.php/Jxxx/xskbxx/optype/1"

    private val nodeNum = 14
    private val maxWeek = 20

    // 错峰第一批次专业表
    private val firstPeakDepartments = setOf(
        // 智能产业学院
        "软件技术",
        "移动互联应用技术",
        "信息安全技术应用",
        "智能产品开发与应用",
        "人工智能技术应用",
        // 智能建造产业学院
        "建设工程管理",
        "建筑室内设计",
        "工程造价",
        "建筑智能化工程技术",
        "智能建造技术",
        // 现代通信产业学院
        "现代通信技术",
        "现代移动通信技术",
        "智能互联网络技术",
        "网络规划与优化技术",
        // 数据产业学院
        "大数据技术",
        "区块链技术应用",
        "云计算技术应用",
        "工业软件开发技术",
    )

    private data class TableCell<T>(var value: T, var height: Int = 1)

    private fun getCourseTableUrlList(): List<String> {
        val body = Jsoup.connect(courseTableUrl).cookies(cookies).execute().body()
        val urlReg = Regex("p.find\\('iframe'\\).attr\\('src','(?<url>.+?)'\\);")
        return urlReg.findAll(body).toList().map { "${baseUrl}${it.groups["url"]!!.value}" }
    }

    private fun getCoursesFromUrl(url: String, weekNum: Int): List<Course> {
        // 处理横着排序且中间有空位的表格
        val soup = Jsoup.connect(url).cookies(cookies).get()
        val tableElem = soup.selectFirst("table")
        val cellElem = tableElem
            .select("tr")
            .drop(1)
            .map { it.children().drop(1) }

        val weekCourseList = Array<MutableList<TableCell<Course?>>>(7) { mutableListOf() }
        for ((currentNode, row) in cellElem.withIndex()) {
            for (cell in row) {
                val weekIndex = fun(): Int {
                    for ((i, courses) in weekCourseList.withIndex()) {
                        val weekLength = courses.sumOf { it.height }
                        if (weekLength < currentNode + 1) return i
                    }
                    return weekCourseList.size - 1
                }.invoke()
                val currList = weekCourseList[weekIndex]

                val nodeLength = cell.attr("rowspan").toInt()
                val infoDiv = cell.selectFirst("div")
                if (infoDiv == null) {
                    if (currList.isEmpty() || currList.last().value != null) {
                        currList.add(TableCell(null, nodeLength))
                    } else {
                        currList.last().height += nodeLength
                    }
                    continue
                }

                val cellContent = infoDiv.attr("title")
                val (name, teacher, room) = cellContent.split("\n")
                val course = Course(
                    name = name,
                    teacher = teacher,
                    room = room,
                    day = weekIndex + 1,
                    startNode = currentNode + 1,
                    endNode = currentNode + nodeLength,
                    startWeek = weekNum,
                    endWeek = weekNum,
                    type = 0
                )
                currList.add(TableCell(course, nodeLength))
            }
        }

        return weekCourseList
            .toList()
            .flatten()
            .mapNotNull { it.value }
    }

    private val courseTableUrlList: List<String> = getCourseTableUrlList()
    private val firstTableTitle: String =
        Jsoup
            .connect(courseTableUrlList[0])
            .cookies(cookies)
            .get()
            .selectFirst("div.f2")
            .text()

    private val tableName: String = (firstTableTitle.substringBefore(" ")
            + "课程表("
            + firstTableTitle.substringAfter("("))
    private val isFirstTablePeak: Boolean = firstPeakDepartments.any { firstTableTitle.contains(it) }
    private val timeTable: TimeTable = if (isFirstTablePeak) {
        TimeTable(
            "福软错峰第一批次时间表",
            listOf(
                TimeDetail(1, "08:25", "09:10"),
                TimeDetail(2, "09:15", "10:00"),
                TimeDetail(3, "10:10", "10:55"),
                TimeDetail(4, "11:00", "11:45"),
                TimeDetail(5, "11:45", "13:00"),  // 中午 1，空
                TimeDetail(6, "13:00", "13:50"),  // 中午 2，空
                TimeDetail(7, "13:50", "14:35"),
                TimeDetail(8, "14:40", "15:25"),
                TimeDetail(9, "15:35", "16:20"),
                TimeDetail(10, "16:25", "17:10"),
                TimeDetail(11, "18:30", "19:15"),
                TimeDetail(12, "19:25", "20:10"),
                TimeDetail(13, "20:20", "21:05"),
                TimeDetail(14, "21:15", "22:00"),
            )
        )
    } else {
        TimeTable(
            "福软错峰第二批次时间表",
            listOf(
                TimeDetail(1, "08:45", "09:30"),
                TimeDetail(2, "09:35", "10:20"),
                TimeDetail(3, "10:30", "11:15"),
                TimeDetail(4, "11:20", "12:05"),
                TimeDetail(5, "12:05", "13:00"),  // 中午 1，空
                TimeDetail(6, "13:00", "13:50"),  // 中午 2，空
                TimeDetail(7, "13:50", "14:35"),
                TimeDetail(8, "14:40", "15:25"),
                TimeDetail(9, "15:35", "16:20"),
                TimeDetail(10, "16:25", "17:10"),
                TimeDetail(11, "18:30", "19:15"),
                TimeDetail(12, "19:25", "20:10"),
                TimeDetail(13, "20:20", "21:05"),
                TimeDetail(14, "21:15", "22:00"),
            )
        )
    }

    override fun generateCourseList(): List<Course> {
        return courseTableUrlList
            .mapIndexed { i, url -> getCoursesFromUrl(url, i + 1) }
            .flatten()
    }

    override fun getTableName(): String = tableName

    override fun getNodes(): Int = nodeNum

    override fun getMaxWeek(): Int = maxWeek

    override fun generateTimeTable(): TimeTable = timeTable
}
