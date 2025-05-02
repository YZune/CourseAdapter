package parser

import bean.Course
import main.java.bean.TimeDetail
import main.java.bean.TimeTable
import org.jsoup.Jsoup

/**
 * 因为目前只是开源了自定义课程表解析器，但不知道查看课表的网址信息在哪设置，因此先在这里进行说明
 * 研究生的教务系统没有域名，只有IP地址，因此必须通过校园网或VPN访问
 * 1. 先进入教务系统登录网址：http://202.205.127.146:8881/#/login
 * 2. 然后进入学生课表查询网址：http://202.205.127.146:8881/#/secStudent/schemeManagement/subPage/studentTimetableQuery
 *   （当然，直接进入学生课表查询网址也会直接跳转到登录界面）
 */

//北京电影学院-研究生教育管理系统
class BFAforYJSParser(source: String) : Parser() {
    var maxWeek = 1 // 学期最大周数

    // 课表名称，以“北京电影学院+当前学期”命名
    override fun getTableName(): String {
        val document = Jsoup.parse(source)
        val currentSemesterText = document.getElementsByClass("ant-select-selection-selected-value")[0].text()
        return "北京电影学院 $currentSemesterText"
    }

    // 每天课程节数
    override fun getNodes(): Int = 12

    // 学期课程周数
    override fun getMaxWeek(): Int = maxWeek

    // 学期课程周数
    override fun generateTimeTable(): TimeTable {
        return TimeTable(
            name = "北京电影学院", timeList = listOf(
                TimeDetail(1, "08:30", "09:15"),
                TimeDetail(2, "09:15", "10:00"),
                TimeDetail(3, "10:15", "11:00"),
                TimeDetail(4, "11:00", "11:45"),
                TimeDetail(5, "13:30", "14:15"),
                TimeDetail(6, "14:15", "15:00"),
                TimeDetail(7, "15:15", "16:00"),
                TimeDetail(8, "16:00", "16:45"),
                TimeDetail(9, "18:00", "18:45"),
                TimeDetail(10, "18:45", "19:30"),
                TimeDetail(11, "19:45", "20:30"),
                TimeDetail(12, "20:30", "21:15")
            )
        )
    }

    // 提取课程信息
    override fun generateCourseList(): List<Course> {
        val courseList = mutableListOf<Course>()
        val document = Jsoup.parse(source)
        val tableBox = document.getElementById("table")
        val tableContents =
            tableBox.getElementsByClass("pages-sec-student-scheme-pages-student-timetable-query-timetable-index-tableContent")
        for (element in tableContents) {
            val tdElements = element.select("td[_nk]:not([_nk=''])")
            for (tdElement in tdElements) {
                // 处理课程表的每个格子元素
                val courseText = tdElement.text()
                if (courseText.isNotEmpty()) {
                    // 有课程信息
                    val textList = mutableListOf<String>()
                    val textListElements = tdElement.child(0).children()
                    //  单独提取每一行的课程信息
                    for (textListElement in textListElements)
                    {
                        textList.add(textListElement.text())
                    }
                    textList.add(tdElement.child(1).text())
                    //  存入课程信息
                    val weekday = tdElements.indexOf(tdElement) + 1
                    val campus = textList[0]
                    val courseName = textList[1]
                    val note = textList[2]
                    val teacher = textList[3]

                    // 提取方括号外的内容为教室（classroom）
                    val classroom = textList[4].substringBefore("[")

                    // 提取方括号内的内容并分割为起始周和结束周（startWeek 和 endWeek）
                    val bracketContent = textList[4].substringAfter("[").substringBefore("]")
                    val (startWeek, endWeek) = bracketContent.split("-").map { it.toInt() }

                    // 使用正则表达式匹配数字并转换为整数列表
                    val numbers = "\\d+".toRegex().findAll(textList[5]).map { it.value.toInt() }.toList()

                    // 获取第一个数字作为起始节点（startNode），获取最后一个数字作为结束节点（endNode）
                    val startNode: Int = numbers.firstOrNull()?.toInt() ?: 0
                    val endNode: Int = numbers.lastOrNull()?.toInt() ?: 0

                    // 计算学分（由于课程表界面没有显示学分，因此是通过学时粗略换算的，1学分=16学时，如果存在多节非连续课程，计算结果就不正确了）
                    val credit = ((endWeek - startWeek + 1) * (endNode - startNode + 1) / 16).toFloat()

                    // 更新最大周数
                    maxWeek = if (maxWeek < endWeek) endWeek else maxWeek

                    // 创建课程对象并添加到课程列表
                    courseList.add(
                        Course(
                            name = courseName,
                            day = weekday,
                            room = classroom,
                            teacher = teacher,
                            startNode = startNode,
                            endNode = endNode,
                            startWeek = startWeek,
                            endWeek = endWeek,
                            type = 0,
                            credit = credit,
                            note = note,
                        )
                    )
                }
            }
        }
        return courseList
    }

}

