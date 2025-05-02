package main.java.parser

import bean.Course
import com.google.gson.Gson
import main.java.bean.HFUTCourse
import parser.Parser

class HFUTParser(sourse : String) : Parser() {
    override fun generateCourseList(): List<Course> {
        val courseList = ArrayList<Course>()
        val datumResponse = Gson().fromJson(source, HFUTCourse::class.java)
        val scheduleList = datumResponse.result.scheduleList
        val lessonList = datumResponse.result.lessonList
        val scheduleGroupList = datumResponse.result.scheduleGroupList

        for (i in scheduleList.indices) {
            var starttime = scheduleList[i].startTime.toString()
            starttime = starttime.substring(0, starttime.length - 2) + ":" + starttime.substring(starttime.length - 2)
            var endtimes = scheduleList[i].endTime.toString()
            endtimes = endtimes.substring(0, endtimes.length - 2) + ":" + endtimes.substring(endtimes.length - 2)

            val room = scheduleList[i].room.nameZh
            val person = scheduleList[i].personName
            var scheduleid = scheduleList[i].lessonId.toString()

            var periods = scheduleList[i].periods
            var lessonType = scheduleList[i].lessonType

            for (k in 0 until scheduleGroupList.size) {
                val id = scheduleGroupList[k].lessonId.toString()
                val std = scheduleGroupList[k].stdCount
                if (scheduleid == id) {
                    periods = std
                }
            }

            for (j in 0 until lessonList.size) {
                val lessonlist_id = lessonList[j].id
                val name = lessonList[j].courseName
                val courseTypeName = lessonList[j].courseTypeName
                if (scheduleid == lessonlist_id) {
                    scheduleid = name
                    lessonType = courseTypeName
                }
            }

            val week = scheduleList[i].weekIndex
            val day = scheduleList[i].weekday
            val startNode = when(scheduleList[i].startTime) {
                800 -> 1
                1010 -> 3
                1400 -> 5
                1600 -> 7
                1900 -> 9
                else -> 0
            }
            val endNode = when(scheduleList[i].startTime) {
                800 -> 2
                1010 -> 4
                1400 -> 6
                1600 -> 8
                1900 -> 10
                else -> 0
            }

            if(starttime == "8:00") starttime = "08:00"

            courseList.add(Course(
                name = scheduleid,
                day = day,
                room = room,
                teacher = person,
                startNode = startNode,
                endNode = endNode,
                startWeek = week,
                endWeek = week,
                type = 0,
                note = "人数:${periods} 类型:${lessonType}",
                startTime = starttime,
                endTime = endtimes,
            ))
        }

        return courseList
    }
}