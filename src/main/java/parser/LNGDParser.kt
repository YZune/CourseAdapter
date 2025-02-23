package parser

import bean.Course
import org.jsoup.Jsoup

class LNGDParser(source: String) : Parser(source) {
    override fun getNodes(): Int = 10

    override fun generateCourseList() : List<Course> {
        val courseList = arrayListOf<Course>()

        var day = 0
        var room = String()
        var name = String()
        var credit = 0f
        var teacher = ""

        val doc = Jsoup.parse(source)

        val id = doc.getElementById("wdkb-kb")
        val byclass = id.getElementsByClass("kbappTimetableDayColumnRoot")
        var fragment = ArrayList<String>().toList()

        byclass.forEach {
            day += 1
            it.getElementsByClass("kbappTimetableCourseRenderCourseItem el-popover__reference").eachText().forEach {
                var startNode = 0
                var endNode = 0
                var startWeek = 0
                var endWeek = 0
                fragment = it.split(" ").toList()
                for (i in fragment) {
                    name = fragment[0]
                    print(fragment)
                    teacher = "周([\\u4e00-\\u9fa5]+)".toRegex().find(fragment[2])?.let { it1 -> it1.groupValues[1] }.toString()

                    credit = fragment[1].replace("[^0-9]".toRegex(), "").toFloat() / 10

                    room = if (fragment[fragment.size - 1].last() == '节') {
                        ""
                    } else {
                        fragment[fragment.size - 1]
                    }
                    val b = """\d+周|\d+-+\d+周""".toRegex().findAll(fragment.toString())
                        .map { it.value }.toList().toString()
                        .replace("""周""".toRegex(), "")
                        .replace(""",""".toRegex(), "")
                        .substringAfter("[").substringBefore("]").split(" ").toList()
                    val c = """第+\d+节+-第+\d+节""".toRegex().findAll(i.toString())
                        .map { it.value }.toList().toString()
                        .replace("第", "")
                        .replace("节", "")
                        .replace(",", "")
                        .substringAfter("[").substringBefore("]")
                        .split(" ").toList()
                    if (c.isNotEmpty()) {
                        for (i in c) {
                            if (i.isNotEmpty()) {
                                val (startNode1, endNode1) = i.split("-").map { it.toInt() }
                                if (startNode1 != startNode) {
                                    startNode = startNode1
                                    endNode = endNode1
                                    for (i in b) {
                                    if (i.length > 2) {
                                        val (startWeek1, endWeek1) = i.split("-").map { it.toInt() }
                                        if (startWeek1 != startWeek) {
                                            startWeek = startWeek1
                                            endWeek = endWeek1
                                        }
                                    } else if (i.isNotEmpty()) {
                                        startWeek = i.toInt()
                                        endWeek = i.toInt()
                                    }
                                    courseList.add(
                                        Course(
                                            name = name,
                                            room = room,
                                            teacher = teacher,
                                            day = day,
                                            startNode = startNode,
                                            endNode = endNode,
                                            startWeek = startWeek,
                                            endWeek = endWeek,
                                            type = 0,
                                            credit = credit
                                        )
                                    )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return courseList
    }
}