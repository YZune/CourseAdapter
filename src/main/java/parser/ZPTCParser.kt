package main.java.parser

import bean.Course
import bean.CourseBaseBean
import bean.CourseDetailBean
import parser.Parser
import org.jsoup.Jsoup

class ZPTCParser(source: String) : Parser() {
    override fun generateCourseList(): List<Course> {
        val course: MutableList<Course> = mutableListOf()

        val doc = Jsoup.parse(source)
        val courseTable = doc.select("#kb")
        val courses = courseTable.select(".pbtd")
        val courseNames: MutableList<String> = mutableListOf()
        val courseBaseBeans: MutableList<CourseBaseBean> = mutableListOf()
        val courseDetailBeans: MutableList<CourseDetailBean> = mutableListOf()
        var courseNum = 0
        for (courseInfo in courses) {
            val courseName = courseInfo.text().trim().split("/")[0]
            if (courseName !in courseNames && courseName != "") {
                courseNames.add(courseName)
                courseBaseBeans.add(CourseBaseBean(courseNum, courseName, "", 0, ""))
                courseNum++
            }
        }

        for (courseInfo in courses) {
            var courseId: Int = -1
            var courseName: String
            var courseDay: Int
            var courseRoom: String
            var courseTeacher: String
            var courseNode: Int
            var courseStartWeek: Int
            var courseEndWeek: Int
            var courseType: Int
            courseName = courseInfo.text().trim().split("/")[0]
            if (courseName in courseNames) {
                courseRoom = courseInfo.text().trim().split("/")[2].split("【")[2].split("】")[0]
                for (i in courseBaseBeans) {
                    if (i.courseName == courseName)
                        courseId = i.id
                }
                val dayNum = Integer.parseInt(courseInfo.attr("id").split("x")[1].split("_")[0])
                val dayStr = courseTable.select("tr:first-child th:nth-child(" + (dayNum + 1) + ")").text()
                courseDay = Common.chineseWeekList.indexOf(dayStr)
                courseTeacher = courseInfo.text().trim().split("/")[2].split("【")[1].split("】")[0]
                courseNode = Integer.parseInt( courseInfo.text().trim().split("/")[2].split("-")[0].split("第")[1])
                courseStartWeek = Integer.parseInt(courseInfo.text().trim().split("/")[2].split("[")[2].split("]")[0].split("-")[0])
                courseEndWeek = Integer.parseInt(courseInfo.text().trim().split("/")[2].split("[")[2].split("]")[0].split("-")[1])
                courseType = when{
                    courseInfo.text().trim().split("/")[1] == "每周" ->0
                    courseInfo.text().trim().split("/")[1] == "单周" ->1
                    courseInfo.text().trim().split("/")[1] == "双周" ->2
                    else -> 0
                }
                courseDetailBeans.add(
                    CourseDetailBean(
                        courseId,
                        courseDay,
                        courseRoom,
                        courseTeacher,
                        courseNode,
                        2,
                        courseStartWeek,
                        courseEndWeek,
                        courseType,
                        0
                    )
                )
            }


        }


        //装填课程
        for (i in courseDetailBeans) {
            var name = ""
            for (j in courseBaseBeans) {
                if (i.id == j.id)
                    name = j.courseName
            }
            course.add(
                Course(
                    name,
                    i.day,
                    i.room!!,
                    i.teacher!!,
                    i.startNode,
                    i.startNode + i.step - 1,
                    i.startWeek,
                    i.endWeek,
                    i.type,
                )
            )
        }
        //println(course)
        return course
    }
}