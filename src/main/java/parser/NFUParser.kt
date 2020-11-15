package main.java.parser

import bean.Course
import org.jsoup.Jsoup
import parser.Parser

class NFUParser(source: String):Parser(source) {

    override fun generateCourseList(): List<Course> {

        val courseList = arrayListOf<Course>()
        val courseStrList = arrayListOf<String>()
        var name:String =""
        var dayOfWeek:Int
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
                val info = c.split(" ")

                val weekInfo = info[0].trim('周')
                val nodeInfo = info[1].trim('节')

                val week = weekInfo.split("-")
                val node = nodeInfo.split("-")

                startNode = node[0].toInt()
                endNode = node[1].toInt()

                startWeek = week[0].toInt()
                endWeek = week[1].toInt()

                teacher = info[2]
                place = info[3]
                dayOfWeek = info[4].toInt()

                courseList.add(
                    Course(
                        name = name, room = place,
                        teacher = teacher, day = dayOfWeek,
                        startNode = startNode, endNode = endNode,
                        startWeek = startWeek, endWeek = endWeek,
                        type = 0
                    )
                )
                //print("$name  ${startWeek}-${endWeek}周  星期${dayOfWeek}  ${startNode}-${endNode}节  ${teacher}  ${place}\n")
            }
        }
        return courseList
    }
}