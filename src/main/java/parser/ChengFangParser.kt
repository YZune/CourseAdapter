package parser

import Common
import bean.ChengFangInfo
import bean.Course
import com.google.gson.Gson

class ChengFangParser(source: String) : Parser() {

    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()
        val json = source.substringAfter("var kbxx = ").substringBefore(';')
        val gson = Gson()
        val weekList = arrayListOf<Int>()
        gson.fromJson(json, Array<ChengFangInfo>::class.java).forEach {
            weekList.clear()
            it.zcs.split(',').forEach { str ->
                weekList.add(str.toInt())
            }
            weekList.sort()
            val day = it.xq.toInt()
            val startNode = it.jcdm2.split(',')[0].toInt()
            val endNode =
                if (it.jcdm2.contains(',')) it.jcdm2.split(',').last().toInt() else it.jcdm2.split(',')[0].toInt()
            val step = endNode - startNode + 1
            Common.weekIntList2WeekBeanList(weekList).forEach { weekBean ->
                courseList.add(
                    Course(
                        name = it.kcmc, day = day,
                        room = it.jxcdmcs, teacher = it.teaxms,
                        startNode = startNode, endNode = startNode + step - 1,
                        startWeek = weekBean.start, endWeek = weekBean.end,
                        type = weekBean.type,
                    )
                )
            }
        }
        return courseList
    }

}