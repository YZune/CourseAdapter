package main.java.parser

import bean.Course
import main.java.bean.TimeDetail
import main.java.bean.TimeTable
import org.jsoup.Connection
import org.jsoup.Jsoup
import parser.Parser
import java.lang.RuntimeException
import java.util.*

// 登录网址
const val loginUrl = "https://jwxt.hhu.edu.cn/jsxsd/xk/LoginToXk"
// 课程表网址
const val scheduleUrl = "https://jwxt.hhu.edu.cn/jsxsd/xskb/xskb_list.do"
// userAgent
const val userAgentStr =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36"
// 登录时需传递此参数
const val loginMethod = "LoginToXk"
// 每个单元格内，单门课程都会用 10 个font元素来记录信息，可以由此计算单元格内包含的课程数
const val fontsEachClass = 10

/**
 * @author LandmineFly
 * @date 2024-02-05
 * @param usr 传入用户名
 * @param pwd 传入密码
 * 河海大学新教务系统
 */
class HHUParser(private val usr: String, private val pwd: String) : Parser("") {

    override fun generateCourseList(): List<Course> {

        // 登录时发给服务器的验证信息需要使用Base64进行加密
        val encodedUsr = Base64.getEncoder().encodeToString(usr.toByteArray())
        val encodedPwd = Base64.getEncoder().encodeToString(pwd.toByteArray())
        val encoded = "$encodedUsr%%%$encodedPwd"

        // 登录
        val response = Jsoup.connect(loginUrl)
            .userAgent(userAgentStr)
            .data("loginMethod", loginMethod)
            .data("userAccount", usr)
            .data("userPassword", "")
            .data("encoded", encoded)
            .ignoreHttpErrors(true)
            .method(Connection.Method.POST)
            .execute()

        if (response.body().contains("该账号不存在或密码错误")) {
            throw RuntimeException("用户名或密码错误，登录失败")
        }

        val cookies = response.cookies()

        // 连接到课程表网页
        val body = Jsoup.connect(scheduleUrl)
            .cookies(cookies)
            .ignoreContentType(true)
            .execute()
            .body()

        // 两个正则均用于解析课程时间及周数
        val regex1 = Regex(""".*\(周\)\[(\d+).*(\d+)节]""")
        val regex2 = Regex("""(\d+)-(\d+)""")

        // 开始解析
        val classList = arrayListOf<Course>()
        val timetable = Jsoup.parse(body).getElementById("timetable")
        val trs = timetable.getElementsByTag("tr")
        // 从第3个tr元素开始才包含有效课程信息，同时最后一个tr也不包含有效信息
        var trIndex = 2
        while (trIndex < trs.size - 1) {
            val tds = trs[trIndex].getElementsByTag("td")
            for (tdIndex in tds.indices) {
                // 获取单个单元格元素
                val cell = tds[tdIndex].getElementsByClass("kbcontent")[0]
                val fontsContent = cell.getElementsByTag("font")
                // 计算单元格内的课程数
                val classTotal = fontsContent.size / fontsEachClass
                var classCounter = 0
                var fontCounter = 0
                while (classCounter < classTotal) {
                    // 获取单元格内课程信息
                    val name = fontsContent[fontCounter++].text()
                    val teacher = fontsContent[fontCounter++].text()
                    val timeAndWeek = fontsContent[fontCounter++].text()
                    fontCounter += 1
                    val room = fontsContent[fontCounter++].text()
                    val day = tdIndex + 1
                    // 对含有课程时间和周数的字符串进行解析
                    val result = regex1.find(timeAndWeek)
                    if (result != null) {
                        // 获取课程时间
                        val (startNode, endNode) = result.destructured
                        // 解析课程周数
                        val subs = timeAndWeek.substringBefore("(周)").split(",")
                        for (sub in subs) {
                            val subResult = regex2.find(sub)
                            if (subResult != null) {
                                // 这是连续周数的情况，比如 1-8,9-12
                                val (startWeek, endWeek) = subResult.destructured
                                classList.add(
                                    Course(
                                        name = name,
                                        day = day,
                                        room = room,
                                        teacher = teacher,
                                        startNode = startNode.toInt(),
                                        endNode = endNode.toInt(),
                                        startWeek = startWeek.toInt(),
                                        endWeek = endWeek.toInt(),
                                        type = 0
                                    )
                                )
                            } else {
                                // 这是非连续周数的情况，比如 1,5,12
                                classList.add(
                                    Course(
                                        name = name,
                                        day = day,
                                        room = room,
                                        teacher = teacher,
                                        startNode = startNode.toInt(),
                                        endNode = endNode.toInt(),
                                        startWeek = sub.toInt(),
                                        endWeek = sub.toInt(),
                                        type = 0
                                    )
                                )
                            }
                        }
                    } else {
                        throw RuntimeException("正则表达式解析错误")
                    }
                    fontCounter += 5
                    classCounter++
                }
            }
            trIndex++
        }
        return classList
    }

    // 上课时间表
    override fun generateTimeTable(): TimeTable {
        return TimeTable(
            name = "河海大学", timeList = listOf(
                TimeDetail(1, "08:00", "08:45"),
                TimeDetail(2, "08:50", "9:35"),
                TimeDetail(3, "09:50", "10:35"),
                TimeDetail(4, "10:40", "11:25"),
                TimeDetail(5, "11:30", "12:15"),
                TimeDetail(6, "14:00", "14:45"),
                TimeDetail(7, "14:50", "15:35"),
                TimeDetail(8, "15:50", "16:35"),
                TimeDetail(9, "16:40", "17:25"),
                TimeDetail(10, "18:30", "19:15"),
                TimeDetail(11, "19:20", "20:05"),
                TimeDetail(12, "20:10", "20:55")
            )
        )
    }

    // 课表名
    override fun getTableName(): String {
        return "Hohai University"
    }

    // 一日课程数
    override fun getNodes(): Int {
        return 12
    }

    // 最大周数
    override fun getMaxWeek(): Int {
        return 20
    }

//    教务系统缺失该信息
//    override fun getStartDate(): String? {
//        return super.getStartDate()
//    }

}