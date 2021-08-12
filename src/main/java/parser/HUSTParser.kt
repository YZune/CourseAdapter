package main.java.parser

import Common.getDayInt
import bean.Course
import org.jsoup.Jsoup
import parser.Parser
import java.io.IOException
/**
 * Created by [Xeu](https://github.com/ThankRain) at 2021/8/12 14:50
 * 华中科技大学微校园
 * @link http://hub.m.hust.edu.cn/kcb/index.jsp#kclist_section?kcname=&lsname=
 */
class HUSTParser(source: String) : Parser(source = source) {
    override fun generateCourseList(): List<Course> {
        val doc = Jsoup.parse(source)
        val list = doc.getElementById("ktlist")//Get Course List
        val courses:MutableList<Course> = mutableListOf()
        list.children().toList().forEach { it ->
            val courseName = it.getElementsByTag("Strong").first().text()
            val raws = it.getElementsByTag("p")
            if (raws.size < 4){
                throw IOException("课表格式有误:段落不足")
            }
            val teacher = raws.first().text().run {
                val start = "教师："
                substring(indexOf(start) + start.length)
            }
            val population = raws[1].text()//人数
            val courseObject = raws[2].text()//开课对象
            val timeArranges = it.getElementsByClass("demo-grid")
            timeArranges.forEach { timeArrange->
            timeArrange.children().run {
                val weeks = get(0).text()//周次
                val day = get(1).text()//星期
                val times = get(2).text()//节次
                val place = get(3).text()//地点
                if (!weeks.contains("周次")&&!day.contains("待定")){ //第一个为表头，忽略
                    val startWeek = weeks.run { substring(0,indexOf("-")) }.toInt()
                    val endWeek = weeks.run { substring(indexOf("-")+1) }.toInt()
                    val startNode = times.run { substring(1,indexOf("-")) }.toInt()
                    val endNode = times.run { substring(indexOf("-")+1,indexOf("节")) }.toInt()
                    courses.add(Course(
                        name = courseName,
                        day = getDayInt(day),
                        room = place,
                        teacher = teacher,
                        startNode = startNode,
                        endNode = endNode,
                        startWeek = startWeek,
                        endWeek = endWeek,
                        type = 0,
                        note = "$population\n$courseObject"
                    ))
                }
            }
            }
        }
        return courses
    }
}