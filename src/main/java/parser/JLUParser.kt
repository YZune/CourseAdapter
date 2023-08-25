package main.java.parser

import Common
import bean.Course
import com.google.gson.Gson
import main.java.bean.JLUCourseInfo
import parser.Parser

/**
 * 吉林大学研究生教务系统
 */
class JLUParser(source:String):Parser(source) {
    override fun generateCourseList(): List<Course> {
        val result = arrayListOf<Course>()
        val gson = Gson()
        val json = gson.fromJson(source, JLUCourseInfo::class.java)
        val rows = json.datas.xspkjgcx.rows

        for (row in rows) {
            val weekList = arrayListOf<Int>()
            row.ZCBH.forEachIndexed { index, c ->
                if (c=='1')
                    weekList.add(index+1)
            }
            Common.weekIntList2WeekBeanList(weekList).forEach{ week ->
                val course = Course(
                    name = row.KCMC,
                    day = row.XQ,
                    room = row.JASMC,
                    teacher = row.JSXM,
                    startNode = row.KSJCDM,
                    endNode = row.KSJCDM,
                    startWeek = week.start,
                    endWeek = week.end,
                    type = week.type
                )
                result.add(course)
            }
        }
        return result
    }
}