package main.java.parser

import bean.Course
import main.java.bean.TimeDetail
import main.java.bean.TimeTable
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import parser.Parser
import java.lang.Integer.max
import kotlin.math.min

/**
 * @author trace1729
 * @date 20240915
 * @email trace1729@gmail.com
 * 上海科技大学研究生教务导入-2024
 **/

// 课程表网页链接 `https://graduate.shanghaitech.edu.cn/gsapp/sys/wdkbappshtech/*default/index.do`

 /**
2024年学校的教务系统更新, 之前的 parser 不能使用，于是就在 @auther mhk 的基础上做了些修改。
使用方式：

1. fork 本项目，git clone 到本地，用 IDEA 导入。
2. 需要配置 jdk17 环境。
3. 访问网页课表 (参加18行)，使用 `CTRL+S` 将网页保存到本地
4. 在 `src/test/SHTechTest.kt` 中替换 File 中的文件路径为你保存在本地的网页路径
5. 运行后， 按照指示将终端的内容复制到 [name].wakeup_schedule 文件
6. 使用qq 将文件发送给手机
7. 手机上选择 「其他应用打开」，点击 「导入 wakeup」
8. 在 wakeup 程序界面，点击右上角的 菜单 键选择导入的课程表

Notice:
1. 为了正确设置作息，你需要手动修改 src/main/java/Generator 下的timePreference变量
2. colorScheme 变量中的值也需要做相应修改 Node -> 13, startDate -> 2024.9.16

1. 缺少单双周的检测
2. 缺少不连续周的识别
**/

class SHTechParser2024(source: String) : Parser() {


    override fun getNodes(): Int = 13

    override fun getTableName(): String = "上科大导入"

    override fun generateTimeTable(): TimeTable {
        val timeList: ArrayList<TimeDetail> = SHTechParser2024.timeList
        return TimeTable("上科大作息", timeList)
    }

    override fun generateCourseList(): List<Course> {
        val contents = source.split("<head>", "</head>")
        val body = contents.last()
        val course = getCourse(body)
        // 合并相邻的两门课程
        merge(course)
        return course
    }

    fun getCourse(html: String): ArrayList<Course> {
        val toReturn = ArrayList<Course>()
        val document = Jsoup.parse(html)
        // 获取课程表
        val table = document.getElementById("jsTbl_01")
        // 课程表有14行，8列
        val trs = table?.select("tr") ?: return arrayListOf()
        for ((row, tr) in trs.withIndex()) {
            val tds = tr.select("td")
            for ((col, td) in tds.withIndex()) {
                val rowspan = td.attr("rowspan").toIntOrNull()
                // rowspan == 1 说明当前单元格没有课程，跳过
                if (rowspan != null && rowspan == 1) {
                    continue
                }
                // 从单元格中提取课程信息
                if (row in 1..13 && col in 2..8) {
                    toReturn.addAll(extractCourseItem(td, row, col))
                }
            }
        }

        return toReturn
    }

    private fun extractCourseItem(td: Element, row: Int, col: Int)
    : ArrayList<Course> {
        val courseItems = td.children()
        val courseList = ArrayList<Course>()

        courseItems.forEach { it ->
            val metadataDivs = it.children()
            val timeScale = metadataDivs[0].text()
            val courseName = metadataDivs[1].text()
            val teacher = metadataDivs[2].text()
            val location = metadataDivs[3].text()

            // 单双周处理
            var typeOfCourse = 0
            if (timeScale.contains("单")) {
                typeOfCourse = 1
            } else if (timeScale.contains("双")) {
                typeOfCourse = 2
            }
            val newTimeScale = timeScale.replace("双", "").replace("单", "")

//            println("$timeScale, $courseName, $teacher, $location")

            // 获取一节课的周次情况
            val weekRange = getWeek(newTimeScale)

            weekRange.forEach {
                courseList.add(
                    Course(
                        name=courseName,
                        day=col - 1, // need passing row
                        room=location,
                        teacher=teacher,
                        startNode=row,
                        endNode=row,
                        startWeek=it.first,
                        endWeek=it.second,
                        type=typeOfCourse,
                        startTime=timeList[row - 1].startTime,
                        endTime=timeList[row - 1].endTime,,
                    )
                )

            }

        }
        return courseList
    }

    private fun getWeek(
        weekStr: String
    ): List<Pair<Int, Int>>
    {
        // 匹配 1-3, 4-10, 14周
        val weekPattern = Regex("""\d+-\d+|\d+""")
        if (weekPattern.containsMatchIn(weekStr)) {
            // Extracting matched groups
            val matchResult = weekPattern.findAll(weekStr)
            val weekRanges = ArrayList<Pair<Int, Int>>()
            matchResult.forEach {
                val match = it.groupValues.first()
                if (match.contains("-"))
                   weekRanges.add(Pair(match.split("-").first().toInt(), match.split("-").last().toInt()))
                else
                    weekRanges.add(Pair(match.toInt(), match.toInt()))
            }
            return weekRanges
        } else {
            return ArrayList()
        }
    }

    private fun merge(data: ArrayList<Course>) {
        var i = 0
        while (i < data.size) {
            val a = data[i]
            var j = i + 1
            while (j < data.size) {
                val b = data[j]
                if (
                       a.name == b.name &&
                       a.startWeek == b.startWeek &&
                       a.endWeek == b.endWeek &&
                       a.room == b.room &&
                       a.teacher == b.teacher &&
                       a.day == b.day
                ) {
                    a.startNode = min(a.startNode, b.startNode)
                    a.endNode = max(a.endNode, b.endNode)
                    data.remove(b)
                } else {
                    j++
                }
            }
            i++
        }
    }

    companion object {
        val timeList: ArrayList<TimeDetail> = arrayListOf(
            TimeDetail(1, "08:15", "09:00"),
            TimeDetail(2, "09:10", "09:55"),
            TimeDetail(3, "10:15", "11:00"),
            TimeDetail(4, "11:10", "11:55"),
            TimeDetail(5, "13:00", "13:45"),
            TimeDetail(6, "13:55", "14:40"),
            TimeDetail(7, "15:00", "15:45"),
            TimeDetail(8, "15:55", "16:40"),
            TimeDetail(9, "16:50", "17:35"),
            TimeDetail(10, "18:00", "18:45"),
            TimeDetail(11, "18:55", "19:40"),
            TimeDetail(12, "19:50", "20:35"),
            TimeDetail(13, "20:45", "21:30")
        )

        // Using this the override the timePreference in `Generator.kt`
        fun timePreference():String {
            var result = "["
            timeList.forEach {
                result += "{" +
                        "\"endTime\": \"${it.endTime}\"," +
                        "\"node\": \"${it.node}\"," +
                        "\"startTime\": \"${it.startTime}\"," +
                        "\"tableTime\": \"${1}\"" +
                        "},"
            }
            result += "]"
            return result.replace("},]","}]")
        }

        // Using this the override the colorScheme in `Generator.kt`
        val colorScheme =
        "{\"background\":\"\",\"courseTextColor\":-1,\"id\":1,\"itemAlpha\":60,\"itemHeight\":64,\"itemTextSize\":12,\"maxWeek\":20,\"nodes\":14,\"showOtherWeekCourse\":true,\"showSat\":true,\"showSun\":true,\"showTime\":false,\"startDate\":\"2024-9-18\",\"strokeColor\":-2130706433,\"sundayFirst\":false,\"tableName\":\"SHTech\",\"textColor\":-16777216,\"timeTable\":1,\"type\":0,\"widgetCourseTextColor\":-1,\"widgetItemAlpha\":60,\"widgetItemHeight\":64,\"widgetItemTextSize\":12,\"widgetStrokeColor\":-2130706433,\"widgetTextColor\":-16777216}"
    
    }
}
