package main.java.parser

import bean.Course
import main.java.bean.TimeDetail
import main.java.bean.TimeTable
import org.jsoup.Jsoup
import parser.Parser

class LNGDParser(source: String) : Parser(source) {
    override fun getNodes(): Int = 12

    override fun generateTimeTable(): TimeTable {
        return TimeTable(
            name = "轨道装备学院",
            timeList = listOf(
                TimeDetail(1, "08:20", "09:05"),
                TimeDetail(2, "09:15", "10:00"),
                TimeDetail(3, "10:10", "10:55"),
                TimeDetail(4, "11:05", "11:50"),
                TimeDetail(5, "13:20", "14:05"),
                TimeDetail(6, "14:15", "15:00"),
                TimeDetail(7, "15:10", "15:55"),
                TimeDetail(8, "16:05", "16:50"),
                TimeDetail(9, "17:15", "18:00"),
                TimeDetail(10, "18:10", "18:55"),
                TimeDetail(11, "19:05", "19:50"),
                TimeDetail(12, "20:00", "20:45"),
            )
        )
    }

    override fun generateCourseList() : List<Course> {
        val courseList = arrayListOf<Course>()

        var day = 0

        val doc = Jsoup.parse(source)

        when (doc.getElementById("root") != null) {
            true -> {
                val id = doc.getElementById("root")
                val byclass = id.getElementsByClass("kbappTimetableDayColumnRoot___1DlDV")

                byclass.forEach { dayInfo ->

                    var room: String
                    var name: String
                    var credit: Float
                    var startNode: Int
                    var endNode: Int
                    var startWeek: Int
                    var endWeek: Int
                    var teacher = String()

                    day += 1
                    dayInfo.getElementsByClass("kbappTimetableCourseRenderCourseItem___MgPtp").forEach { classinfo ->
                        val nc = classinfo.getElementsByClass("title___3o2RH").text().split(" ").toList()
                        name = nc[0]
                        credit = nc[1].replace("[^0-9]".toRegex(), "").toFloat() / 10
                        classinfo.getElementsByClass("kbappTimetableCourseRenderCourseItemInfoText___2Zmwu").forEach { fragment ->
                            val teacherNodeWeekRoom =
                                fragment.getElementsByClass("kbappTimetableCourseRenderCourseItemInfoText___2Zmwu").text().split(" ")
                                    .toList()

                            room = room(teacherNodeWeekRoom)

                            val teacher1 = teacher(teacherNodeWeekRoom.toString())

                            if (teacher1.isNotEmpty()) {
                                teacher = teacher1
                            }

                            val n = node(teacherNodeWeekRoom.toString())
                            val (startNode1, endNode1) = n.split("-").map { it.toInt() }
                            startNode = startNode1
                            endNode = endNode1

                            val w = week(teacherNodeWeekRoom.toString())
                            w.forEach { w ->
                                if (w.length > 2) {
                                    val (startWeek1, endWeek1) = w.split("-").map { it.toInt() }
                                    startWeek = startWeek1
                                    endWeek = endWeek1
                                } else {
                                    if (w.isEmpty()) {
                                        error("请在学期课表导入")
                                    }
                                    startWeek = w.toInt()
                                    endWeek = w.toInt()
                                }
                                courseList.add(Course(
                                    day = day,
                                    name = name,
                                    teacher = teacher,
                                    credit = credit,
                                    room = room,
                                    startNode = startNode,
                                    endNode = endNode,
                                    startWeek = startWeek,
                                    endWeek = endWeek,
                                    type = 0,
                                ))
                            }
                        }
                    }
                }
                return courseList

            }
            false -> {
                val id = doc.getElementById("wdkb-kb")
                val byclass = id.getElementsByClass("kbappTimetableContentContainer")

                byclass.forEach { allInfo ->
                    allInfo.getElementsByClass("kbappTimetableDayColumnRoot").forEach { dayInfo ->

                        var room: String
                        var name: String
                        var credit: Float
                        var startNode: Int
                        var endNode: Int
                        var startWeek: Int
                        var endWeek: Int
                        var teacher = String()

                        day += 1
                        dayInfo.getElementsByClass("kbappTimetableCourseRenderCourseItemContainer").forEach { classinfo ->
                            val all = classinfo.getElementsByClass("kbappTimetableCourseRenderCourseItemInfoText").toList().map { it.text().split(" ").toList() }
                            name = all[0][0]
                            credit = all[0][1].replace("[^0-9]".toRegex(), "").toFloat() / 10
                            for (i in 1..all.size - 1) {
                                val teacherNodeWeekRoom = all[i]

                                room = room(teacherNodeWeekRoom)

                                val teacher1 = teacher(teacherNodeWeekRoom.toString())

                                if (teacher1.isNotEmpty()) {
                                    teacher = teacher1
                                }
                                val n = node(teacherNodeWeekRoom.toString())
                                val (startNode1, endNode1) = n.split("-").map { it.toInt() }
                                startNode = startNode1
                                endNode = endNode1

                                val w = week(all[i].toString())
                                w.forEach { w ->
                                    val s = w
                                    if (s.length > 2) {
                                        val (startWeek1, endWeek1) = s.split("-").map { it.toInt() }
                                        startWeek = startWeek1
                                        endWeek = endWeek1
                                    } else {
                                        if (w.isEmpty()) {
                                            error("请在学期课表导入")
                                        }
                                        startWeek = s.toInt()
                                        endWeek = s.toInt()
                                    }
                                    courseList.add(Course(
                                        day = day,
                                        name = name,
                                        teacher = teacher,
                                        credit = credit,
                                        room = room,
                                        startNode = startNode,
                                        endNode = endNode,
                                        startWeek = startWeek,
                                        endWeek = endWeek,
                                        type = 0,
                                    ))
                                }
                            }
                        }
                    }
                }


                return courseList
            }
        }
    }

    private fun week(string: String): List<String> {
        return """\d+周|\d+-+\d+周""".toRegex().findAll(string)
            .map { it.value }.toList().toString()
            .replace("""周""".toRegex(), "")
            .replace(""",""".toRegex(), "")
            .substringAfter("[").substringBefore("]").split(" ").toList()
    }

    private fun teacher(string: String): String {
        return "周([\\u4e00-\\u9fa5]+)".toRegex().find(string)?.let { it -> it.groupValues[1] }
            .toString()
    }

    private fun node(string: String): String {
        return """第+\d+节+-第+\d+节""".toRegex().findAll(string)
            .map { it -> it.value }.toList().toString()
            .replace("第", "")
            .replace("节", "")
            .replace(",", "")
            .substringAfter("[").substringBefore("]")
    }

    private fun room(list: List<String>): String {
        return if (list.size > 2) {
            list[list.size - 1]
        } else {
            ""
        }
    }
}