package main.java.parser

import Common

import bean.Course
import com.google.gson.JsonParser
import main.java.exception.EmptyException
import main.java.exception.GetTermDataErrorException
import main.java.exception.PasswordErrorException
import org.jsoup.Jsoup
import org.jsoup.Connection.*
import org.jsoup.HttpStatusException
import parser.Parser

/**
 * (2022/4, @ludoux) 维护小提示：
 *
 * 1. (Deprecated) 接口是从 https://students-schedule.nwpu.edu.cn/ui/#/courseTable 来的
 * 大致为 get 拿 cookie -> post 发登录凭证，回复 url 有 token -> token 写 header 里面取所有学期列表 -> token 写 header 根据匹配的学期拿课表
 *
 * 2. 关于时间表：是在作者私仓 com/suda/yzune/wakeupschedule/schedule_import/ImportViewModel.kt 里 addNwpuTimeTables 函数写的。
 * 不管导入的是哪个校区，如上函数都会新建所有的时间表（存在同名不会覆盖）
 * 假如时间表变更，请联系作者更改里面的函数。注意假如有同名时间表将不会覆盖，所以建议新建以“西工大长安v2”类似来命名，同时这里的_timeTableName也相应更改。
 * 假如太仓校区确认了，同上逻辑，建议新建以“西工大太仓”类似来命名，这里的_timeTableName也相应更改。
 *
 * 3. 关于学期开始日期等：_startDate 等变量以及 override 的相关 get 函数，wakeup 会读取自动正确存储。
 * 这里是因为 courseAdapter 没做相关功能，所以导出的 WakeUp 时间表相关信息不正确，但实际上 App 中是正确的。
 * 假如你要改相关 UI,在作者私仓 com/suda/yzune/wakeupschedule/schedule_import/LoginWebFragment.kt 里，请联系开发者。
 */

/**
 * (2022/6/22, @Pinming) 接口更改说明：
 *
 * 2022/4 版本采用翱翔门户接口存在更新不及时的情况，即一旦发生了调课等情况，接口返回的课程表依然是原有状态，不会随之更新。
 * 本次修改将课程表接口更换为教务系统提供的 API，避免类似的情况。
 * 在发生调课后，可以重新通过 WakeUp App 导入调整后的课程表。
 *
 * 更新后，课程表爬取的大体步骤是：先登入翱翔门户，获取 SESSION 与 __pstsid__，携带这两个 cookie 进入教务系统，进行进一步的爬取。
 * 进入教务系统后：
 * 1）通过接口 semesterIndexUrl 获取 Semester 信息，维护 Semester (ID->Name) 的 Map；
 * 2）通过接口 semesterIndexUrl 获取 dataId 信息，供后续调接口时使用；
 * 3）通过接口 courseTableStdUrl 获取 personId 信息，供后续调接口时使用；
 * 4）获取到这两个 ID 后，分别调用教务系统 API：
 *    /student/for-std/course-table/semester/$semesterId/print-data
 *    /student/for-std/course-table/get-data
 *    a) get-data   获取到的信息主要用于更新 Parser 的内部属性，如 _tableName, _maxWeek, _startDate 等
 *    b) print-data 用于获取各门课中的具体时间信息；get-data 的课程信息结构不太友好
 * 之后就是将获取到的 JSON 丢入 generateCourseList() 方法中格式化处理，生成 courseList 对象。
 *
 * 此外，关于校区判断的逻辑也有所修改，之前的方案应该是以课表中获取到的最后一门课为准。
 * 新的逻辑对各校区的 Activity 作计数，取 Activity 最多的校区为主校区，作为 App 中采用的时间表。
 */

/**
 * @Last_Update 2022/6/22
 * @Creator @ludoux (chinaluchang@live.com)
 * @Maintainer @Pinming (i@pm-z.tech)
 */
const val casLoginUrl: String =
    "https://uis.nwpu.edu.cn/cas/login?service=https%3A%2F%2Fjwxt.nwpu.edu.cn%2Fstudent%2Fsso-login"
const val semesterIndexUrl: String = "https://jwxt.nwpu.edu.cn/student/for-std/lesson-search/index/"
const val courseTableStdUrl: String = "https://jwxt.nwpu.edu.cn/student/for-std/course-table"
var dataId: String = ""
var personId: String = ""

class NWPUParser(
    private val xh: String, private val pwd: String, private val semesterYear: String, private val semesterTerm: Int
) : Parser("") {

    private val headers: HashMap<String, String> = hashMapOf(
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.5111.0 Safari/537.36",
        "origin" to "https://uis.nwpu.edu.cn",
        "referer" to casLoginUrl,
        "content-Type" to "application/x-www-form-urlencoded",
        "scheme" to "https"
    )

    private var _timeTableName = "西工大长安"
    private var _tableName = "西工大 2020-2021 春学期"
    private var _nodes = 13
    private var _startDate = "1970-01-01"
    private var _maxWeek = 22

    private fun webFunc(): String {
        var cookies: Map<String, String>?
        var res: Response = Jsoup.connect(casLoginUrl).method(Method.GET).execute()
        cookies = res.cookies()
        val executionValue = res.parse().getElementsByAttributeValue("name", "execution")[0].attributes().get("value")
        try {
            res = Jsoup.connect(casLoginUrl).method(Method.POST).headers(headers).cookies(cookies).data("username", xh)
                .data("password", pwd).data("currentMenu", "1").data("execution", executionValue)
                .data("_eventId", "submit").data("geolocation", "").data("submit", "One moment please...").execute()
        } catch (hse: HttpStatusException) {
            throw PasswordErrorException("登录失败。因帐号或密码错误，或失败过多暂时被锁定，请核对或稍后更换网络后重试。[$hse]")
        }

        // 获取到 SESSION 与 __pstsid__
        cookies = res.cookies()

        // 获取 personId, dataId
        res = Jsoup.connect(courseTableStdUrl).method(Method.GET).headers(headers).cookies(cookies).execute()
        var parsedRes = res.parse()
        personId =
            Regex("""(?<=personId = ).*(?=;)""").findAll(parsedRes.select("script").toString()).elementAt(0).value
        dataId = Regex("""(?<=dataId = ).*(?=;)""").findAll(parsedRes.select("script").toString()).elementAt(0).value

        // 在 semesterIndexUrl 中获取各学期名及对应 ID
        res = Jsoup.connect(semesterIndexUrl + dataId).method(Method.GET).headers(headers).cookies(cookies).execute()
        parsedRes = res.parse()
        val semesterHtml = parsedRes.select("option[name=\"semesterAssoc\"]").toString()
        val semesterNames = Regex("""(?<=>).*(?=</option>)""").findAll(semesterHtml)
        val semesterIds = Regex("""(?<=value=").*(?=">)""").findAll(semesterHtml)

        // 维护 Map[semesterId, semesterName]
        val semestersName = "秋春夏"
        var semesterId = ""
        var semesterName = ""
        var idx = 0
        for (name in semesterNames) {
            if (name.value.contains("$semesterYear-") && name.value.contains(semestersName[semesterTerm])) {
                semesterId = semesterIds.elementAt(idx).value
                semesterName = semesterNames.elementAt(idx).value
                break
            }
            idx++
        }
        if (semesterId == "") {
            throw GetTermDataErrorException("没有在教务网站上查找到相关学期信息，请重新选择。若持续出错请联系维护者。")
        }


        // 爬取教务系统信息
        // 接口 get-data 与 print-data 所能获取到的数据有所不同：
        // get-data 获取到的信息主要用于更新 Parser 的内部属性，如 _tableName, _maxWeek, _startDate 等
        // print-data 用于获取各门课中的具体时间信息；get-data 的课程信息结构不太友好
        try {
            res =
                Jsoup.connect("https://jwxt.nwpu.edu.cn/student/for-std/course-table/get-data?semesterId=$semesterId&dataId=$personId&bizTypeId=2")
                    .method(Method.GET).headers(headers).cookies(cookies).ignoreContentType(true).execute()
            val lessonsFromGetData = res.parse().select("body").toString().replace("<body>", "").replace("</body>", "")
            res =
                Jsoup.connect("https://jwxt.nwpu.edu.cn/student/for-std/course-table/semester/$semesterId/print-data/$dataId?hasExperiment=true")
                    .method(Method.GET).headers(headers).cookies(cookies).ignoreContentType(true).execute()
            val lessonsFromPrintData =
                res.parse().select("body").toString().replace("<body>", "").replace("</body>", "")
            return "$lessonsFromGetData<split>$lessonsFromPrintData<split>$semesterName"
        } catch (hse: HttpStatusException) {
            throw EmptyException("查询课表信息失败，这不应该发生。若持续出错请联系维护者。[$hse]")
        }
    }

    override fun generateCourseList(): List<Course> {
        val ori = webFunc().split("<split>")
        val jsonFromGetData = JsonParser.parseString(ori[0])
        val jsonFromPrintData = JsonParser.parseString(ori[1])
        val semesterName = ori[2]
        var countChangAn = 0
        var countYouYi = 0
        var countTaiCang = 0
        _tableName = "西工大 $semesterName" // 西工大 2021-2022春
        _maxWeek = jsonFromGetData.asJsonObject.get("weekIndices").asJsonArray.count() // 18
        _startDate =
            jsonFromGetData.asJsonObject.get("lessons").asJsonArray.get(0).asJsonObject.get("semester").asJsonObject.get(
                "startDate"
            ).asString // 2022-02-21

        val courseList = ArrayList<Course>()
        jsonFromPrintData.asJsonObject.get("studentTableVm").asJsonObject.get("activities").asJsonArray.forEach { activity ->
            activity.asJsonObject.get("weekIndexes").asJsonArray.forEach { unit ->
                val weekList = arrayListOf<Int>()
                weekList.add(unit.asInt)
                Common.weekIntList2WeekBeanList(weekList).forEach {
                    courseList.add(
                        Course(
                            name = activity.asJsonObject.get("courseName").asString,
                            day = activity.asJsonObject.get("weekday").asInt,
                            // 对教室增加判空，有可能出现教室为空的情况
                            room = if (!activity.asJsonObject.get("room").isJsonNull) {
                                activity.asJsonObject.get("room").asString
                            } else "",
                            teacher = activity.asJsonObject.get("teachers").asJsonArray.toString().replace("\"", "")
                                .replace("[", "").replace("]", ""),
                            startNode = activity.asJsonObject.get("startUnit").asInt,
                            endNode = activity.asJsonObject.get("endUnit").asInt,
                            startWeek = weekList[0],
                            // minWeekIndex(activity.asJsonObject.get("weekIndexes").asJsonArray),
                            endWeek = weekList[0],
                            // maxWeekIndex(activity.asJsonObject.get("weekIndexes").asJsonArray),
                            type = 0,
                            credit = activity.asJsonObject.get("credits").asFloat,
                            note = activity.asJsonObject.get("lessonCode").asString
                        )
                    )
                    // 通过统计各校区的 activity 数来决定校区
                    if (!activity.asJsonObject.get("campus").isJsonNull) {
                        when {
                            activity.asJsonObject.get("campus").asString.contains("长安校区") -> {
                                countChangAn++
                            }
                            activity.asJsonObject.get("campus").asString.contains("友谊校区") -> {
                                countYouYi++
                            }
                            activity.asJsonObject.get("campus").asString.contains("太仓校区") -> {
                                countTaiCang++
                            }
                        }
                    }
                }
            }
        }
        // 课表校区判断
        // 关于校区判断的逻辑也有所修改，之前的方案应该是以课表中获取到的最后一门课为准。
        // 新的逻辑对各校区的 Activity 作计数，取 Activity 最多的校区为主校区，作为 App 中采用的时间表。
        when {
            (maxOf(countChangAn, countYouYi, countTaiCang) == countChangAn) -> {
                _nodes = 13
                _timeTableName = "西工大长安"
            }
            (maxOf(countChangAn, countYouYi, countTaiCang) == countYouYi) -> {
                _nodes = 12
                val month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
                _timeTableName = if (month >= 10 || month <= 4) {
                    "西工大友谊冬(10.1-4.30)"
                } else {
                    "西工大友谊夏(5.1-9.30)"
                }
            }
            (maxOf(countChangAn, countYouYi, countTaiCang) == countTaiCang) -> {
                _nodes = 13
                _timeTableName = "西工大太仓(未实现)"
            }
        }
        println("课表名称：$_tableName\n启用时间表：$_timeTableName\n每天节数：$_nodes\n学期开始日期（务必周一）：$_startDate\n学期周数：$_maxWeek")
        return courseList
    }

    override fun getTableName(): String {
        return _tableName
    }

    override fun getNodes(): Int {
        return _nodes
    }

    override fun getStartDate(): String? {
        return if (_startDate == "1970-01-01") {
            null
        } else {
            _startDate
        }
    }

    override fun getMaxWeek(): Int {
        return _maxWeek
    }

}
