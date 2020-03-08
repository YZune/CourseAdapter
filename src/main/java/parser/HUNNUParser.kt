package main.java.parser

import bean.Course
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import parser.Parser
import java.util.ArrayList
import java.util.regex.Pattern

class HUNNUParser (source:String) : Parser(source){
    override fun generateCourseList(): List<Course> {
            val list = ArrayList<Course>()
            var course: Course? = null
            val document = Jsoup.parse(source)
            var element: Element? = null
            val pre: Element? = null
            var str: String? = null
            val pName = Pattern.compile("(.+)\\(\\w+\\.")
            val pTeacher = Pattern.compile("\\(([\\u4e00-\\u9fa5]+)\\)$")
            for (i in 0..90) {
                element = document.getElementById("TD" + i + "_0")
                if (element != null) {
                    str = element.html()
                    if ("" != str) {
                        val strings1 = str.split("<br>".toRegex()).toTypedArray()
                        var name: String = ""
                        var teacher: String = ""
                        val day = i / 13 + 1
                        val startNode = i % 13 + 1
                        var endNode = i % 13 + 1
                        var k = i + 1
                        while (document.getElementById("TD" + k + "_0") == null) {
                            endNode++
                            k++
                        }
                        var strings2: Array<String>? = null
                        var weekInfo: Array<String>? = null
                        var startWeek = 0
                        var endWeek = 0
                        if ("" == strings1[1] && "" == strings1[2]) {
                            val mName = pName.matcher(strings1[0])
                            if (mName.find()) {
                                name = mName.group(1)
                            }
                            val mTeacher = pTeacher.matcher(strings1[0])
                            if (mTeacher.find()) {
                                teacher = mTeacher.group(1)
                            }
                            strings2 =
                                strings1[3].substring(1, strings1[3].length - 1).split(",".toRegex()).toTypedArray()
                            weekInfo = strings2[0].split("-".toRegex()).toTypedArray()
                            startWeek = weekInfo[0].toInt()
                            endWeek = weekInfo[weekInfo.size - 1].toInt()
                            val room = strings2[1]
                            course = Course(name, day, room, teacher, startNode, endNode, startWeek, endWeek, 0)
                            list.add(course)
                        } else {
                            var j = 0
                            while (j * 2 < strings1.size) {
                                name = ""
                                teacher = ""
                                startWeek = 0
                                endWeek = 0
                                val mName = pName.matcher(strings1[j * 2])
                                if (mName.find()) {
                                    name = mName.group(1)
                                }
                                val mTeacher = pTeacher.matcher(strings1[j * 2])
                                if (mTeacher.find()) {
                                    teacher = mTeacher.group(1)
                                }
                                strings2 = strings1[j * 2 + 1].substring(1, strings1[j * 2 + 1].length - 1)
                                    .split(",".toRegex()).toTypedArray()
                                weekInfo = strings2[0].split("-".toRegex()).toTypedArray()
                                startWeek = weekInfo[0].toInt()
                                endWeek = weekInfo[weekInfo.size - 1].toInt()
                                val room = strings2[1]
                                course = Course(name, day, room, teacher, startNode, endNode, startWeek, endWeek, 0)
                                list.add(course)
                                j++
                            }
                        }
                    }
                }
            }
        return list
    }
}