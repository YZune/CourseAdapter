package parser

import bean.Course
import bean.WeekBean
import main.java.bean.TimeDetail
import main.java.bean.TimeTable

class THUParser(source: String) : Parser(source) {

    fun parseWeeks(courseWeeks: String): List<WeekBean> {
        when (courseWeeks) {
            "全周" -> return listOf(WeekBean(1, 16, 0))
            "前八周" -> return listOf(WeekBean(1, 8, 0))
            "后八周" -> return listOf(WeekBean(9, 16, 0))
            "单周" -> return listOf(WeekBean(1, 15, 1))
            "双周" -> return listOf(WeekBean(2, 16, 2))
            else -> {
                if (!courseWeeks.endsWith("周")) {
                    return listOf(WeekBean(0, 0, -1))
                }
                val courseWeeksRanges = courseWeeks
                    .removePrefix("第")
                    .removeSuffix("周")
                    .split(",")
                val weekIntList = mutableListOf<Int>()
                courseWeeksRanges.forEach {
                    when {
                        "-" in it ->
                            (it.substringBefore("-").toInt()..it.substringAfter("-").toInt())
                                .forEach { weekInt -> weekIntList.add(weekInt) }
                        else -> weekIntList.add(it.toInt())
                    }
                }
                return Common.weekIntList2WeekBeanList(weekIntList)
            }
        }
    }

    override fun generateTimeTable(): TimeTable {
        return TimeTable(
            name = "清华大学", timeList = listOf(
                TimeDetail(1, "08:00", "08:45"),
                TimeDetail(2, "08:50", "09:35"),
                TimeDetail(3, "09:50", "10:35"),
                TimeDetail(4, "10:40", "11:25"),
                TimeDetail(5, "11:30", "12:15"),
                TimeDetail(6, "13:30", "14:15"),
                TimeDetail(7, "14:20", "15:05"),
                TimeDetail(8, "15:20", "16:05"),
                TimeDetail(9, "16:10", "16:55"),
                TimeDetail(10, "17:05", "17:50"),
                TimeDetail(11, "17:55", "18:40"),
                TimeDetail(12, "19:20", "20:05"),
                TimeDetail(13, "20:10", "20:55"),
                TimeDetail(14, "21:00", "21:45")
            )
        )
    }

    override fun generateCourseList(): List<Course> {
        val script = Regex("""setInitValue\(\).+setInitValue""", RegexOption.DOT_MATCHES_ALL)
            .find(source)!!.value

        val secondaryCoursesDetails = mutableMapOf<String, Pair<String, String>>() // name to <teacher, notes>
        val secondaryCoursesHeader = Regex("""var gridColumns = \[(.+)];""", RegexOption.DOT_MATCHES_ALL)
            .find(source)?.groupValues?.get(1)
        val secondaryCoursesData = Regex("""var gridData = \[(.+)];""", RegexOption.DOT_MATCHES_ALL)
            .find(source)?.groupValues?.get(1)
        var secondaryCoursesNameIndex = 0
        var secondaryCoursesTeacherIndex = 0
        var secondaryCoursesNotesIndex = 0
        secondaryCoursesHeader?.let { header ->
            header.split(",").forEachIndexed { i, s ->
                when {
                    "课程名" in s -> secondaryCoursesNameIndex = i
                    "任课教师" in s -> secondaryCoursesTeacherIndex = i
                    "选课文字说明" in s -> secondaryCoursesNotesIndex = i
                }
            }
        }
        secondaryCoursesData?.let { data ->
            Regex("""\[([^\[\]]+)]""").findAll(data).forEach { array ->
                val items = array.groupValues[1].split(",")
                val courseName = items[secondaryCoursesNameIndex].trim().removeSurrounding("\"")
                val courseTeacher = items[secondaryCoursesTeacherIndex].trim().removeSurrounding("\"")
                val courseNotes = items[secondaryCoursesNotesIndex].trim().removeSurrounding("\"")
                secondaryCoursesDetails[courseName] = Pair(courseTeacher, courseNotes)
            }
        }

        val courseList = mutableListOf<Course>()

        data class CourseDetails(
            var number: String = "",
            var name: String = "",
            var teacher: String = "",
            var weeks: String = "",
            var location: String = "", // 二级课程内容加在这里
            var notes: String = "",
            var time: String = "",
            val params: MutableList<String> = mutableListOf()
        )

        var courseInfo = CourseDetails()

        val startNodeMap = mapOf("1" to 1, "2" to 3, "3" to 6, "4" to 8, "5" to 10, "6" to 12)
        val endNodeMap = mapOf("1" to 2, "2" to 5, "3" to 7, "4" to 9, "5" to 11, "6" to 14)

        script.lines().forEach { line ->
            if ("getElementById" in line) {
                // finalize
                val (_, node, day) = Regex("""a(\d)_(\d)""").find(line)!!.groupValues
                if (courseInfo.params.isNotEmpty()) {
                    courseInfo.params.reverse()
                    courseInfo.params.forEach {
                        when {
                            // 倒数第一个以周结尾的是周数
                            it.endsWith("周") && courseInfo.weeks.isBlank() -> courseInfo.weeks = it
                            // 上课教室在周数的后面
                            courseInfo.weeks.isBlank() -> courseInfo.location = it
                            // 周数前面的不是课程属性的是教师（有可能空）
                            it !in Common.courseProperty -> courseInfo.teacher = it
                        }
                    }
                }
                parseWeeks(courseInfo.weeks.trim()).forEach { week ->
                    courseList.add(
                        Course(
                            name = courseInfo.name,
                            day = day.toInt(),
                            room = courseInfo.location,
                            teacher = courseInfo.teacher,
                            startNode = startNodeMap.getOrDefault(node, 0),
                            endNode = endNodeMap.getOrDefault(node, 0),
                            startWeek = week.start,
                            endWeek = week.end,
                            type = week.type,
                            credit = when (courseInfo.number) {
                                "" -> 0.0f
                                else -> courseInfo.number.last().toFloat() - 48 // char to float: ASCII
                            },
                            startTime = when (courseInfo.time) {
                                "" -> ""
                                else -> courseInfo.time.substringBefore("-")
                            },
                            endTime = when (courseInfo.time) {
                                "" -> ""
                                else -> courseInfo.time.substringAfter("-")
                            }
                        )
                    )
                }
                courseInfo = CourseDetails()
            } else if ("blue_red_none" in line) {
                // secondary courses
                var topic = ""
                Regex("""<font color='blue'>([^<>]+?)</font>""").findAll(line).forEachIndexed { i, result ->
                    when (i) {
                        0 -> courseInfo.name = result.groupValues[1].trim()
                        1 -> {
                            var details = result.groupValues[1].trim()
                            if (details.startsWith("(")) {
                                topic = details.substringBefore(")") + ")"
                                details = details.substringAfter(")")
                            }
                            details = details.substringAfter("(").substringBefore(")").trim()
                            details.split("；").forEach {
                                when {
                                    "周" in it -> courseInfo.weeks = it
                                    "时间" in it -> courseInfo.time = it.removePrefix("时间：")
                                    courseInfo.location.isBlank() -> courseInfo.location = it.trim()
                                }
                            }
                        }
                    }
                }
                courseInfo.location += topic
                courseInfo.teacher = secondaryCoursesDetails[courseInfo.name]?.first ?: ""
                courseInfo.notes = secondaryCoursesDetails[courseInfo.name]?.second ?: ""
            } else if ("strHTML += \"" in line) {
                if ("<b>" in line) {
                    courseInfo.name = line.substringAfter("<b>").substringBefore("</b>").trim()
                } else if ("<a " in line) {
                    courseInfo.number = Regex("""\d{10};(\d{8})""").find(line)?.groupValues?.get(1) ?: ""
                }
            } else if ("strHTML1 +=" in line) {
                courseInfo.params.add(line.substringAfter("；").substringBefore("\"").trim())
            }
        }

        return courseList
    }
}