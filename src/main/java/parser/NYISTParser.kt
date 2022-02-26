package main.java.parser

import Common
import bean.Course
import org.jsoup.Jsoup
import parser.Parser
import java.io.File

class NYISTParser(source: String) : Parser(source) {
    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()
        val doc = Jsoup.parse(source)
        val courseItems = doc.getElementsByClass("course-content")
        courseItems.forEach{
            val courseRecords = it.getElementsByClass("course-item-list")
            val courseName = it.getElementsByClass("name").text()
            courseRecords.forEach {
                val teacher = it.select("div.teacher > p.content").text()
                var room = "网络课/实验课"
                if(it.select("div.address > p.content").text()!=""){
                    room = it.select("div.address > p.content").text()
                }
                var timeBuff ="第[19]周 周日 5-6节 14:30~16:00".split(" ")
                if(it.select("div.time > p.content").text()!=""){
                    timeBuff = it.select("div.time > p.content").text().split(" ")
                }
                val day= Common.getWeekFromChinese(timeBuff[timeBuff.size-3])
                val nodes = timeBuff[timeBuff.size-2].substringBefore('节').split('-')
                val startNode = nodes.first().toInt()
                val endNode = nodes.last().toInt()
                var weekText = ""
                val weekList  = mutableListOf<String>()
                for(i in 0..timeBuff.size-4){
                    weekText += timeBuff[i]
                    weekList.add(timeBuff[i])
                }
                var type = 0
                if (weekText.contains('单')) {
                    type = 1
                } else if (weekText.contains('双')) {
                    type = 2
                }
                weekList.forEach{
                    val newit=it.substringAfter("第").substringBefore("周")
                    val weeks=newit.substringAfter("[").substringBefore("]").split("-")
                    val startWeek = weeks.first().toInt()
                    val endWeek = weeks.last().toInt()
                    courseList.add(Course(name = courseName, day = day, room = room, teacher = teacher,
                        startNode = startNode, endNode = endNode, startWeek = startWeek, endWeek = endWeek,
                        type = type))
                }
            }
        }
        return courseList
    }
}
fun main() {
    val file = File("D:/project/NYISTOSUG/CourseWeb/DP/我的课表.html")
    val parser = NYISTParser(file.readText())
    parser.saveCourse()
}