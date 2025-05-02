package main.java.parser

import bean.Course
import parser.Parser

/**
 * 哈尔滨工程大学研究生院
 *
 * 仅根据哈尔滨工程大学研究生院课表解析而来，不代表全部可用
 */
class HrbeuGraduateParser(source: String) : Parser() {
    private val moreTeacher = "一班多师"
    private val brTrimRegex = Regex("(^<br>)|(<br>$)")
    private val numNodeRegex = Regex("(\\d+)(-(\\d+))?")
    private val numWeekRegex = Regex("(\\d+)(-(\\d+))?")
    private val teachRegex = Regex("[(（](.*)[)）]")
    private val gradeRegex = Regex("(\\d+班)")

    override fun generateCourseList(): List<Course> {
        val doc = org.jsoup.Jsoup.parse(source)
        val table1 = doc.getElementById("StuCul_TimetableQry_TimeTable").getElementsByClass("WtbodyZlistS")[0]
        val trs = table1.getElementsByTag("tr")
        val courseList = mutableListOf<Course>()
        trs.forEach { tr ->
            val tds = tr.getElementsByTag("td")
            var dayIndex = 0
            tds.drop(1).forEach { td ->
                val courseSource = td.html().replace(brTrimRegex, "").trim()
                if (courseSource.isNotEmpty()) {
                    courseSource.split(Regex("<br>\\s*<br>")).forEach { course ->
                        val split = course.split("<br>")
                        if (split.size >= 5) {
                            courseList.addAll(parseCourseInfo(split, dayIndex))
                        }
                    }
                }
                dayIndex++
            }
        }
        return courseList
    }

    private fun parseCourseInfo(split: List<String>, dayIndex: Int): MutableList<Course> {
        //去除类型词
        val splitEnd = split.map { it.substringAfter(':').trim() }.toMutableList()
        //获取信息
        //增加班级文字
        val grade = gradeRegex.find(splitEnd[1])?.groupValues?.get(1)
        if (grade != null) {
            splitEnd[0] += "($grade)"
        }

        val resList = mutableListOf<Course>()
        // 获取所有课程区间
        val allNodeList = numNodeRegex.findAll(splitEnd[2]).map { matchResult ->
            val startNode = matchResult.groupValues[1].toInt()
            val endNode = matchResult.groupValues[3].takeIf { it.isNotEmpty() }?.toInt() ?: startNode
            intArrayOf(startNode, endNode)
        }.toMutableList()

        mergeInterval(allNodeList).forEach { (startNode, endNode) ->
            if (moreTeacher in splitEnd[1]) {
                for (item in splitEnd[3].split(";")) {
                    val teacher = teachRegex.find(item)?.groupValues?.get(1) ?: ""
                    resList.addAll(parseItem(item, splitEnd, dayIndex, teacher, startNode, endNode, split))
                }
            } else {
                val teacher = splitEnd[1].substringBeforeLast(" ").trim()
                resList.addAll(parseItem(splitEnd[3], splitEnd, dayIndex, teacher, startNode, endNode, split))
            }
        }

        return resList
    }

    private fun parseItem(
        item: String, splitEnd: MutableList<String>, dayIndex: Int,
        teacher: String, startNode: Int, endNode: Int, split: List<String>
    ): List<Course> {
        // 所有周
        val allWeekList = numWeekRegex.findAll(item).map { matchResult ->
            val startWeek = matchResult.groupValues[1].toInt()
            val endWeek = matchResult.groupValues[3].takeIf { it.isNotEmpty() }?.toInt() ?: startWeek
            intArrayOf(startWeek, endWeek)
        }.toMutableList()

        return mergeInterval(allWeekList).map { (startWeek, endWeek) ->
            Course(
                splitEnd[0], dayIndex, splitEnd[4], teacher, startNode, endNode,
                startWeek, endWeek, 0, note = split.subList(5, split.size).joinToString(),
            )
        }.toList()
    }

    // 合并区间 [1,1]和[2,2] -> [1,2]
    private fun mergeInterval(intervals: MutableList<IntArray>): MutableList<IntArray> {
        // 排序
        intervals.sortBy { it.first() }
        val ans = mutableListOf<IntArray>()

        for (i in intervals.indices) {
            if (ans.none()) ans += intervals[i]
            else {
                val lastTo = ans.last()[1]
                val (from, to) = intervals[i]
                if (lastTo + 1 >= from)
                    ans.last()[1] = lastTo.coerceAtLeast(to)
                else {
                    ans += intervals[i]
                }
            }
        }
        return ans
    }
}