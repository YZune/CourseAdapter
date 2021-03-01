package main.java.parser

import bean.Course
import parser.Parser
import java.io.File

/*
北京交通大学本科
研究生可能不通用
 */
var debug = false

class BJTUParser(source: String) : Parser(source) {
    override fun generateCourseList(): List<Course> {

        //使用jsoup解析源码
        val doc = org.jsoup.Jsoup.parse(source)

        //获取课程表的table
        val table = doc.getElementsByClass("table table-bordered")
        //因为byclass拿到的是一个数组所以[0],获取列trs
        val trs = table[0].getElementsByTag("tr")

        //用的set，存课程信息
        val setOfClasses = hashSetOf<Course>()
        var weekdayNo: Int//代表星期几
        var isFirstLine: Boolean//因为第一个显示的是课程时间段，得筛掉

        for ((timeNo, tr) in trs.withIndex()) {
            val tds = tr.getElementsByTag("td")
            isFirstLine = true
            weekdayNo = 1
            for (td in tds) {
                //比方说获取周一第一节的所有课 的文本信息
                val courseSource = td.text().trim()
                var numOfClass: Int
                var courseSources: List<String> = ArrayList()
                if (courseSource == "") {

                    numOfClass = 0
                } else {
                    val regex = Regex("\\w\\w\\w\\w\\w\\w\\w")
                    courseSources = regex.split(courseSource)

                    numOfClass = courseSources.size - 1
                }


                if(debug)
                    println("$courseSource  $numOfClass")

                if (isFirstLine) {
                    isFirstLine = false
                    continue
                }


                var startWeek = 1
                var endWeek = 16
                var type = 0
                var className = ""
                var classRoom = ""
                var classTeacher = ""
                for (each in courseSources) {
                    if (each == "")
                        continue

                    val regexClassName = Regex("]\\s.+\\s\\[")
                    className = regexClassName.find(each)?.value?.split(" ")?.get(1).toString()

                    val regexClassRoom = Regex("\\S+\\s\\w\\w\\d\\d\\d")
                    classRoom = regexClassRoom.find(each)?.value?.split(" ")?.get(0).toString() + regexClassRoom.find(
                        each
                    )?.value?.split(" ")?.get(1).toString()

                    val regexClassTeacher = Regex("\\d\\d周\\s\\S+")
                    classTeacher = regexClassTeacher.find(each)?.value?.split(" ")?.get(1).toString()

                    val regexStartWeek = Regex("]\\s第\\d\\d")
                    startWeek = regexStartWeek.find(each)?.value?.slice(3..4)?.toInt() ?: 1

                    val regexEndWeek = Regex("\\d\\d周")
                    endWeek = regexEndWeek.find(each)?.value?.slice(0..1)?.toInt() ?: 16

                    if (each.contains(",")){
                        type = if (endWeek%2==0) 2 else 1
                    }else{
                        type = 0
                    }

                }

                if (className!="")
                    setOfClasses.add(
                        Course(
                            name = className,
                            day = weekdayNo,
                            room = classRoom,
                            teacher = classTeacher,
                            startNode = timeNo,
                            endNode = timeNo,
                            type = type,
                            startWeek = startWeek,
                            endWeek = endWeek
                        )
                    )

                weekdayNo++


            }
        }


        //整合进result里
        val result = arrayListOf<Course>()
        for (each in setOfClasses) {
            result.add(each)

            if(debug)
                println(each)
        }
        return result
    }

}

fun main() {


    val file = File("C:\\Users\\14223\\Desktop\\北京交通大学教学服务管理平台02.html")
//    BJTUParser(file.readText()).generateCourseList()

    val parser = BJTUParser(file.readText())
    parser.saveCourse()
}