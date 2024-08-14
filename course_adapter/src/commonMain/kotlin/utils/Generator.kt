package utils

import bean.CourseBaseBean
import bean.CourseDetailBean

class Generator(baseList: MutableList<CourseBaseBean>, detailList: MutableList<CourseDetailBean>) {
    //默认配置
    private val header =
        "{\"courseLen\":50,\"id\":1,\"name\":\"默认\",\"sameBreakLen\":false,\"sameLen\":true,\"theBreakLen\":10}"

    //课程时间配置
    private val timePreference =
        "[{\"endTime\":\"08:50\",\"node\":1,\"startTime\":\"08:00\",\"timeTable\":1},{\"endTime\":\"09:50\",\"node\":2,\"startTime\":\"09:00\",\"timeTable\":1},{\"endTime\":\"11:00\",\"node\":3,\"startTime\":\"10:10\",\"timeTable\":1},{\"endTime\":\"12:00\",\"node\":4,\"startTime\":\"11:10\",\"timeTable\":1},{\"endTime\":\"14:20\",\"node\":5,\"startTime\":\"13:30\",\"timeTable\":1},{\"endTime\":\"15:20\",\"node\":6,\"startTime\":\"14:30\",\"timeTable\":1},{\"endTime\":\"16:30\",\"node\":7,\"startTime\":\"15:40\",\"timeTable\":1},{\"endTime\":\"17:30\",\"node\":8,\"startTime\":\"16:40\",\"timeTable\":1},{\"endTime\":\"19:20\",\"node\":9,\"startTime\":\"18:30\",\"timeTable\":1},{\"endTime\":\"20:20\",\"node\":10,\"startTime\":\"19:30\",\"timeTable\":1},{\"endTime\":\"21:20\",\"node\":11,\"startTime\":\"20:30\",\"timeTable\":1},{\"endTime\":\"21:30\",\"node\":12,\"startTime\":\"21:25\",\"timeTable\":1},{\"endTime\":\"21:40\",\"node\":13,\"startTime\":\"21:35\",\"timeTable\":1},{\"endTime\":\"21:50\",\"node\":14,\"startTime\":\"21:45\",\"timeTable\":1},{\"endTime\":\"22:00\",\"node\":15,\"startTime\":\"21:55\",\"timeTable\":1},{\"endTime\":\"22:10\",\"node\":16,\"startTime\":\"22:05\",\"timeTable\":1},{\"endTime\":\"22:20\",\"node\":17,\"startTime\":\"22:15\",\"timeTable\":1},{\"endTime\":\"22:30\",\"node\":18,\"startTime\":\"22:25\",\"timeTable\":1},{\"endTime\":\"22:40\",\"node\":19,\"startTime\":\"22:35\",\"timeTable\":1},{\"endTime\":\"22:50\",\"node\":20,\"startTime\":\"22:45\",\"timeTable\":1},{\"endTime\":\"23:00\",\"node\":21,\"startTime\":\"22:55\",\"timeTable\":1},{\"endTime\":\"23:10\",\"node\":22,\"startTime\":\"23:05\",\"timeTable\":1},{\"endTime\":\"23:20\",\"node\":23,\"startTime\":\"23:15\",\"timeTable\":1},{\"endTime\":\"23:30\",\"node\":24,\"startTime\":\"23:25\",\"timeTable\":1},{\"endTime\":\"23:40\",\"node\":25,\"startTime\":\"23:35\",\"timeTable\":1},{\"endTime\":\"23:50\",\"node\":26,\"startTime\":\"23:45\",\"timeTable\":1},{\"endTime\":\"23:55\",\"node\":27,\"startTime\":\"23:51\",\"timeTable\":1},{\"endTime\":\"23:59\",\"node\":28,\"startTime\":\"23:56\",\"timeTable\":1},{\"endTime\":\"00:00\",\"node\":29,\"startTime\":\"00:00\",\"timeTable\":1},{\"endTime\":\"00:00\",\"node\":30,\"startTime\":\"00:00\",\"timeTable\":1}]"

    //配色
    private val colorScheme =
        "{\"background\":\"\",\"courseTextColor\":-1,\"id\":1,\"itemAlpha\":60,\"itemHeight\":64,\"itemTextSize\":12,\"maxWeek\":20,\"nodes\":20,\"showOtherWeekCourse\":true,\"showSat\":true,\"showSun\":true,\"showTime\":false,\"startDate\":\"2021-9-6\",\"strokeColor\":-2130706433,\"sundayFirst\":false,\"tableName\":\"未命名\",\"textColor\":-16777216,\"timeTable\":1,\"type\":0,\"widgetCourseTextColor\":-1,\"widgetItemAlpha\":60,\"widgetItemHeight\":64,\"widgetItemTextSize\":12,\"widgetStrokeColor\":-2130706433,\"widgetTextColor\":-16777216}"
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



