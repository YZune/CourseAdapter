package main.java

import Common.TimeHM
import bean.CourseBaseBean
import bean.CourseDetailBean
import main.java.bean.TimeDetail
import parser.Parser

class Generator(baseList: ArrayList<CourseBaseBean>, detailList: ArrayList<CourseDetailBean>, parser: Parser?=null) {
    //读入关键参数
    private class ParserAttrib(parser: Parser?=null) {
        var ttbName = "默认"
        var tableName = "未命名"
        var courseLen = 50
        var sameLen = true
        var ttbList = listOf<TimeDetail>()
        var nodes = 30

        init { if (parser != null) {
            val i = parser.getTableName()
            if (i != null)
                tableName = i

            val ttb = parser.generateTimeTable()
            if (ttb != null) {
                ttbName = ttb.name
                ttbList = ttb.timeList
                if (ttbList.isNotEmpty())
                    nodes = ttb.timeList.size
                    courseLen = TimeHM(ttbList[0].startTime).duration(ttbList[0].endTime)
                    sameLen = false
            }
        } }
    }
    private val pAttrib = ParserAttrib(parser)

    //默认配置
    private val header =
        "{\"courseLen\":${pAttrib.courseLen},\"id\":1,\"name\":\"${pAttrib.ttbName}\",\"sameBreakLen\":false,\"sameLen\":${pAttrib.sameLen},\"theBreakLen\":10}"

    //课程时间配置
    private fun timePreference(): String {
        val ttbList = this.pAttrib.ttbList
        var retval =
        "[{\"endTime\":\"08:50\",\"node\":1,\"startTime\":\"08:00\",\"timeTable\":1},{\"endTime\":\"09:50\",\"node\":2,\"startTime\":\"09:00\",\"timeTable\":1},{\"endTime\":\"11:00\",\"node\":3,\"startTime\":\"10:10\",\"timeTable\":1},{\"endTime\":\"12:00\",\"node\":4,\"startTime\":\"11:10\",\"timeTable\":1},{\"endTime\":\"14:20\",\"node\":5,\"startTime\":\"13:30\",\"timeTable\":1},{\"endTime\":\"15:20\",\"node\":6,\"startTime\":\"14:30\",\"timeTable\":1},{\"endTime\":\"16:30\",\"node\":7,\"startTime\":\"15:40\",\"timeTable\":1},{\"endTime\":\"17:30\",\"node\":8,\"startTime\":\"16:40\",\"timeTable\":1},{\"endTime\":\"19:20\",\"node\":9,\"startTime\":\"18:30\",\"timeTable\":1},{\"endTime\":\"20:20\",\"node\":10,\"startTime\":\"19:30\",\"timeTable\":1},{\"endTime\":\"21:20\",\"node\":11,\"startTime\":\"20:30\",\"timeTable\":1},{\"endTime\":\"21:30\",\"node\":12,\"startTime\":\"21:25\",\"timeTable\":1},{\"endTime\":\"21:40\",\"node\":13,\"startTime\":\"21:35\",\"timeTable\":1},{\"endTime\":\"21:50\",\"node\":14,\"startTime\":\"21:45\",\"timeTable\":1},{\"endTime\":\"22:00\",\"node\":15,\"startTime\":\"21:55\",\"timeTable\":1},{\"endTime\":\"22:10\",\"node\":16,\"startTime\":\"22:05\",\"timeTable\":1},{\"endTime\":\"22:20\",\"node\":17,\"startTime\":\"22:15\",\"timeTable\":1},{\"endTime\":\"22:30\",\"node\":18,\"startTime\":\"22:25\",\"timeTable\":1},{\"endTime\":\"22:40\",\"node\":19,\"startTime\":\"22:35\",\"timeTable\":1},{\"endTime\":\"22:50\",\"node\":20,\"startTime\":\"22:45\",\"timeTable\":1},{\"endTime\":\"23:00\",\"node\":21,\"startTime\":\"22:55\",\"timeTable\":1},{\"endTime\":\"23:10\",\"node\":22,\"startTime\":\"23:05\",\"timeTable\":1},{\"endTime\":\"23:20\",\"node\":23,\"startTime\":\"23:15\",\"timeTable\":1},{\"endTime\":\"23:30\",\"node\":24,\"startTime\":\"23:25\",\"timeTable\":1},{\"endTime\":\"23:40\",\"node\":25,\"startTime\":\"23:35\",\"timeTable\":1},{\"endTime\":\"23:50\",\"node\":26,\"startTime\":\"23:45\",\"timeTable\":1},{\"endTime\":\"23:55\",\"node\":27,\"startTime\":\"23:51\",\"timeTable\":1},{\"endTime\":\"23:59\",\"node\":28,\"startTime\":\"23:56\",\"timeTable\":1},{\"endTime\":\"00:00\",\"node\":29,\"startTime\":\"00:00\",\"timeTable\":1},{\"endTime\":\"00:00\",\"node\":30,\"startTime\":\"00:00\",\"timeTable\":1}]"
        if (ttbList.isEmpty()) return retval

        retval = "["
        ttbList.forEach {retval +=
            "{\"endTime\":\"${it.endTime}\",\"startTime\":\"${it.startTime}\",\"node\":${it.node},\"timeTable\":1},"
        }
        return retval.removeSuffix(",") + "]"
    }
    //配色
    private val colorScheme =
        "{\"background\":\"\",\"courseTextColor\":-1,\"id\":1,\"itemAlpha\":60," +
        "\"itemHeight\":${if (pAttrib.nodes==30) 64 else 64*10/pAttrib.nodes},\"itemTextSize\":12,\"maxWeek\":20,\"nodes\":${pAttrib.nodes},\"showOtherWeekCourse\":true,\"showSat\":true,\"showSun\":true,\"showTime\":false,\"startDate\":\"2021-9-6\",\"strokeColor\":-2130706433,\"sundayFirst\":false,\"tableName\":\"${pAttrib.tableName}\",\"textColor\":-16777216,\"timeTable\":1,\"type\":0,\"widgetCourseTextColor\":-1,\"widgetItemAlpha\":60,\"widgetItemHeight\":64,\"widgetItemTextSize\":12,\"widgetStrokeColor\":-2130706433,\"widgetTextColor\":-16777216}"
    //课程列表
    private fun courseList(courses: List<CourseBaseBean>): String {
        var result = "["
        courses.forEach {
            result += course(it)
        }
        result += "]"
        return result.replace("},]","}]")
    }
    private fun course(
        course: CourseBaseBean
    ) = " {" +
            "\"color\": \"${course.color}\"," +
            "\"courseName\": \"${course.courseName}\"," +
            "\"credit\": ${course.credit}," +
            "\"id\": ${course.id}," +
            "\"note\": \"${course.note.replace("\n","")}\"," +
            "\"tableId\": ${course.tableId}" + //课表Id
            " },"
    private fun courseDetailList(courses: List<CourseDetailBean>): String {
        var result = "["
        courses.forEach {
            result += courseDetail(it)
        }
        result += "]"
        return result.replace("},]","}]")
    }
    private fun courseDetail(course: CourseDetailBean): String {
        var ownTime = "false"
        if (course.endTime != "") ownTime = "true"
        return "{" +
            "\"day\": ${course.day}," +
            "\"endTime\": \"${course.endTime}\"," +
            "\"endWeek\": ${course.endWeek}," +
            "\"id\": ${course.id}," +
            "\"level\": 0," +
            "\"ownTime\": ${ownTime}," +
            "\"room\": \"${course.room}\"," +
            "\"startNode\": ${course.startNode}," +
            "\"startTime\": \"${course.startTime}\"," +
            "\"startWeek\": ${course.startWeek}," +
            "\"step\": ${course.step}," +
            "\"tableId\": ${course.tableId}," +
            "\"teacher\": \"${course.teacher}\"," +
            "\"type\": ${course.type}" +
            "  },"
    }

    init {
        println("保存以下内容为 [name].wakeup_schedule 文件可直接导入WakeUp\n" +
                "注意：文件第一行不能是空格/换行\n" +
                "注意：不允许出现特殊影响Json格式的符号如：\" \\n等\n" +
                "否则会导入失败\n" +
                "=============================\n" +
                header + "\n" +
                timePreference() + "\n" +
                colorScheme + "\n" +
                courseList(baseList) + "\n" +
                courseDetailList(detailList) +
                "\n\n============================="
        )
    }
}



