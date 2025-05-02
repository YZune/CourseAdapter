package main.java.parser

import bean.Course
import com.google.gson.Gson
import org.jsoup.Connection
import org.jsoup.Jsoup
import parser.Parser
import java.util.regex.Pattern

//年份为学年的起始年，学期[秋、春、夏]分别对应[1、2、3]，例如2021年夏季学期选择[2020 3]
class SUSTechParser(
    private val sid: String,
    private val pwd: String,
    private val semesterYear: String,
    private val semesterTerm: String
) : Parser() {

    override fun generateCourseList(): List<Course> {
        val userAgent =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36"
        val timeout = 5000

        //loginTIS
        val casURL = "https://cas.sustech.edu.cn/cas/login?service=https%3A%2F%2Ftis.sustech.edu.cn%2Fcas"
        val doc = Jsoup.connect(casURL).userAgent(userAgent).timeout(timeout).get()
        val executionValue = doc.getElementsByAttributeValue("name", "execution")[0].attributes().get("value")
        val cookies: Map<String, String>?
        try {
            val response = Jsoup.connect(casURL)
                .userAgent(userAgent)
                .data("username", sid)
                .data("password", pwd)
                .data("execution", executionValue)
                .data("_eventId", "submit")
                .data("geolocation", "")
                .timeout(timeout)
                .ignoreHttpErrors(true)
                .method(Connection.Method.POST)
                .execute()
            if (response.body().contains("必须录入用户名") || response.body().contains("必须录入密码")) {
                throw Exception("必须录入用户名和密码")
            }
            if (response.body().contains("认证信息无效")) {
                throw Exception("认证信息无效")
            }
            if (response.statusCode() !in 200..399) {
                throw Exception("HttpStatusCode " + response.statusCode())
            }
            cookies = response.cookies()
            cookies.remove("TGC")
        } catch (e: Exception) {
            throw Exception("登录失败\n$e")
        }

        //getCourseList
        val rd = Jsoup.connect("https://tis.sustech.edu.cn/xszykb/queryxszykbzong")
            .userAgent(userAgent)
            .header("Accept", "*/*")
            .header("X-Requested-With", "XMLHttpRequest")
            .referrer("https://tis.sustech.edu.cn/authentication/main")
            .cookies(cookies)
            .data("xn", "$semesterYear-" + (Integer.parseInt(semesterYear) + 1).toString())
            .data("xq", semesterTerm)
            .timeout(timeout)
            .ignoreContentType(true)
            .method(Connection.Method.POST)
            .execute().body()

        //parseCourseList
        val gson = Gson()
        val courseInfoList = gson.fromJson(rd, Array<CourseInfo>::class.java).asList()

        if (courseInfoList.isEmpty()) {
            throw Exception("未找到课表信息")
        }

        val result = ArrayList<Course>()

        for (course in courseInfoList) {
            if (course.KEY == "bz") {
                //忽略备注栏的课程
                continue
            }
            result.addAll(parseCourseInfo(course.SKSJ, course.KEY))
        }
        return result
    }

    private fun parseCourseInfo(str: String, pos: String): List<Course> {
        val result = ArrayList<Course>()
        val name = str.substringBefore('[').trim()

        var pattern = Pattern.compile("\\[(.*?)]")
        var matcher = pattern.matcher(str)
        val arr = ArrayList<String>()
        while (matcher.find()) {
            arr.add(matcher.group(1))
        }
        if (arr.size != 5) {
            throw Exception("数据格式不匹配")
        }

        var noteCourseClass = ""
        if (arr[1].contains('-')) {
            noteCourseClass = arr[1].substring(arr[1].indexOf('-') + 1)
        }

        val day: Int
        pattern = Pattern.compile("xq(\\d+)_jc\\d+")
        matcher = pattern.matcher(pos)
        if (matcher.find()) {
            day = Integer.parseInt(matcher.group(1))
        } else {
            throw Exception("数据格式不匹配")
        }

        val nodeNum = arr[4].replace("[第节]".toRegex(), "")
        val startNode: Int
        val endNode: Int
        if (nodeNum.contains('-')) {
            startNode = Integer.parseInt(nodeNum.substringBefore('-'))
            endNode = Integer.parseInt(nodeNum.substringAfter('-'))
        } else {
            startNode = Integer.parseInt(nodeNum)
            endNode = startNode
        }

        val weekList = arr[2].replace("周", "").split(',')
        for (item in weekList) {
            val type = when {
                item.endsWith("单") -> 1
                item.endsWith("双") -> 2
                else -> 0
            }
            val weekNum = item.replace("[单双]".toRegex(), "")
            val startWeek: Int
            val endWeek: Int
            if (weekNum.contains('-')) {
                startWeek = Integer.parseInt(weekNum.substringBefore('-'))
                endWeek = Integer.parseInt(weekNum.substringAfter('-'))
            } else {
                startWeek = Integer.parseInt(weekNum)
                endWeek = startWeek
            }
            result.add(
                Course(
                    name,
                    day,
                    arr[3],
                    arr[0],
                    startNode,
                    endNode,
                    startWeek,
                    endWeek,
                    type,
                    note = noteCourseClass,
                )
            )
        }
        return result
    }

    data class CourseInfo(
        val KCWZSM: String?,
        val RWH: String?,
        val FILEURL: String?,
        val SKSJ: String,
        val XB: Int?,
        val SKSJ_EN: String?,
        val KEY: String
    )
}
