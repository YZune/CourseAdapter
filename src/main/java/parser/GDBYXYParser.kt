package main.java.parser

import Common
import bean.Course
import org.jsoup.Jsoup
import parser.Parser
import java.io.File
import java.util.*
import java.util.regex.Pattern


class GDBYXYParser(source: String) : Parser(source) {

    override fun generateCourseList(): List<Course> {
        val list = ArrayList<Course>()
        var course: Course? = null


        //正则匹配
        //“[2,6-10,14,18周]四[7-8节]双”，获取星期几;获取第几节开始和结束，以及单双周;周\u5468节\u8282
        val pTimeInfo = Pattern.compile("^\\[(.+)\\u5468\\]([\\u4e00-\\u9fa5])\\[(.+)\\u8282\\]([\\u5355,\\u53cc]?)$")

        val doc = org.jsoup.Jsoup.parse(source)
        //println(doc)
        val tablepageRpt = doc.getElementById("pageRpt")
        //println(tablepageRpt)
        //选中含有课程信息的table
        val trueTable = tablepageRpt.child(0).child(0).child(0).child(2)
        //从td元素提取字符串
        val tds = trueTable.getElementsByTag("td")

        //遍历td元素
        for (td in tds) {
            val courseSource = td.text().trim()
            //若td元素内字符串为空则进行下一次循环
            if (courseSource.length <= 1) {
                continue
            }
            //字符串去空格并被<br>分割
            val temp = td.html().trim().split("<br>".toRegex()).toTypedArray()
            //遍历被<br>分割的字符串
            for ((index, brstr) in temp.withIndex()) {
                //若字符串为空则进行下一次循环
                if (brstr.length <= 1) {
                    continue
                }
                //若匹配到类似"[B0241045]JSP"字段则触发
                if (brstr.matches(Regex("^\\[[A-Z][0-9]+\\].+$"))) {
                    //从"]"往后截取字符串
                    val name: String = brstr.substring(brstr.indexOf("]")+1)                 //课程名

                    //temp[brstr的索引值+2]获取时间信息
                    val mTimeInfo = pTimeInfo.matcher(temp[index + 2])
                    if(mTimeInfo.find()){
                        //从时间信息里获取周几
                        val day: Int = Common.getWeekFromChinese("周" + mTimeInfo.group(2))    //该课程的是星期几（7代表星期天）参数范围：1 - 7

                        //temp[brstr的索引值+3]获取教室地方
                        val room: String = temp[index + 3]                                                      //教室
                        //temp[brstr的索引值+1]获取教师名字
                        val teacher: String = temp[index + 1]                                                   //老师
                        //println(teacher)
                        //temp[brstr的索引值+2]获取类似"[3-4节]双"

                        val startNode: Int                                 //开始为第几节课
                        val endNode: Int                                   //结束时为第几节课
                        val NodeNumstr = mTimeInfo.group(3)         //获取类似"[12节]"字段内的"12"
                        //若只有数字则直接str转数字
                        if (NodeNumstr.matches(Regex("^[0-9]{1,2}$"))) {
                            startNode = NodeNumstr.toInt()
                            endNode = NodeNumstr.toInt()
                        } else {
                            //字符串去空格并被"-"分割
                            val strArray = NodeNumstr.trim().split("-".toRegex()).toTypedArray()
                            startNode = strArray[0].toInt()
                            endNode = strArray[1].toInt()
                        }

                        var type: Int               //单双周，每周为0，单周为1，双周为2
                        type = when {
                            mTimeInfo.group(4) == "单" -> {
                                1
                            }
                            mTimeInfo.group(4) == "双" -> {
                                2
                            }
                            else -> {
                                0
                            }
                        }

                        //字符串去空格并被","分割
                        val startendArray = mTimeInfo.group(1).trim().split(",".toRegex()).toTypedArray()
                        //遍历字符串数组
                        for (str: String in startendArray) {
                            val startWeek: Int          //开始周
                            val endWeek: Int            //结束周
                            // 处理开始周和结束周
                            //若只有数字则直接str转数字
                            if (str.matches(Regex("^[0-9]{1,2}$"))) {
                                startWeek = str.toInt()
                                endWeek = str.toInt()
                                course = Course(name, day, room, teacher, startNode, endNode, startWeek, endWeek, type)
                                list.add(course)
                            } else {
                                //字符串去空格并被"-"分割
                                val strArray = str.trim().split("-".toRegex()).toTypedArray()
                                startWeek = strArray[0].toInt()
                                endWeek = strArray[1].toInt()
                                course = Course(name, day, room, teacher, startNode, endNode, startWeek, endWeek, type)
                                list.add(course)
                            }
                        }
                    }
                }
            }
        }
        return list
    }

}

fun main() {
    // 示例中用了相对路径，Windows 下可能需要修改
    // 建议从项目外引用 html 文件
    // 提交时一定不要上传 html 文件，涉及隐私问题
    val file = File("E:\\SoftwareDevelopment\\Github\\CourseAdapter\\课程表2.html")
    val parser = GDBYXYParser(file.readText())
    parser.saveCourse()
}
