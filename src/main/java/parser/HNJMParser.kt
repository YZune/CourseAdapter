package parser

import bean.Course
import main.java.bean.TimeDetail
import main.java.bean.TimeTable
import org.jsoup.Jsoup

/**
 * 适配河南经贸职业学院微信网页端课表
 * @author fanyy0418
 * @date 2022/08/28
 *
 * 直接复制微信端课表页面链接到导入界面即可
 */


class HNJMParser(source: String) : Parser(source) {
    /*
    //覆写时间表
    override fun generateTimeTable(): TimeTable? {
        val timeList : ArrayList<TimeDetail> = arrayListOf(
            TimeDetail(1,"08:10","08:55"),
            TimeDetail(2,"09:05","09:50"),
            TimeDetail(3,"10:15","11:00"),
            TimeDetail(4,"11:10","11:55"),
            TimeDetail(5,"14:40","15:25"),
            TimeDetail(6,"15:35","16:20"),
            TimeDetail(7,"16:40","17:25"),
            TimeDetail(8,"17:35","18:20"),
            TimeDetail(9,"19:00","19:45"),
            TimeDetail(10,"19:55","20:40")
        )
        return TimeTable("河南经贸作息",timeList)
    }

    override fun getTableName(): String? ="河南经贸导入"
    */
    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()
        val doc = Jsoup.parse(source)
        val weeks = doc.getElementsByClass("Sub-kcbt")//获取星期
        val tables = doc.getElementsByTag("table")//获取一天的课
        //val trs=doc.getElementsByTag("tr")//获取课表内容

        var count = 0
        val weekPattern1 = Regex("""\d{1,2}[-]*\d{1,2}周""")
        val roomPattern1 = Regex("""[(|（][图|\d]\d{1,3}""")

        //获取信息
        for (week in weeks) {
            //获取周几
            val weekInt = getWeekInt(week.text())
            //如果存在某天没课，则跳过
            if (week.nextElementSibling().tag().toString() == "br")
                continue
            //提取当天课程
            val table = tables[count++]
            val trs = table.getElementsByTag("tr")
            //获取课程 节数 课程名 0-0周 教室 老师
            for (tr in trs) {
                //提取一节课
                val tds = tr.getElementsByTag("td")
                //获取老师
                val teacherName = tds[2].text()
                //获取节数
                val node = tds[0].text()
                val startNode: Int = node.substring(0, node.indexOf('-')).toInt()
                val endNode: Int = node.substring(node.indexOf('-') + 1, node.length - 1).toInt()
                //课程名字
                var i = 0
                var j = 0
                j = weekPattern1.find(tds[1].text())?.range?.first ?: -1
                val courseName: String = tds[1].text().substring(i, j - 1)
                //开始与结束周
                i = j
                j = weekPattern1.find(tds[1].text())?.range?.last ?: -1
                val week = tds[1].text().substring(i, j)
                val startWeek: Int = week.substring(0, week.indexOf('-')).toInt()
                val endWeek: Int = week.substring(week.indexOf('-') + 1, week.length).toInt()
                //获取教室
                var index1 = roomPattern1.find(tds[1].text())?.range?.first ?: -1
                var index2 = tds[1].text().length-2
                //课程教室为空的情况
                if (index1==-1){
                    index1=tds[1].text().indexOf("；")
                    index2=tds[1].text().indexOf("】")
                }
                val courseRoom: String = tds[1].text().substring(index1 + 1, index2)
                //生成课程
                courseList.add(
                    Course(
                        name = courseName,
                        day = weekInt,
                        room = courseRoom,
                        teacher = teacherName,
                        startNode = startNode,
                        endNode = endNode,
                        startWeek = startWeek,
                        endWeek = endWeek,
                        type = 0
                    )
                )
            }
        }
        return courseList
    }
}

/**
 * 该函数用于将 (String)星期一 转换为 (Int)1
 */
fun getWeekInt(chineseWeek: String): Int {
    val otherHeader = arrayOf("时间", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日", "早晨", "上午", "下午", "晚上")
    for (i in otherHeader.indices) {
        if (chineseWeek.contains(otherHeader[i])) {
            return i
        }
    }
    return -1
}
