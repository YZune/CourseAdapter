package main.java.parser

import Common.getNodeInt
import bean.Course
import com.google.gson.Gson
import main.java.bean.CourseForCCIBE
import parser.Parser

/**
 * @author eucaly
 * @date 2022-06-07 17:21
 * 重庆对外经贸学院
 */

class CCIBEParser(source: String) : Parser(source) {

    override fun generateCourseList(): List<Course> {
        val courseList = ArrayList<Course>()
        val gson = Gson()
        gson.fromJson(source, Array<CourseForCCIBE>::class.java).forEach {
            val startNode: String
            val endNode: String
            if (it.jc.contains('-')) {
                startNode = it.jc.substringBefore('-')
                endNode = it.jc.substringAfter('-')
            } else {
                startNode = it.jc
                endNode = startNode
            }
            courseList.add(
                Course(
                    it.kcmc,
                    getNodeInt(it.xqj),
                    it.jxdd?: "",
                    it.jsxm?: "",
                    Integer.parseInt(startNode),
                    Integer.parseInt(endNode),
                    it.qsz,
                    it.jsz,
                    1,
                    0f,
                    "",
                    "",
                    ""
                )
            )
        }
        return courseList
    }
}