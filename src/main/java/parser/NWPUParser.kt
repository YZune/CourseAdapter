package main.java.parser

import Common

import bean.Course
import com.google.gson.JsonParser
import main.java.exception.EmptyException
import main.java.exception.GetTermDataErrorException
import main.java.exception.NetworkErrorException
import main.java.exception.PasswordErrorException
import org.jsoup.Connection
import org.jsoup.Jsoup
import parser.Parser

/*
*  // 接口地址

/* harmony default export */ __webpack_exports__[\"default\"] = ({
  // 获取所有学期
  getAllSemester: function getAllSemester() {
    return axios__WEBPACK_IMPORTED_MODULE_1___default.a.get('/api/semester/list');
  },
  // 获取所有周课表
  getAllWeek: function getAllWeek(id) {
    return axios__WEBPACK_IMPORTED_MODULE_1___default.a.get(\"/api/semester/weekList/\".concat(id));
  },
  // 获取所有周课表
  getSemesterAndWeek: function getSemesterAndWeek(id) {
    return axios__WEBPACK_IMPORTED_MODULE_1___default.a.get(\"/api/semester/semesterAndWeek/\".concat(id));
  },
  // 根据学期+周id 获取课程表数据
  getCourseByWeekId: function getCourseByWeekId(semesterId, weekId) {
    return axios__WEBPACK_IMPORTED_MODULE_1___default.a.get(\"/api/courseTable/\".concat(semesterId, \"/\").concat(weekId));
  },
  // 获取当前周的数据
  getCurrentWeek: function getCurrentWeek() {
    return axios__WEBPACK_IMPORTED_MODULE_1___default.a.get('/api/semester/currentWeek');
  },
  // 获取学生名单列表
  getStudentList: function getStudentList(id) {
    return axios__WEBPACK_IMPORTED_MODULE_1___default.a.get(\"/api/stdList/list/\".concat(id));
  },
  // 课程安排表
  getSchedule: function getSchedule(semesterId, courseId) {
    return axios__WEBPACK_IMPORTED_MODULE_1___default.a.get(\"/api/courseList/schedule/\".concat(semesterId, \"/\").concat(courseId));
  },
  // 获取学期全部课程
  getAllCourseBySemesterId: function getAllCourseBySemesterId(semesterId) {
    return axios__WEBPACK_IMPORTED_MODULE_1___default.a.get(\"/api/courseList/list/\".concat(semesterId));
  },
  // 获取课程选项数据
  getCourseOptions: function getCourseOptions(semesterId) {
    return axios__WEBPACK_IMPORTED_MODULE_1___default.a.get(\"/api/courseList/names/\".concat(semesterId));
  },
  // 根据课程名获取课程列表
  getCourseByName: function getCourseByName(semesterId, keyword) {
    return axios__WEBPACK_IMPORTED_MODULE_1___default.a.get(\"/api/courseList/list/\".concat(semesterId, \"/\").concat(keyword));
  },
  //获取用户头像
  getAvatar: function getAvatar(stdCode) {
    return axios__WEBPACK_IMPORTED_MODULE_1___default.a.get(\"/api/user/avatar/\".concat(stdCode));
  }
});
* */
/*
维护小提示：
接口是从 https://students-schedule.nwpu.edu.cn/ui/#/courseTable 来的
大致为 get 拿cookie -> post 发登录凭证，回复url有token -> token 写 header 里面取所有学期列表 -> token 写 header 根据匹配的学期拿课表

关于时间表：是在作者私仓 com/suda/yzune/wakeupschedule/schedule_import/ImportViewModel.kt 里addNwpuTimeTables函数写的。
不管导入的是哪个校区，如上函数都会新建所有的时间表（存在同名不会覆盖）
假如时间表变更，请联系作者更改里面的函数。注意假如有同名时间表将不会覆盖，所以建议新建以“西工大长安v2”类似来命名，同时这里的_timeTableName也相应更改。
假如太仓校区确认了，同上逻辑，建议新建以“西工大太仓”类似来命名，这里的_timeTableName也相应更改。

关于学期开始日期等：_startDate等变量以及 override 的相关 get 函数，wakeup会读取自动正确存储。这里是因为courseadapter没做相关功能，所以导出的wakeup数据时间等不对，但实际上app是对的。

假如你要改相关UI,在作者私仓 com/suda/yzune/wakeupschedule/schedule_import/LoginWebFragment.kt 里，请联系开发者。
 */

/*
* Last_Update: 2022-4-9
* Creator: @ludoux (chinaluchang@live.com)
* Maintainer:
 */
class NWPUParser(
    private val xh: String,
    private val pwd: String,
    private val semesterYear: String,
    private val semesterTerm: Int
) : Parser("") {
    private val headers: Map<String, String>? = mapOf(
        "User-Agent" to "Mozilla/5.0 (X11; Linux x86_64; rv:99.0) Gecko/20100101 Firefox/99.0",
        "Accept-Language" to "zh-CN"
    )
    private val timeout = 5000

    private var _timeTableName = "西工大长安"
    private var _tableName = "西工大 2020-2021 春学期"
    private var _nodes = 13
    private var _startDate = "1970-01-01"
    private var _maxWeek = 22

    //相当于 login_school/NWPU/nwpu 里的 getWebApi。所以这两个函数不建议融合在一起
    private fun webFunc(): String {
        //针对 vue 应用网页 https://students-schedule.nwpu.edu.cn/ui/
        var rt = ""
        val cookies: Map<String, String>?
        var token: String? = ""
        if (false) {//强制SSO//原来是[port==1] 教务系统直接登录
            /*cookies = withContext(Dispatchers.IO) {
                Jsoup.connect("http://us.nwpu.edu.cn/eams/login.action")
                        .headers(headers)//第一步获取cookies
                        .timeout(timeout).method(Connection.Method.GET).execute().cookies()
            }

            responseBody = withContext(Dispatchers.IO) {
                Jsoup.connect("http://us.nwpu.edu.cn/eams/login.action").headers(headers)
                        .cookies(cookies)//第二步骤模拟登录
                        .data("username", xh).data("password", pwd).data("encodedPassword", "")
                        .data("session_locale", "zh_CN")
                        .timeout(timeout).method(Connection.Method.POST).execute().body()
            }
            if (responseBody.contains("欢迎使用西北工业大学教务系统。")) {
                //ok
            } else if (responseBody.contains("已经被锁定")) {
                throw NetworkErrorException("账号被锁定，请稍等后重试")
            } else if (responseBody.contains("密码错误")) {
                throw PasswordErrorException("密码错误哦")
            } else if (responseBody.contains("账户不存在")) {
                throw UserNameErrorException("登录失败，账户不存在。")
            } else if (responseBody.contains("验证码不正确")) {
                throw NetworkErrorException("登录失败，失败尝试过多，请尝试更换网络环境。")
            }*/
        } else {//走统一登录
            var response =
                Jsoup.connect("https://uis.nwpu.edu.cn/cas/login?service=https://students-schedule.nwpu.edu.cn/login/cas?redirect_uri=https://students-schedule.nwpu.edu.cn/ui/")
                    .headers(headers)//第一步获取cookies
                    .timeout(timeout).method(Connection.Method.GET).execute()

            cookies = response.cookies()
            //Copy from SUSTechParser.kt@GGAutomaton,thanks!
            val executionValue =
                response.parse().getElementsByAttributeValue("name", "execution")[0].attributes().get("value")
            response =
                Jsoup.connect("https://uis.nwpu.edu.cn/cas/login?service=https://students-schedule.nwpu.edu.cn/login/cas?redirect_uri=https://students-schedule.nwpu.edu.cn/ui/")
                    .headers(headers).cookies(cookies)//第二步骤模拟登录
                    .data("username", xh).data("password", pwd).data("currentMenu", "1")
                    .data("execution", executionValue).data("_eventId", "submit").data("geolocation", "")
                    .data("submit", "稍等片刻……")
                    .timeout(timeout).method(Connection.Method.POST).execute()

            if (response.statusCode() != 200 && response.statusCode() != 302) {
                throw PasswordErrorException("登录失败。因帐号或密码错误，或失败过多暂时被锁定，请核对或稍后更换网络后重试。[${response.statusCode()}]")
            }

            token = Regex("token=.+$").find(response.url().toString())?.value
            if (token != null) {
                token = token.replace("token=", "")
            } else {
                throw NetworkErrorException("获取 token 失败，这不应该发生。若持续出错请联系维护者。[${response.url()}]")
            }
        }

        //获得学期id
        var response =
            Jsoup.connect("https://students-schedule.nwpu.edu.cn/api/semester/list")
                .header("X-Id-Token", token).header("X-Device-Info", "PC").header("X-Requested-With", "XMLHttpRequest")
                .header("X-Terminal-Info", "PC").header("X-User-Type", "student")
                .ignoreContentType(true)//Jsoup 不支持返回的content-type为 application/json，所以强制忽略
                .timeout(5000).method(Connection.Method.GET).execute()

        rt = response.body()

        //本来不应该解析json的，但是考虑到要获取课表要先知道学期id,所以还是解析了。
        var json = JsonParser.parseString(response.body())
        var semesterId = ""
        if (json.asJsonObject.get("msg").asString == "成功") {
            val semestersName = "秋春夏"
            json.asJsonObject.get("data").asJsonArray.forEach {
                var curSemesterName = it.asJsonObject.get("name").toString()
                //以防后面学校会变更，所以这里不精确匹配
                if (curSemesterName.contains("$semesterYear-") && curSemesterName.contains(semestersName[semesterTerm])) {
                    semesterId = it.asJsonObject.get("id").asString
                }
            }
        } else {
            throw GetTermDataErrorException("没有在教务网站上查找到相关学期信息，请重新选择。若持续出错请联系维护者。")
        }

        response =
            Jsoup.connect("https://students-schedule.nwpu.edu.cn/api/courseList/list/$semesterId")
                .header("X-Id-Token", token).header("X-Device-Info", "PC").header("X-Requested-With", "XMLHttpRequest")
                .header("X-Terminal-Info", "PC").header("X-User-Type", "student")
                .ignoreContentType(true)//Jsoup 不支持返回的content-type为 application/json，所以强制忽略
                .timeout(5000).method(Connection.Method.GET).execute()


        json = JsonParser.parseString(response.body())
        println(json.asJsonObject.get("msg").asString)
        if (json.asJsonObject.get("msg").asString != "成功") {
            throw EmptyException("查询课表信息失败，这不应该发生。若持续出错请联系维护者。[${json.asJsonObject.get("msg").asString}]")
        }
        rt = rt + "<split>" + response.body() + "<split>" + semesterId
        //所以rt其实是 回应1<split>回应2<split>学期id
        return rt
    }

    override fun generateCourseList(): List<Course> {
        val ori = webFunc().split("<split>")
        //回应1<split>回应2<split>学期id
        var json = JsonParser.parseString(ori[0])
        val semesterId = ori[2]

        json.asJsonObject.get("data").asJsonArray.forEach {
            if (it.asJsonObject.get("id").asString == semesterId) {
                _tableName = "西工大 " + it.asJsonObject.get("code").asString // 西工大 2019-2020-秋
                _maxWeek = it.asJsonObject.get("weeks").asJsonArray.count()
                _startDate = it.asJsonObject.get("startDay").asString
            }
        }

        json = JsonParser.parseString(ori[1])

        val courseList = ArrayList<Course>()
        json.asJsonObject.get("data").asJsonArray.forEach { course ->


            //如果为 false，则为网课一类，不会在日历上显示，skip
            if (!course.asJsonObject.get("hasSchedule").asBoolean)
                return@forEach//相当于continue

            course.asJsonObject.get("weekdayUnits").asJsonArray.forEach { unit ->

                val weekList = arrayListOf<Int>()
                //weeks: [1,2,3,5]
                unit.asJsonObject.get("weeks").asJsonArray.forEach { x ->
                    weekList.add(x.asInt)
                }

                Common.weekIntList2WeekBeanList(weekList).forEach {
                    courseList.add(Course(
                        name = course.asJsonObject.get("name").asString,
                        day = unit.asJsonObject.get("weekday").asInt,
                        room = unit.asJsonObject.get("roomString").asString.replace(regex = Regex("(长安|友谊|太仓)校区 "), replacement = ""),
                        teacher = unit.asJsonObject.get("teacherString").asString.toString().trim(),
                        startNode = unit.asJsonObject.get("startUnit").asInt,
                        endNode = unit.asJsonObject.get("endUnit").asInt,
                        startWeek = it.start,
                        endWeek = it.end,
                        type = it.type,
                        credit = course.asJsonObject.get("credit").asFloat,
                        note = course.asJsonObject.get("code").asString
                    ))

                    when {
                        unit.asJsonObject.get("roomString").asString.contains("长安校区") -> {
                            _nodes = 13
                            _timeTableName = "西工大长安"
                        }
                        unit.asJsonObject.get("roomString").asString.contains("友谊校区") -> {
                            _nodes = 12
                            val month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
                            _timeTableName = if (month >= 10 || month <= 4) {
                                "西工大友谊冬(10.1-4.30)"
                            } else {
                                "西工大友谊夏(5.1-9.30)"
                            }
                        }
                        unit.asJsonObject.get("roomString").asString.contains("太仓校区") -> {
                            _nodes = 13
                            _timeTableName = "西工大太仓(未实现)"
                        }
                    }
                }
            }
        }
        println("课表名称：$_tableName\n启用时间表：$_timeTableName\n每天节数：$_nodes\n学期开始日期（务必周一）：$_startDate\n学期周数：$_maxWeek")
        return courseList
    }

    /*override fun generateTimeTable(): TimeTable {
        return TimeTable(name = _timeTableName, timeList = listOf())
    }*/
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

    override fun getMaxWeek(): Int? {
        return _maxWeek
    }

}
