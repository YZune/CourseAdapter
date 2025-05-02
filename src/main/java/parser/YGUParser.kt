package main.java.parser

import bean.Course
import com.google.gson.Gson
import com.google.gson.JsonParser
import main.java.bean.TimeDetail
import main.java.exception.GetTermDataErrorException
import org.jsoup.Connection.Method
import org.jsoup.Jsoup
import parser.Parser

/**
 * 阳光学院(大学)教务
 * 系统登录地址：https://ygu.edu.cn/user/login
 * 登陆账号并获取 cookie 即可
 * 若有适配不完善，可在本人fork的项目下提issue
 *
 *
 * 登陆接口 POST https://ygu.edu.cn/api/auth/login/slide/v1
 * {
 *     "redirect": "/",
 *     "password": "0495ef******",
 *     "username": "qgtr****",
 *     "captchaVO": {
 *         "captchaVerification": "****U/58="
 *     }
 * }
 * 接口返回
 * {
 *     "code": 0,
 *     "msg": "请求成功",
 *     "data": {
 *         "access_token": "ff0****04", // 取这个值作为 cookie 传入
 *         "expires_in": **
 *     }
 * }
 *
 * @author gouzil
 * @date 2024/2/16
 */
class YGUParser : Parser {
    // 学期时间
    private var _semesterYear: String = ""

    // 学号
    private var _studentsNumber: String = ""

    // 最大课程数
    private var _nodes = 12
    private var _tableName = "未命名"

        // 学生姓名
    private var _nickName: String = ""
    private val _cookies = HashMap<String, String>()
    private val _headers: HashMap<String, String> = hashMapOf(
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.5111.0 Safari/537.36",
        "content-Type" to "application/json;charset=UTF-8"
    )

    private val _courseDataArrayList = ArrayList<courseData>()

    companion object {
        private const val _baseUrl = "https://ygu.edu.cn/api/"
    }

    // 从教务系统获取当前学期 (优先使用这个)
    constructor(cookie: String) {
        _cookies["Access-Token"] = cookie
        initSemesterYearData()
        initUserData()
        initCoursesData()
    }

    // 传入学期时间
    constructor(cookie: String, semesterYear: String) {
        _cookies["Access-Token"] = cookie
        this._semesterYear = semesterYear
        initUserData()
        initCoursesData()
        this._tableName = semesterYear
    }

    private fun initSemesterYearData() {
        // 学期时间列表
        val semesterYearListJsonStr = Jsoup.connect(_baseUrl + "jw/xlgl/listR")
            .headers(this._headers)
            .method(Method.POST)
            .cookies(this._cookies)
            .requestBody("{}")
            .ignoreContentType(true)
            .execute()
            .body()
        val semesterYearListJson = JsonParser.parseString(semesterYearListJsonStr).asJsonObject
        if (semesterYearListJson["code"].asInt != 0) {
            throw GetTermDataErrorException("获取学期时间列表失败")
        }
        // 这里取当前学期时间
        this._semesterYear = semesterYearListJson["data"].asJsonArray[0].asJsonObject["xqjc"].asString
        this._tableName = semesterYearListJson["data"].asJsonArray[0].asJsonObject["xqqc"].asString
    }

    private fun initUserData() {
        // 获取用户基本信息
        val userInfoJsonStr = Jsoup.connect(_baseUrl + "system/user/getInfo")
            .headers(this._headers)
            .method(Method.GET)
            .cookies(this._cookies)
            .ignoreContentType(true)
            .execute()
            .body()

        val userInfoJson = JsonParser.parseString(userInfoJsonStr).asJsonObject
        if (userInfoJson["code"].asInt != 0) {
            throw GetTermDataErrorException("获取用户基本信息失败")
        }
        this._studentsNumber = userInfoJson["user"].asJsonObject["studentsNumber"].asString
        this._nickName = userInfoJson["user"].asJsonObject["nickName"].asString
    }

    private fun initCoursesData() {
        // 获取课程列表
        val requestData = HashMap<String, String?>()
        requestData["xm"] = _nickName
        requestData["xh"] = _studentsNumber
        requestData["xqjc"] = _semesterYear
        val coursesDataJsonStr = Jsoup.connect(_baseUrl + "jw/kpkglkcb/student").method(Method.POST)
            .headers(this._headers)
            .cookies(this._cookies)
            .requestBody(Gson().toJson(requestData))
            .ignoreContentType(true)
            .execute()
            .body()

        val coursesDataJson = JsonParser.parseString(coursesDataJsonStr).asJsonObject
        if (coursesDataJson["code"].asInt != 0) {
            throw GetTermDataErrorException("获取课程信息失败")
        }
        for (course in coursesDataJson["data"].asJsonArray) {
            for (weekList in course.asJsonObject["weekList"].asJsonArray) {
                for (kcbVoList in weekList.asJsonObject["kcbVoList"].asJsonArray) {
                    this._courseDataArrayList.add(
                        Gson().fromJson(
                            kcbVoList.asJsonObject.toString(),
                            courseData::class.java
                        )
                    )
                }
            }
        }
    }

    override fun generateCourseList(): List<Course> {
        val courseList = ArrayList<Course>()
        // 解析数据
        for (course in this._courseDataArrayList) {
            // 截取第几节课
            val classNode = course.jcs.split(",")
            val startNode = classNode[0].toInt()
            val endNode = classNode[classNode.size - 1].toInt()
            // 截取周数
            val weekList = course.zcs.split(",")
            val startWeek = weekList[0].toInt()
            val endWeek = weekList[weekList.size - 1].toInt()
            // 单双周
            var type = 0
            if (course.dsz == "单") {
                type = 1
            } else if (course.dsz == "双") {
                type = 2
            }
            // 区分南北校区
            val southCampus = campusSelect(course.jsmc)

            courseList.add(
                Course(
                    course.kcmc,
                    course.xq,
                    course.jsmc.replace(" ", ""),
                    course.rkls,
                    startNode,
                    endNode,
                    startWeek,
                    endWeek,
                    type,
                    startTime = defaultTimeList(southCampus, startNode).startTime,  // 没有学分数据
                    endTime = defaultTimeList(southCampus, endNode).endTime,
                )
            )
        }
        return courseList
    }

    // 最大课程数
    override fun getNodes(): Int {
        return _nodes
    }

    // 表名
    override fun getTableName(): String {
        return _tableName
    }

    /**
     * 这个有点问题(处理无效)可能后续不在处理南北校区, 直接重写`generateTimeTable`方法
     * @param southCampus 是否为南校区(仅有南校区和北校区)
     * @param node        第几节课
     */
    private fun defaultTimeList(southCampus: Boolean, node: Int): TimeDetail {
        return when (node) {
            1 -> TimeDetail(1, "08:10", "08:55")
            2 -> TimeDetail(2, "09:00", "09:45")
            3 -> if (southCampus) TimeDetail(3, "10:05", "10:50") else TimeDetail(3, "10:20", "11:05")
            4 -> if (southCampus) TimeDetail(4, "10:55", "11:40") else TimeDetail(4, "11:15", "12:00")
            5 -> TimeDetail(5, "14:00", "14:45")
            6 -> TimeDetail(6, "14:50", "15:35")
            7 -> TimeDetail(7, "15:55", "16:40")
            8 -> TimeDetail(8, "16:45", "17:30")
            9 -> TimeDetail(9, "18:30", "19:15")
            10 -> TimeDetail(10, "19:20", "20:05")
            11 -> TimeDetail(11, "20:10", "20:55")
            else -> TimeDetail(-1, "00:00", "00:00")
        }
    }

    // 南校区返回true, 北校区返回false
    private fun campusSelect(room: String): Boolean {
        return room.contains("文科楼") || room.contains("商学楼") || room.contains("音艺楼") ||
                room.contains("行政楼") || room.contains("操场")
    }


    // 这里的名称与json请求基本保持一致
    data class courseData(
        // 星期
        var xq: Int,
        // 周次
        var zcs: String,
        // 节次
        var jcs: String,
        // 单双周
        var dsz: String,
        // 任课老师
        var rkls: String,
        // 课程名称
        var kcmc: String,
        // 教室名称
        var jsmc: String,
    )
}
