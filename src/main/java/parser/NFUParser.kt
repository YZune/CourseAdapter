package main.java.parser

import bean.Course
import org.jsoup.Jsoup
import parser.Parser
import java.util.regex.Pattern

val pattern = Pattern.compile("第[0-9]*周")

class NFUParser(source: String):Parser(source) {

    override fun generateCourseList(): List<Course> {

        val courseList = arrayListOf<Course>()
        val courseStrList = arrayListOf<String>()
        var courseStr:String
        var name:String
        var dayOfWeek:Int
        var type:Int
        var startWeek:Int
        var endWeek:Int
        var startNode:Int
        var endNode:Int
        var teacher:String
        var place:String

        val doc = Jsoup.parse(source)
        val trs = doc.select("tr.ui-widget-content")

        for (i in 0 until trs.size){
            courseStrList.clear()
            val tr = trs[i]
            val span = tr.select("span")

            name = span.first().text()

            for (j in 3..9){
                var str = span[j].text()

                if (str != ""){
                    if (str.contains("/")){
                        str = str.replace("/ ","/")
                        val strs = str.split("/").map {
                            it + " ${j-2}"
                        }
                        courseStrList.addAll(strs)
                    }else{
                        courseStrList.add((str+" ${j-2}"))
                    }
                }
            }

            for (c in courseStrList){
                courseStr = c
                if (courseStr.contains("单")){
                    type = 1
                    courseStr = courseStr.replace(" 单周","")
                }else if (courseStr.contains("双")){
                    type =2
                    courseStr = courseStr.replace(" 双周","")
                }else{
                    type = 0
                }

                if (courseStr.contains("第")){
                    val matcher = pattern.matcher(courseStr)
                    if (matcher.find()){
                        val onlyWeek = matcher.group()
                        val onlyWeekNum = onlyWeek.subSequence(1,onlyWeek.length-1)
                        courseStr = courseStr.replace("第${onlyWeekNum}周","${onlyWeekNum}-${onlyWeekNum}周")
                    }
                }

                val info = courseStr.split(" ")

                val weekInfo = info[0].trim('周')
                val nodeInfo = info[1].trim('节')

                val week = weekInfo.split("-")
                val node = nodeInfo.split("-")

                startNode = node[0].toInt()
                endNode = node[1].toInt()

                startWeek = week[0].toInt()
                endWeek = week[1].toInt()

                teacher = info[2]
                if (info.size < 5){
                    place = ""
                    dayOfWeek = info[3].toInt()
                }else{
                    place = info[3]
                    dayOfWeek = info[4].toInt()
                }


                courseList.add(
                    Course(
                        name = name, room = place,
                        teacher = teacher, day = dayOfWeek,
                        startNode = startNode, endNode = endNode,
                        startWeek = startWeek, endWeek = endWeek,
                        type = type
                    )
                )
                //print("$name  ${startWeek}-${endWeek}周  ${type}  星期${dayOfWeek}  ${startNode}-${endNode}节  ${teacher}  ${place}\n")
            }
        }
        return courseList
    }
}