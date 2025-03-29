package main.java.parser

import Common.acquireInBlock
import bean.Course
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import main.java.bean.TimeDetail
import main.java.bean.TimeTable
import org.jsoup.Connection.Method
import org.jsoup.Jsoup
import parser.Parser
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil


/**
 * 福州软件职业技术学院
 *
 * 注意：调用 `generateCourseList()` 后，其他重载函数才会返回正确信息
 *
 * @author LgCookie <lgc2333@126.com>
 */
class FSTVCParser(
    phpSessionId: String,  // from cookies
) : Parser("") {
    private val gson = GsonBuilder().create()
    private val cookies = mapOf("PHPSESSID" to phpSessionId)
    private val baseUrl = "https://jw.fzrjxy.com"
    private val studyPlanUrl = "${baseUrl}/studentportal.php/Jxxx/xxjdxx"

    private val nodeNum = 12
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

    private data class StudyPlan(
        /** 班级描述 */
        @SerializedName("skbjmc") val className: String,
        @SerializedName("kcmc") val courseName: String,
        @SerializedName("skjsxm") val teacher: String,
        @SerializedName("zc") val week: Int,
        @SerializedName("skrq") val date: String,
        /** 课程节数，`-` 分隔 */
        @SerializedName("jcshow") val nodes: String,
        @SerializedName("skcdmc") val room: String?,
        // @SerializedName("sknl") val content: String,
        @SerializedName("xq") val semester: Int,
        @SerializedName("xqs") val day: Int,
        @SerializedName("xn") val schoolYear: String,
    ) {
        private var _nodesLi: List<Int>? = null
        private val nodesLi: List<Int>
            get() {
                if (_nodesLi == null) {
                    _nodesLi = nodes.split("-").map { it.toInt() }
                }
                return _nodesLi!!
            }
        val startNode: Int get() = nodesLi.first()
        val endNode: Int get() = nodesLi.last()
    }

    private data class StudyPlanAPIReturn(
        val rows: List<StudyPlan>,
        val total: Int,
    )

    private var tableName: String? = null
    private var startDate: String? = null
    private var timeTable: TimeTable? = null

    private fun setTimeTable(isFirstTablePeak: Boolean) {
        timeTable = if (isFirstTablePeak) {
            TimeTable(
                "福软错峰第一批次时间表", listOf(
                    TimeDetail(1, "08:25", "09:10"),
                    TimeDetail(2, "09:15", "10:00"),
                    TimeDetail(3, "10:10", "10:55"),
                    TimeDetail(4, "11:00", "11:45"),
                    TimeDetail(5, "13:50", "14:35"),
                    TimeDetail(6, "14:40", "15:25"),
                    TimeDetail(7, "15:35", "16:20"),
                    TimeDetail(8, "16:25", "17:10"),
                    TimeDetail(9, "18:30", "19:15"),
                    TimeDetail(10, "19:25", "20:10"),
                    TimeDetail(11, "20:20", "21:05"),
                    TimeDetail(12, "21:15", "22:00"),
                )
            )
        } else {
            TimeTable(
                "福软错峰第二批次时间表", listOf(
                    TimeDetail(1, "08:45", "09:30"),
                    TimeDetail(2, "09:35", "10:20"),
                    TimeDetail(3, "10:30", "11:15"),
                    TimeDetail(4, "11:20", "12:05"),
                    TimeDetail(5, "13:50", "14:35"),
                    TimeDetail(6, "14:40", "15:25"),
                    TimeDetail(7, "15:35", "16:20"),
                    TimeDetail(8, "16:25", "17:10"),
                    TimeDetail(9, "18:30", "19:15"),
                    TimeDetail(10, "19:25", "20:10"),
                    TimeDetail(11, "20:20", "21:05"),
                    TimeDetail(12, "21:15", "22:00"),
                )
            )
        }
    }

    private suspend fun getStudyPlanApiUrl(): String {
        val soup = withContext(Dispatchers.IO) {
            Jsoup.connect(studyPlanUrl).cookies(cookies).get()
        }
        val apiData = soup
            .selectFirst("table#mainlist")
            .attr("data-options")
        val path = Regex("url:'(?<url>.+?)'").find(apiData)!!.groups["url"]!!.value
        return "${baseUrl}${path}"
    }

    private suspend fun fetchStudyPlans(apiUrl: String, weekNum: Int? = null): List<StudyPlan> {
        val limit = 30

        suspend fun task(page: Int): StudyPlanAPIReturn {
            val raw = withContext(Dispatchers.IO) {
                Jsoup
                    .connect(apiUrl)
                    .cookies(cookies)
                    .data(
                        mapOf(
                            "page" to page.toString(),
                            "rows" to limit.toString(),
                            "zc" to (weekNum ?: 0).toString(),
                            "sort" to "skrq", // 日期从低到高
                            "order" to "asc",
                        )
                    )
                    .method(Method.POST)
                    .execute()
                    .body()
            }
            return gson.fromJson(raw, StudyPlanAPIReturn::class.java)
        }

        val firstData = task(1)
        if (firstData.rows.size >= firstData.total) return firstData.rows

        val totalPage = ceil(firstData.total.toDouble() / limit).toInt()
        val sem = Semaphore(4)
        return coroutineScope {
            firstData.rows + (2..totalPage)
                .map { page -> async { sem.acquireInBlock { task(page) } } }
                .awaitAll()
                .flatMap { it.rows }
        }
    }

    private fun transformPlanToCourse(plan: StudyPlan): Course = Course(
        name = plan.courseName,
        day = plan.day,
        room = plan.room ?: "",
        teacher = plan.teacher,
        startNode = plan.startNode,
        endNode = plan.endNode,
        startWeek = plan.week,
        endWeek = plan.week,
        type = 0,
        // note = normalizeLineEnds(plan.content.trim())
    )

    override fun generateCourseList(): List<Course> {
        val apiUrl = runBlocking { getStudyPlanApiUrl() }
        val plans = runBlocking { fetchStudyPlans(apiUrl) }  // 0 为全部周次

        val p0 = plans.first()
        tableName = "福软${p0.className}课程表（${p0.schoolYear}第${p0.semester}学期）"
        val p0Date = SimpleDateFormat("yyyy-MM-dd").parse(p0.date)
        startDate = SimpleDateFormat("yyyy-MM-dd").format(
            Date(
                p0Date.time
                        - ((p0.week - 1).toLong() * 604800000)  // * 7 * 24 * 60 * 60 * 1000
                        - ((p0.day - 1).toLong() * 86400000) // * 24 * 60 * 60 * 1000
            )
        )
        val isFirstTablePeak = firstPeakDepartments.any { p0.className.contains(it) }
        setTimeTable(isFirstTablePeak)

        return plans.map(::transformPlanToCourse)
    }

    override fun getTableName(): String? = tableName

    override fun getNodes(): Int = nodeNum

    override fun getMaxWeek(): Int = maxWeek

    override fun getStartDate(): String? = startDate

    override fun generateTimeTable(): TimeTable? = timeTable
}
