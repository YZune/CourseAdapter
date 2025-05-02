package main.java.parser

import bean.Course
import org.jsoup.Jsoup
import parser.Parser

/**
 * Created by [YenalyLiew](https://github.com/YenalyLiew)
 *
 * 重庆邮电大学本科生课表
 *
 * 不是所有类型课表全部适配，比如4节课连上的情况可能有BUG，可以在Github联系我。
 */

class CQUPTParser(source: String) : Parser() {

    companion object {
        // 我这里课程列表在程序里进行修正后长度就是8，
        // 如果你的课表不是很幸运，可能导入就没用或者全乱了。
        private const val COURSE_INFO_SIZE = 8

        // 通常课程的节数是2。
        private const val GENERAL_CLASS_LENGTH = 2

        private val indexWithClassStart = mapOf(0 to 1, 1 to 3, 3 to 5, 4 to 7, 6 to 9, 7 to 11)
        private val indexWithClassEnd = mapOf(0 to 2, 1 to 4, 3 to 6, 4 to 8, 6 to 10, 7 to 12)
    }

    override fun generateCourseList(): List<Course> {
        val doc = Jsoup.parse(source)
        val classesLine = doc.getElementById("stuPanel").select("tr[style=text-align:center]")
        val courseList = ArrayList<Course>()
        // 最寄O(n^4)复杂度，哈哈了。
        // 最外层循环，是先一行一行扫一遍，得到每行的数据。
        // 第二层循环，一行内每个框扫一遍，因为index=0的元素正好没用，所以扫到的索引数值正好对应当前星期数值。
        // 第三层循环，为了防止有的行内的框内有好几个课只能取到第一个，只能循环全取。
        // 最内层循环，有的周是一个周，有的周是好多周，又有的周用逗号隔开，只能靠循环把他们分开。
        classesLine.forEachIndexed { classIndex, classLine ->
            classLine.select("td").forEachIndexed { dayWeek, courses ->
                val coursesInfo = courses.text().replace(" -", "-").split(" ")
                if (coursesInfo.size >= COURSE_INFO_SIZE) {
                    val courseInfoNumber: Int = coursesInfo.size / COURSE_INFO_SIZE
                    for (i in 0 until courseInfoNumber) {
                        val name = coursesInfo[1 + i * COURSE_INFO_SIZE].run { substring(indexOf("-") + 1) }
                        val room = coursesInfo[2 + i * COURSE_INFO_SIZE].run { substring(indexOf("：") + 1) }
                        val teacher = coursesInfo[4 + i * COURSE_INFO_SIZE]
                        val credit = coursesInfo[6 + i * COURSE_INFO_SIZE].run { substring(0, indexOf("学分")).toFloat() }
                        val startNode = indexWithClassStart[classIndex] ?: 1
                        val endNode = indexWithClassEnd[classIndex] ?: 2
                        coursesInfo[3 + i * COURSE_INFO_SIZE].split(",").forEach { weeks ->
                            val startWeek: Int
                            val endWeek: Int
                            val type = if (weeks.contains("单周")) 1 else if (weeks.contains("双周")) 2 else 0
                            val classInRow = if (weeks.contains("节连上")) {
                                weeks.run { substring(indexOf("周") + 1, indexOf("节连上")).toInt() }
                            } else 0
                            val realEndNode = if (classInRow != 0) {
                                endNode + classInRow - GENERAL_CLASS_LENGTH
                            } else endNode
                            if (weeks.contains("-")) {
                                startWeek = weeks.run { substring(0, indexOf("-")).toInt() }
                                endWeek = weeks.run { substring(indexOf("-") + 1, indexOf("周")).toInt() }
                            } else {
                                startWeek = weeks.run { substring(0, indexOf("周")).toInt() }
                                endWeek = startWeek
                            }
                            val course = Course(
                                name = name,
                                day = dayWeek,
                                room = room,
                                teacher = teacher,
                                startNode = startNode, // dayWeek 代表星期X
                                endNode = realEndNode,
                                startWeek = startWeek,
                                endWeek = endWeek,
                                type = type, // startWeek-endWeek 代表 x周-y周
                                credit = credit,
                            )
                            courseList.add(course)
                        }
                    }
                }
            }
        }
        return courseList
    }
}