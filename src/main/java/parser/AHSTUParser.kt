package main.java.parser

import org.jsoup.Jsoup
import java.util.Base64
import java.util.TreeMap
import org.jsoup.Connection
import com.google.gson.Gson
import java.util.regex.Pattern
import main.java.bean.TimeDetail
import main.java.bean.TimeTable
import main.java.exception.ServerErrorException
import main.java.parser.supwisdom.SupwisdomParser
import main.java.exception.CheckCodeErrorException

//安徽科技学院
//http://www.ahstu.edu.cn/
class AHSTUParser(source: String) : SupwisdomParser(source = source) {
    //http://www.ahstu.edu.cn/jwc/info/2002/8178.htm
    val timeTable = TimeTable(
        name = "安科时间表", timeList = listOf(
            TimeDetail(1, "08:00", "08:45"),
            TimeDetail(2, "08:55", "09:40"),
            TimeDetail(3, "10:00", "10:45"),
            TimeDetail(4, "10:55", "11:40"),
            TimeDetail(5, "14:00", "14:45"),
            TimeDetail(6, "14:55", "15:40"),
            TimeDetail(7, "16:00", "16:45"),
            TimeDetail(8, "16:55", "17:40"),
            TimeDetail(9, "19:00", "19:45"),
            TimeDetail(10, "19:55", "20:40"),
            TimeDetail(11, "20:50", "21:35")
        )
    )

}


class AHSTUCourseProvider {
    companion object {
        private val HTTP = "http://"
        private val JWXT_HOST = "jwxt.ahstu.edu.cn"
        private val SSO_HOST = "sso.ahstu.edu.cn"
        private val JWXT_URL = "$HTTP$JWXT_HOST"
        private val SSO_URL = "$HTTP$SSO_HOST"
        private val SSO_LOGIN_URL = "$SSO_URL/sso/login?service=$JWXT_URL/eams/login.action"//登录地址
        private val SSO_LOGOUT_URL = "$SSO_URL/sso/logout?service=$JWXT_URL"//退出地址
        private val SSO_CHECK_CODE_URL = "$SSO_URL/sso/code/code.jsp"//验证码地址
        private val SSO_CHECK_CODE_VERIFY_URL = "$SSO_URL/sso/code/validationCode.jsp"//验证 验证马是否正确
        private val JWXT_DATA_QUERY_URL = "$JWXT_URL/eams/dataQuery.action"
        private val CHECK_CODE_EX = "验证码错误"
        private val SERVER_ERROR_EX = "服务器发生改变，需要适配者重新适配"
        private val GET = Connection.Method.GET
        private val POST = Connection.Method.POST
    }


    //1.后续获取验证码及登录需要使用此Cookies
    private var Cookies = Jsoup.connect(SSO_LOGIN_URL)
        .method(GET)
        .execute()
        .cookies()

    //2.获取验证码
    data class VerifyCodeRes(
        var msg: String,
        var success: Boolean
    )

    fun getCaptchaImage() = Jsoup.connect(SSO_CHECK_CODE_URL)
        .cookies(Cookies)
        .ignoreContentType(true)
        .execute().bodyAsBytes()!!

    //3.登录 获取cookies
    fun login(userId: String, userPw: String, captchaCode: String) {
        if (captchaCode.length != 4)
            throw CheckCodeErrorException(CHECK_CODE_EX)

        val verifyCodeResTp: String = Jsoup.connect("$SSO_CHECK_CODE_VERIFY_URL?code=$captchaCode")
            .cookies(Cookies)
            .execute()
            .body()

        /**
         *成功
        {
        "msg": "",
        "success": true
        }

         *  错误
        {
        "msg": "验证码输入错误",
        "success": false
        }
         */
        val verifyCodeRes = Gson().fromJson(verifyCodeResTp, VerifyCodeRes::class.java)
        if (!verifyCodeRes.success)
            throw CheckCodeErrorException(CHECK_CODE_EX)

        val passwordB64 = Base64.getEncoder().encodeToString(userPw.toByteArray())

        val loginRes = Jsoup.connect(SSO_LOGIN_URL)
            .method(GET)
            .cookies(Cookies)
            .data("username", userId)
            .data("password", passwordB64)
            .data("validatePass", passwordB64)
            .data("code", captchaCode)
            .data("lt", "e1s1")
            .data("_eventId", "submit")
            .followRedirects(false)
            .execute()

        val headerName = "Location"
        if (!loginRes.hasHeader(headerName))
            throw ServerErrorException(SERVER_ERROR_EX)

        val realLoginUrl = loginRes.header(headerName)
        Cookies = Jsoup.connect(realLoginUrl)
            .method(Connection.Method.GET)
            .followRedirects(false)
            .ignoreHttpErrors(true)
            .execute()
            .cookies()

    }

    //4.获取导入的选项 例如2022-2023 第一学期 2022-2023 第二学期 ...
    data class Semester(
        val id: Int,
        val schoolYear: String,//学年 2022-2023
        val name: String //第name学期
    )

    data class SemesterCalendar(
        val yearDom: String,
        val termDom: String,
        var semesters: TreeMap<String, ArrayList<Semester>>
    )

    data class ImportOption(
        val name: String,//学期的名称，例如：2022-2023 来自于semester中的第一个schoolYear一样
        val semester: ArrayList<Semester>//学期及对应的id
    )

    /**获取导出选项
     *
     * 学期的编号 ：学期的名称
     * name: [1, 2]
     * 将选中的学期作为参数传入下一步
     */
    fun getImportOption(): ArrayList<ImportOption> {
        //需要先访问这个地址才能获得学期选项
        //2023/4/26 将此结果缓存，否则在 getCourseHtml 中服务器出现 `请不要点击过快`的错误
        courseTable = Jsoup.connect("http://jwxt.ahstu.edu.cn/eams/courseTableForStd.action")
            .cookies(Cookies)
            .method(GET)
            .execute()
            .body()

        val body = Jsoup.connect(JWXT_DATA_QUERY_URL)
            .method(POST)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .data("dataType", "semesterCalendar")
            .cookies(Cookies)
            .execute().body()
        val json = Gson().fromJson(body, SemesterCalendar::class.java)

        //排序
        json.semesters = TreeMap<String, ArrayList<Semester>>(compareBy {
            it.substring(1).toInt()
        }).apply {
            this.putAll(json.semesters)
        }

        return arrayListOf<ImportOption>().apply {
            json.semesters.forEach { (_, v) ->
                v.let {
                    if (it.size > 0) {
                        val name = it[0].schoolYear

                        this.add(ImportOption(name, it))
                    }
                }
            }
        }

    }

    //5.获取对应id的课程表
    private lateinit var courseTable: String //getImportOption 中缓存此变量
    fun getCourseHtml(semester: Semester): String {
        /**
         * @param semester: 选中的学期
         */
        val ids_pattern = Pattern.compile("addInput\\(form,\"ids\",\"(\\d+)\"\\)")
        val ids_matcher = ids_pattern.matcher(courseTable)
        ids_matcher.find()
        val ids: String
        try {
            ids = ids_matcher.group(1)
        } catch (e: IllegalStateException) {
            throw ServerErrorException("获取课表 ids 时出错")
        }
        val semesterId: String = semester.id.toString()

        val courseHtml: String = Jsoup.connect("$JWXT_URL/eams/courseTableForStd!courseTable.action")
            .method(Connection.Method.GET)
            .cookies(Cookies)
            .data("setting.kind", "std")
            .data("semester.id", semesterId)
            .data("ids", ids)
            .execute()
            .body()

        val html = Jsoup.parse(courseHtml)
        val js = html.selectFirst("div#ExportA > script")
        return js.html()
    }
}
