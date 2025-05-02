// 导入所需的包和类
package parser

import bean.Course
import org.jsoup.Jsoup

// 黑龙江大学的课程表解析器
class HLJUParser(source: String) : Parser() {

    // 重写generateCourseList方法，用于生成课程列表
    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>() // 创建一个空的课程列表
        val doc = Jsoup.parse(source, "utf-8") // 使用Jsoup解析传入的HTML字符串
        // 通过特定的属性值查找table元素中的tbody元素
        val tBody = doc.getElementsByClass("ivu-table-tbody")[0]
        val trs = tBody.getElementsByTag("tr") // 获取tbody中的所有tr元素
        var flag = 0
        // 遍历每一行tr元素
        for (tr in trs) {
            val tds = tr.getElementsByTag("td") // 获取当前tr中的所有td元素
            var day = 0 // 初始化星期几的计数器
            // 遍历每一个td元素
            for (td in tds) {

                val courseS = td.html() // 获取课程信息
                val doc = Jsoup.parse(courseS)
                val elem = doc.select("div.codedd-wrap")
                if (!elem.isEmpty()) {
                    for( index in elem.indices){
                        val courseSource = elem[index].text().split("\n")
                        convertHLJU(day, courseSource, courseList) // 调用convertHNIU方法处理课程信息
                    }
                } else {
                    day++
                    continue
                }
                day++
            }
        }
        return courseList // 返回解析后的课程列表
    }

    // convertHNIU方法用于将解析出的课程信息转换为Course对象，并添加到courseList中
    private fun convertHLJU(day: Int, courseSource: List<String>, courseList: MutableList<Course>) {
        var startNode = 0
        var step = 0
        var startWeek = 0
        var endWeek = 0
        var node = false
        var weekIf = false
        var type = 0 // 0表示全周，1表示单周，2表示双周

        val tempSplit = courseSource[0].split(" ")
        val courseSource = tempSplit
        val courseName = courseSource[0].substringBeforeLast('【')
        val teacher = courseSource[1].substringAfter("[").substringBeforeLast(']')
        val room = courseSource[2].substringAfter("][").substringBeforeLast(']')
        val weekList = courseSource[2].substringAfter('[').substringBeforeLast('周').split(",")
        val nodeStr = courseSource[3].substringAfter('第').substringBeforeLast('节')

        val nodeList = nodeStr.split('-')
        if (nodeList.size == 1) {
            if(!nodeList[0].isEmpty() && nodeList[0][0].isDigit()) {
                startNode = nodeList[0].toInt()
                step = 1
                node = true
            }
        } else {
            startNode = nodeList[0].toInt()
            step = nodeList[1].toInt() - startNode + 1
            node = true
        }

        weekList.forEach {
            var weeks = it.split('-')
            if (it.contains("单")) {
                type = 1 // 单周
                weeks = it.replace("单", "").split('-')
            } else if (it.contains("双")) {
                type = 2 // 双周
                weeks = it.replace("双", "").split('-')
            } else {
                type = 0 // 全周
            }

            if (weeks.isNotEmpty()) {
                if(weeks[0][0].isDigit()) {
                    startWeek = weeks[0].toInt()
                    weekIf = true
                }
            }
            if (weeks.size > 1) {
                endWeek = weeks[1].toInt()
            }

            if (node && weekIf) {
                courseList.add(
                    Course(
                        name = courseName, day = day,
                        room = room, teacher = teacher,
                        startNode = startNode, endNode = startNode + step - 1,
                        startWeek = startWeek, endWeek = endWeek,
                        type = type,
                        // 设置单双周类型
                    )
                )
            }
        }
    }
}
