package main.java

import bean.CourseBaseBean
import bean.CourseDetailBean

class Generator(baseList: ArrayList<CourseBaseBean>, detailList: ArrayList<CourseDetailBean>) {
    //默认配置
    private val header =
        "{\"courseLen\":45,\"id\":1,\"name\":\"默认\",\"sameBreakLen\":false,\"sameLen\":true,\"theBreakLen\":10}"

    //课程时间配置
    private val timePreference =
        "[{\"endTime\":\"09:25\",\"node\":1,\"startTime\":\"08:45\",\"timeTable\":1},{\"endTime\":\"10:15\",\"node\":2,\"startTime\":\"09:35\",\"timeTable\":1},{\"endTime\":\"11:10\",\"node\":3,\"startTime\":\"10:30\",\"timeTable\":1},{\"endTime\":\"12:00\",\"node\":4,\"startTime\":\"11:20\",\"timeTable\":1},{\"endTime\":\"15:10\",\"node\":5,\"startTime\":\"14:30\",\"timeTable\":1},{\"endTime\":\"16:00\",\"node\":6,\"startTime\":\"15:20\",\"timeTable\":1},{\"endTime\":\"16:55\",\"node\":7,\"startTime\":\"16:15\",\"timeTable\":1},{\"endTime\":\"17:45\",\"node\":8,\"startTime\":\"17:05\",\"timeTable\":1},{\"endTime\":\"19:40\",\"node\":9,\"startTime\":\"19:00\",\"timeTable\":1},{\"endTime\":\"20:30\",\"node\":10,\"startTime\":\"19:50\",\"timeTable\":1},{\"endTime\":\"21:20\",\"node\":11,\"startTime\":\"20:40\",\"timeTable\":1},{\"endTime\":\"22:10\",\"node\":12,\"startTime\":\"21:30\",\"timeTable\":1}]"
    //配色
    private val colorScheme =
        "{\"background\":\"\",\"courseTextColor\":-1,\"id\":1,\"itemAlpha\":60,\"itemHeight\":64,\"itemTextSize\":12,\"maxWeek\":22,\"nodes\":12,\"showOtherWeekCourse\":true,\"showSat\":true,\"showSun\":true,\"showTime\":false,\"startDate\":\"2021-9-6\",\"strokeColor\":-2130706433,\"sundayFirst\":false,\"tableName\":\"未命名\",\"textColor\":-16777216,\"timeTable\":1,\"type\":0,\"widgetCourseTextColor\":-1,\"widgetItemAlpha\":60,\"widgetItemHeight\":64,\"widgetItemTextSize\":12,\"widgetStrokeColor\":-2130706433,\"widgetTextColor\":-16777216}"
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
    private fun courseDetail(course: CourseDetailBean) = "{" +
            "\"day\": ${course.day}," +
            "\"endTime\": \"\"," +
            "\"endWeek\": ${course.endWeek}," +
            "\"id\": ${course.id}," +
            "\"level\": 0," +
            "\"ownTime\": false," +
            "\"room\": \"${course.room}\"," +
            "\"startNode\": ${course.startNode}," +
            "\"startTime\": \"\"," +
            "\"startWeek\": ${course.startWeek}," +
            "\"step\": ${course.step}," +
            "\"tableId\": ${course.tableId}," +
            "\"teacher\": \"${course.teacher}\"," +
            "\"type\": ${course.type}" +
            "  },"

    init {
        println("保存以下内容为 [name].wakeup_schedule 文件可直接导入WakeUp\n" +
                "注意：文件第一行不能是空格/换行\n" +
                "注意：不允许出现特殊影响Json格式的符号如：\" \\n等\n" +
                "否则会导入失败\n" +
                "=============================\n" +
                header + "\n" +
                timePreference + "\n" +
                colorScheme + "\n" +
                courseList(baseList) + "\n" +
                courseDetailList(detailList) +
                "\n\n============================="
        )
    }
}



