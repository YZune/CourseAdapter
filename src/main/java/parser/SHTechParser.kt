package main.java.parser

import bean.Course
import main.java.bean.TimeDetail
import main.java.bean.TimeTable
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import parser.Parser
import java.lang.Integer.max
import kotlin.math.min

class SHTechParser(source: String) : Parser(source) {

    /**@author trace1729
     * @date 20240915
     * 上海科技大学研究生教务导入
     * https://graduate.shanghaitech.edu.cn/gsapp/sys/yjsemaphome/portal/index.do
     */
    /*
2024年学校的教务系统更新, 之前的 parser 不能使用，于是就在 @auther mhk 的基础上做了些修改。
使用方式：

1. fork 本项目，git clone 到本地，用 IDEA 导入。
2. 在 [研究生综合服务平台](https://graduate.shanghaitech.edu.cn/gsapp/sys/yjsemaphome/portal/index.do) 中在「我的教学」-> 「我的课表」中点开学生课程表
3. 由于课程表是以 iframe 的形式嵌入在网页中，直接 ctrl+s 保存网页不能保存课程表的 html 代码，所以你需要找到 iframe 嵌入的网页链接
   1. （**使用chrome 浏览器** ）右键，审查元素，找到 iframe 这个 tag, 点开对应的连接 (在 `#document (link)` 中)
   2. 打开链接，保存这个网页到本地
4. 在 `src/test/SHTechTest.kt` 下替换为你保存到本地的网页
5. 运行后， 按照指示将终端的内容复制到 [name].wakeup_schedule 文件
6. 使用qq 将文件发送给手机
7. 手机上选择 「其他应用打开」，点击 「导入 wakeup」
8. 在 wakeup 程序界面，点击右上角的 菜单 键选择导入的课程表

Notice:
1. 由于 妮可的开课时间和其他学校不同，你需要手动修改 src/main/java/Generator 下的timePreference变量
2. colorScheme 变量中的值也需要做相应修改 Node -> 13, startData -> xxxx.xx.xx
     */

    /*
Notice by last @auther mhy:
欢迎使用上海科技大学研究生课表导入工具,本科生小朋友请出门左转用树维系统导入工具导入.
登录后,请打开 我的培养-查看课表 再导入.如果右上角用户角色为 答辩秘书,还需要先切换为 研究生.
1.对于研究生选修本科生课的情况,教务系统中显示的课表中可能没有课程的标题信息.
2.对于SIST/SLST/SPST以外的其他学院开设的课程,教务系统中显示的课表中可能没有课程的标题信息.
对于这些情况,课程标题暂且展示为班级+教师信息.
这些问题均出自教务系统的bug,对于未有明确修正说明的情况本工具均“依样”输出.
<>建议自行在我的培养-排课结果查询 利用教室信息查询并手动修正.<>
如果你遇到其他问题,可以带上截图及课表页面HTML发邮件到 y@wanghy.gq .
     */

    override fun getNodes(): Int = 13

    override fun getTableName(): String = "上科大导入"

    override fun generateTimeTable(): TimeTable {
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
        return TimeTable("上科大作息", timeList)
    }

    override fun generateCourseList(): List<Course> {
        val contents = source.split("<head>", "</head>")
        val body = contents.last()
        val course = getCourse(body)
        merge(course)
        return course
    }

    fun getCourse(html: String): ArrayList<Course> {
        val toReturn = ArrayList<Course>()
        val document = Jsoup.parse(html)
        // Getting the course table
        val table = document.getElementById("jsTbl_01")
        // 14 rows
        val trs = table?.select("tr") ?: return arrayListOf()
        for ((row, tr) in trs.withIndex()) {
            val tds = tr.select("td")
            for ((col, td) in tds.withIndex()) {
                val rowspan = td.attr("rowspan").toIntOrNull()
                if (rowspan != null && rowspan == 1) {
                    continue
                }
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

//            println("$timeScale, $courseName, $teacher, $location")

            val weekRange = getWeek(timeScale)

            courseList.add(
                Course(
                    name=courseName,
                    day=col - 1, // need passing row
                    room=location,
                    teacher=teacher,
                    startNode=row,
                    endNode=row,
                    startWeek=weekRange.first(),
                    endWeek=weekRange.last(),
                    type=0,
                    startTime=timeList[row - 1].startTime,
                    endTime=timeList[row - 1].endTime,
                )
            )
        }
        return courseList
    }


    private fun getWeek(
        weekStr: String
    ): List<Int>
    {
        val strNum = "([1-9][0-9]*)"
        val weekPattern1 = "$strNum\\-${strNum}周"
        val weekPattern2 = "${strNum}周"

        val regex = Regex(weekPattern1)
        if (regex.containsMatchIn(weekStr)) {
            val matchGroup = regex.findAll(weekStr)
            return matchGroup.last().groupValues
                .filter { !it.contains("周") }
                .map { it.toInt() }
                .sorted()
        }

        val regex2 = Regex(weekPattern2)
        if (regex2.containsMatchIn(weekStr)) {
            val matchGroup = regex2.findAll(weekStr)
            return matchGroup.last().groupValues
                .filter { !it.contains("周") }
                .map { it.toInt() }
                .sorted()
        }
        return ArrayList()
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
    }
}
