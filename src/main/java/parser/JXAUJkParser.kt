package main.java.parser

import bean.Course
import com.google.gson.Gson
import org.jsoup.Connection
import org.jsoup.Jsoup
import parser.Parser
import java.lang.Exception
import java.util.regex.Pattern

/*
* @author mrwoowoo
* @description 江西农业大学接口版倒入，内容更完整
 */
class JXAUJkParser(private val username:String, private val password:String) : Parser("") {
    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()

        val loginCon = Jsoup.connect("http://jwgl.jxau.edu.cn/User/CheckLogin")
            .followRedirects(false)
            .header("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0))")
            .data("UserName", username)
            .data("PassWord", password)
            .method(Connection.Method.POST)
        val loginResp = loginCon.execute()
        val mainUrl = loginResp.header("Location").split("/")
        val gid = mainUrl[mainUrl.size-1]
        val cookies = loginResp.cookies()
        if(mainUrl[1] != "Main") {
            throw Exception("登陆失败")
        }
        val semesterId = getSemesterId(cookies, gid)
        val courseTable = getCourseTable(cookies, gid, semesterId)
        for (course in courseTable) {
            val week = course.SkZhou.split(",")
            val name = course.KcMc
            val day = course.XingQi.split(".")[0].toInt()
            val room = course.Skdd
            val teacher = course.Rkls
            val nodeReg = Pattern.compile("""(\d+)-(\d+)节""")
            val nodes = nodeReg.matcher(course.Jieci)
            if(!nodes.find()) {
                throw Exception("课程节点错误")
            }
            val startNode = nodes.group(1).toInt()
            val endNode = nodes.group(2).toInt()
            for(reg in week) {
                var startWeek: Int
                var endWeek: Int
                if (reg.contains("-")) {
                    val allweek = reg.split("-")
                    startWeek = allweek[0].toInt()
                    endWeek = allweek[1].toInt()
                } else {
                    startWeek = reg.toInt()
                    endWeek = startWeek
                }
                val c = Course(
                    name = name, day = day, room = room, teacher = teacher, startNode = startNode,
                    endNode = endNode, startWeek = startWeek, endWeek = endWeek, type = 0
                )
                courseList.add(c)
            }
        }

        return courseList
    }

    data class CourseTableInfo(
        val Message : String,
        val Result : String,
        val Data : List<CourseTableData>,
        val OtherData : String,
        val totalCount : String,
        val success : String
    )

    data class CourseTableData(
        val ID : String,
        val Skdx : String,
        val KkDwDm : String,
        val KkDw : String,
        val KcDm : String,
        val KcMc : String,
        val Jxbbh : String,
        val Jxb : String,
        val SkZhou : String,
        val Rkls : String,
        val Sjd : String,
        val SjdText : String,
        val XueQi : String,
        val XingQi : String,
        val Jieci : String,
        val Skdd : String,
        val Jslb : String,
        val KcXz : String,
        val Skrs : String
    )

    private fun getSemesterId(cookies: Map<String, String>, gid: String) : String {
        val semesterCon = Jsoup.connect("http://jwgl.jxau.edu.cn/PaikeManage/KebiaoInfo/GetStudentkebiao/$gid")
            .ignoreContentType(true)
            .cookies(cookies)
            .header("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0))")
            .data("treeroot", "treeroot")
            .data("method", "POST")
            .data("node", "treeroot")
            .method(Connection.Method.POST)
        val rawSemesterBody = semesterCon.execute().body()
        val semesterReg = Pattern.compile("""var Dqxq = '(\d+)';""")
        val semesterMatch = semesterReg.matcher(rawSemesterBody)
        if(semesterMatch.find()) {
            return semesterMatch.group(1)
        }else{
            throw Exception("学期错误")
        }
    }

    private fun getCourseTable(cookies: Map<String, String>, gid: String, semesterId: String) : List<CourseTableData>{
        val parser = Gson()
        val semesterCon = Jsoup.connect("http://jwgl.jxau.edu.cn/PaikeManage/KebiaoInfo/GetStudentKebiaoByXq/$gid")
            .ignoreContentType(true)
            .cookies(cookies)
            .header("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0))")
            .data("xq", semesterId)
            .method(Connection.Method.POST)
        val rawCourseTable = semesterCon.execute().body()
        val courseTableJson = parser.fromJson(rawCourseTable, CourseTableInfo::class.java)
        return courseTableJson.Data
    }
}
