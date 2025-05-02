/**
 * @author: D5error
 * @GitHub: https://www.github.com/D5error
 * @Date: 2024.08.12
 */
package parser

import bean.Course
import org.jsoup.Jsoup
import org.jsoup.nodes.Element as Element

class SYSUParser2024(source: String) : Parser() {
    override fun generateCourseList(): List<Course> {
        val table = Jsoup.parse(source).getElementById("table-bot")
        var courseList = getCourseInfo(table)
        return courseList
    }
    private fun getCourseInfo(table: Element): ArrayList<Course> {
        val dayColspan = getDayColspan(table)
        var timeList = getTimeList(table)
        var ret = ArrayList<Course>()
        var colspanThreshold = getColspanThreshold(dayColspan)
        var tbody = table.getElementsByTag("tbody").first()
//        每个tr标签的从第二个开始td标签都是课程信息
        var trElements = tbody.getElementsByTag("tr")
//        遍历课程表的列，即第几节
        for ((index, tr) in trElements.withIndex()) {
            var section = index + 1
            var currentColspan = 0
            var tdElements = tr.getElementsByTag("td")
            tdElements.removeAt(0) // 删除第一个td标签元素
//            遍历课程表的行，即第几行
            for (td in tdElements) {
                currentColspan += td.attr("colspan").toInt()
                var currentDay = getCurrentDay(currentColspan, colspanThreshold)
                if(td.getElementsByTag("span").isEmpty()) {
                    continue
                }
                var course = Course(
                    name = getCourseName(td),
                    day = currentDay,
                    room = getRoom(td),
                    teacher = getTeacher(td),
                    startNode = section,
                    endNode = getEndNode(td, section),
                    startWeek = getStartWeek(td),
                    endWeek = getEndWeek(td),
                    type = 0,
                    startTime = getStartTime(timeList, section),
                    endTime = getEndTime(timeList, getEndNode(td, section)),
                    // 好像只有每周的情况
                )
                ret.add(course)
            }
        }
        return ret
    }
    private fun getEndTime(timeList: ArrayList<String>, section: Int): String {
        var ret = timeList[section - 1].split("~")[1]
        return ret
    }
    private fun getStartTime(timeList: ArrayList<String>, section: Int): String {
        var ret = timeList[section - 1].split("~")[0]
        return ret
    }
    private fun getTimeList(table: Element): ArrayList<String> {
        var tbody = table.getElementsByTag("tbody").first()
//        每个tr标签的第一个td标签就是时间信息
        var trElements = tbody.getElementsByTag("tr")
        var ret = ArrayList<String>()
        for (tr in trElements) {
            var duration = tr.getElementsByTag("td").first().getElementsByTag("div")[1].text()
            ret.add(duration)
        }
        return ret
    }
    private fun getCourseName(td: Element): String {
        var ret = td.getElementsByTag("span")[1].text()
        ret = ret.substring(5, ret.length - 1)
        return ret
    }
    private fun getRoom(td: Element): String {
        var ret = td.getElementsByTag("span")[3].text()
        ret = ret.substring(0, ret.length - 1)
        return ret
    }
    private fun getTeacher(td: Element): String {
        var ret = td.getElementsByTag("span")[2].text()
        ret = ret.substring(0, ret.length - 1)
        return ret
    }
    private fun getEndNode(td: Element, startNode: Int): Int {
        var ret = startNode + td.attr("rowspan").toInt() - 1
        return ret
    }
    private fun getStartWeek(td: Element): Int {
        var ret = td.getElementsByTag("span")[0].text().split("-")[0].toInt()
        return ret
    }
    private fun getEndWeek(td: Element): Int {
        var ret = td.getElementsByTag("span")[0].text().split("-")[1].split("每周")[0].toInt()
        return ret
    }
    private fun getCurrentDay(currentColspan: Int, colspanThreshold: ArrayList<Int>): Int {
        if(currentColspan <= colspanThreshold[0]){
            return 1
        }
        else if(currentColspan <= colspanThreshold[1]){
            return 2
        }
        else if(currentColspan <= colspanThreshold[2]){
            return 3
        }
        else if(currentColspan <= colspanThreshold[3]){
            return 4
        }
        else if(currentColspan <= colspanThreshold[4]){
            return 5
        }
        else if(currentColspan <= colspanThreshold[5]){
            return 6
        }
        else{
            return 7
        }
    }
    private fun getColspanThreshold(dayColspan: ArrayList<Int>): ArrayList<Int> {
        var ret = ArrayList<Int>()
        for ((index, colspan) in dayColspan.withIndex()) {
            ret.add(colspan)
            if(index >= 1){
                ret[index] += ret[index-1]
            }
        }
        return ret
    }
    private fun getDayColspan(table: Element): ArrayList<Int> {
        val thElements = table.getElementsByTag("th")
        val ret = ArrayList<Int>()
        for (thElement in thElements) {
            val colspan = thElement.attr("colspan")
            if(colspan.isNotEmpty()){
                ret.add(colspan.toInt())
            }
        }
        return ret
    }
    override fun getTableName(): String {
        val title = Jsoup.parse(source).getElementsByTag("h1").first().text()
        return title
    }
}
