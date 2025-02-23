// 导入所需的包和类
package parser

import bean.Course
import org.jsoup.Jsoup

// 黑龙江大学的课程表解析器
class HLJUParser(source: String) : Parser(source) {

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
        // 初始化课程时间的起始节点、步长、起始周和结束周
        var startNode = 0
        var step = 0
        var startWeek = 0
        var endWeek = 0
        var node = false
        var weekIf = false

        val tempSplit = courseSource[0].split(" ")
        val courseSource = tempSplit
        // 解析课程名称
        val courseName = courseSource[0].substringBeforeLast('【')
        // 解析教师姓名
        val teacher = courseSource[1].substringAfter("[").substringBeforeLast(']')
        // 解析教室信息，如果courseSource的第三个元素不为空且非空白，则使用它；否则，从第二个元素中提取
//        val room = if (courseSource.size > 2 && courseSource[2].trim().isNotBlank()) {
//            courseSource[2].trim()
//        } else {
//            val tmp = courseSource[1].split(' ')
//            tmp[tmp.size - 1] // 取第二个元素中的最后一个单词作为教室信息
//        }
        // 解析课程时间信息
//        val timeStr
        val room = courseSource[2].substringAfter("][").substringBeforeLast(']')
        val weekList = courseSource[2].substringAfter('[').substringBeforeLast('周').split(",")
        val nodeStr = courseSource[3].substringAfter('第').substringBeforeLast('节') // 分割节点信息

        // 解析节点信息
        val nodeList = nodeStr.split('-')
        if (nodeList.size == 1) {
            if(!nodeList[0].isEmpty() && nodeList[0][0].isDigit()) {
                startNode = nodeList[0].toInt() // 只有一个节点时，起始节点即为该节点
                step = 1 // 步长为1
                node = true
            }
        } else {
            startNode = nodeList[0].toInt() // 有多个节点时，起始节点为第一个节点
            step = nodeList[1].toInt() - startNode + 1 // 计算步长
            node = true
        }

            // 遍历周次信息，为每个周次创建一个Course对象并添加到courseList中
            weekList.forEach {
                if (it.contains('-')) {
                    var weeks = it.split('-') // 分割起始周和结束周
                    if (weeks.isNotEmpty()) {
                        if(weeks[0][0].isDigit()) {
                            startWeek = weeks[0].toInt() // 起始周
                            weekIf = true
                        }
                    }
                    if (weeks.size > 1) {
                        endWeek = weeks[1].toInt() // 结束周
                    }
                } else {
                    if(it[0].isDigit()) {
                        startWeek = it.toInt() // 只有一个周次时，起始周和结束周相同
                        endWeek = it.toInt()
                        weekIf = true
                    }
                }
                if (node && weekIf) {
                    // 创建Course对象并添加到courseList中
                    courseList.add(
                        Course(
                            name = courseName, room = room,
                            teacher = teacher, day = day,
                            startNode = startNode, endNode = startNode + step - 1,
                            startWeek = startWeek, endWeek = endWeek,
                            type = 0 // 课程类型，这里假设为0，具体含义可能依赖于Course类的定义
                        )
                    )
                }

            }

    }

}