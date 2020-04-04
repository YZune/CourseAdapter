package main.java.parser

import bean.Course
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import parser.Parser
import java.io.File

//source参数接收教务系统中的课表frame中的源代码
class SITParser(source: String) : Parser(source) {
    
    val allResults=ArrayList<Course>() //用于存放所有的课程
    init {
        val doc: Document = Jsoup.parse(source)
        val tableElement: Element = doc.getElementsByTag("form").first()
        val tables: Elements = tableElement.getElementsByTag("table")

        //以下代码开始解析课表
        val tableMain = tables.get(0)   //获取课程表的主表格
        val trs: Elements = tableMain.getElementsByTag("tr")    //获取课程表中的每一行
        //从第二行开始为第一节课
        for (i in 2 until trs.size) {
            //获取每节课在整个星期的课程列表
            val nodeCourses = trs[i].getElementsByTag("td")
            parseCoursesByTrs(nodeCourses).forEach{
                allResults.add(it)
            }
        }

        //以下代码应用调课信息
        //暂时懒得写
    }

    override fun generateCourseList(): List<Course> {
        allResults.forEach {
            println(it)
        }
        return allResults
    }

    //解析每节课在一周的课程列表，传入每一行的Elements
    fun parseCoursesByTrs(courses: Elements): List<Course> {
        val result = ArrayList<Course>()
        val startNode = Integer.parseInt(courses[0].text())      //开始于第几节课
        for (i in 1 until courses.size) { //i的含义为星期几
            val rowSpan = courses[i].attributes().get("rowspan")
            if (rowSpan.isEmpty()) {
                //如果某天没课，那么直接跳过本次循环
                continue
            }
            val nodeLength = Integer.parseInt(rowSpan)
            val endNode = startNode + nodeLength - 1
            val course = courses[i].getElementsByAttributeValue("name", "d1")
            course.forEach {
                val courseElement: Course = parseCourseElement(it, i, startNode, endNode)
                result.add(courseElement)
            }
        }
        return result
    }


    /*
       * <pre>

       courseDiv参数传入以下Element：
           <div name="d1" onmouseover="do1(this,45)" onmouseout="do2(this,45)">
           高等数学（工）2
           <br>第1-16周 一教A308
           <br>1956448 李娟
           </div>
       * </pre>
       * */
    fun parseCourseElement(courseDiv: Element, day: Int, startNode: Int, endNode: Int): Course {
        val courseInfos = courseDiv.text().split(' ')
        val name = courseInfos[0]

        val weekParser = WeekParser(courseInfos[1])
        val startWeek = weekParser.getStartWeek()
        val endWeek = weekParser.getEndWeek()
        val type = weekParser.getSingleDouble()

        val room = courseInfos[2]
        //将课程序号与老师名称合并到一个teacher属性中
        val teacher = courseInfos[3] + ' ' + courseInfos[4]
        return Course(name, day, room, teacher, startNode, endNode, startWeek, endWeek, type)
    }

    //用于解析开始，终止周，单双周信息
    //source参数传入原始数据，比如
    //第1-16周**，第1-16周*，第1-16周，第3周
    //其中，**为双周，*为单周，不带星号的是每周
    class WeekParser(val source: String) {
        //消除单双周数据，掐头去尾
        private val mySource = source.replace("*", "").substring(1).removeSuffix("周")

        companion object {
            const val EVERY_WEEK: Int = 0
            const val SINGLE_WEEK: Int = 1
            const val DOUBLE_WEEK: Int = 2
        }


        fun getStartWeek(): Int {
            //有可能一周的这一天仅上一节课
            return if (mySource.contains("-"))
                Integer.parseInt(mySource.split("-")[0])
            else
                Integer.parseInt(mySource)

        }

        fun getEndWeek(): Int {
            return if (mySource.contains("-"))
                Integer.parseInt(mySource.split("-")[1])
            else
                Integer.parseInt(mySource)
        }

        fun getSingleDouble(): Int {
            if (source.contains("**")) {
                return WeekParser.DOUBLE_WEEK
            }
            if (source.contains("*")) {
                return WeekParser.SINGLE_WEEK
            }
            return WeekParser.EVERY_WEEK
        }

    }
}
