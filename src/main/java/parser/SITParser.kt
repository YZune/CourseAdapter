package parser

import bean.Course
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class SITParser(source: String) : Parser(source) {

    private fun isBlankWithoutIndex(elements: Elements): Boolean {
        val str = StringBuffer()
        for (i in 1 until elements.size) {
            str.append(elements[i].text())
        }
        return str.trim().isEmpty()
    }

    override fun generateCourseList(): List<Course> {
        val allResults = ArrayList<Course>() //用于存放所有的课程
        val doc: Document = Jsoup.parse(source.substringAfterLast("<body>"))
        val tableElement: Element = doc.getElementsByTag("form").first()
        val tables: Elements = tableElement.getElementsByTag("table")

        //以下代码开始解析课表
        val tableMain = tables[0]   //获取课程表的主表格
        val trs: Elements = tableMain.getElementsByTag("tr")    //获取课程表中的每一行
        //从第二行开始为第一节课
        for (i in 2 until trs.size) {
            //获取每节课在整个星期的课程列表
            val nodeCourses: Elements = trs[i].getElementsByTag("td")
            //若为第一节课，或者该节课每周都有，或者不为空
            if (i == 2 || nodeCourses.size == 8) {
                parseCoursesByTrs(nodeCourses).forEach {
                    allResults.add(it)
                }
            } else if (!isBlankWithoutIndex(nodeCourses)) {
                val lastNodeCourses: Elements = trs[i - 1].getElementsByTag("td")
                parseCourseByTrsWhenTdNumIsNot8(lastNodeCourses, nodeCourses).forEach {
                    allResults.add(it)
                }
            }

        }

        //以下代码应用调课信息
        //暂时懒得写
        return allResults
    }

    private fun getRowSpan(td: Element): Int {
        val rowSpan = td.attributes().get("rowspan")
        return if (rowSpan.isEmpty()) {
            1
        } else {
            Integer.parseInt(rowSpan.toString())
        }
    }

    //当一行中td的数目不等于8时，即不能与星期一一对应时，使用本解析器
    private fun parseCourseByTrsWhenTdNumIsNot8(lastLineCourses: Elements, lineCourses: Elements): List<Course> {
        val emptyCourse = Element("td")
        val fullElements = Elements()
        val iter = lineCourses.iterator()
        lastLineCourses.forEach {
            val rowSpan = getRowSpan(it)
            if (rowSpan == 1 && iter.hasNext()) {
                fullElements.add(iter.next())
            } else {
                fullElements.add(emptyCourse)
            }
        }
        return parseCoursesByTrs(fullElements)
    }


    //解析每节课在一周的课程列表，传入每一行的Elements，表格中的一个tr标签，下面有许多td标签
    private fun parseCoursesByTrs(lineCourses: Elements): List<Course> {
        val result = ArrayList<Course>()
        val startNode = Integer.parseInt(lineCourses[0].text())      //开始于第几节课
        for (i in 1 until lineCourses.size) { //i的含义为星期几
            val rowSpan = lineCourses[i].attributes().get("rowspan")
            if (rowSpan.isEmpty()) {
                //如果某天没课，那么直接跳过本次循环
                continue
            }
            val nodeLength = Integer.parseInt(rowSpan)
            val endNode = startNode + nodeLength - 1
            val course = lineCourses[i].getElementsByAttributeValue("name", "d1")
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
    private fun parseCourseElement(courseDiv: Element, day: Int, startNode: Int, endNode: Int): Course {
        val courseInfos = courseDiv.text().split(' ')
        val name = courseInfos[0]

        val weekParser = WeekParser(courseInfos[1])
        val startWeek = weekParser.getStartWeek()
        val endWeek = weekParser.getEndWeek()
        val type = weekParser.getSingleDouble()

        val room = courseInfos[2]
        // 将课程序号与老师名称合并到一个teacher属性中
        // val teacher = courseInfos[3] + ' ' + courseInfos[4]
        val teacher = courseInfos[4]
        return Course(name, day, room, teacher, startNode, endNode, startWeek, endWeek, type)
    }

    class WeekParser(val source: String) {
        //消除单双周数据，掐头去尾
        private val mySource = source.replace("*", "").substring(1).removeSuffix("周")
        private val EVERY_WEEK: Int = 0
        private val SINGLE_WEEK: Int = 1
        private val DOUBLE_WEEK: Int = 2

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
                return DOUBLE_WEEK
            }
            if (source.contains("*")) {
                return SINGLE_WEEK
            }
            return EVERY_WEEK
        }

    }
}
