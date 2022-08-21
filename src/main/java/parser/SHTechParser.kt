package main.java.parser

import bean.Course
import org.jsoup.Jsoup
import parser.Parser

class SHTechParser(source: String) : Parser(source) {
    override fun generateCourseList(): List<Course> {
        val courseWebs = getCourseWeb(source)
        val to_return = courseWebs.flatMap { transform(it) }
        return to_return
    }

    val strClassMate = "(.*)\\d+班"
    fun transform(courseWeb: CourseWeb): ArrayList<Course> {
        val to_return: ArrayList<Course> = ArrayList()
        val regClassMate = Regex(strClassMate)
        val matchClassMate = regClassMate.findAll(courseWeb.classMate)

        val name = matchClassMate.elementAtOrNull(0)?.groupValues?.get(1) ?: courseWeb.classMate
        val note =""
        if (courseWeb.schedule.except.size == 0) {
            to_return.add(
                Course(
                    name,
                    courseWeb.schedule.weekday,
                    courseWeb.schedule.classRoom,
                    courseWeb.schedule.teacher,
                    courseWeb.schedule.LessonStart,
                    courseWeb.schedule.LessonEnd,
                    courseWeb.schedule.weekStart,
                    courseWeb.schedule.weekEnd,
                    0,
                    note = note
                )
            )
        } else {
            val weekList: ArrayList<Int> = ArrayList()
            for (i in courseWeb.schedule.weekStart..courseWeb.schedule.weekEnd) {
                if (i !in courseWeb.schedule.except)
                {
                    weekList.add(i)
                }
            }
            val weeks = Common.weekIntList2WeekBeanList(weekList)
            for (week in weeks)
            {
                to_return.add(
                    Course(
                        name,
                        courseWeb.schedule.weekday,
                        courseWeb.schedule.classRoom,
                        courseWeb.schedule.teacher,
                        courseWeb.schedule.LessonStart,
                        courseWeb.schedule.LessonEnd,
                        week.start,
                        week.end,
                        week.type,
                        note = note
                    )
                )
            }
        }
        return to_return
    }

    fun getCourseWeb(html: String): ArrayList<CourseWeb> {

        val to_return = ArrayList<CourseWeb>()
        val document = Jsoup.parse(html)
        val table = document.getElementById("div-table")
        val trs = table?.select("tr")!!

        for ((row, tr) in trs.withIndex()) {
            val tds = tr.select("td")
            for ((col, td) in tds.withIndex()) {
                if (row in 1..13 && col in 1..7) {
                    td.select("br").append("\n")//这里因为Jsoup版本不同,和原项目不同
                    val tdText = td.wholeText()
                    if (tdText != null && tdText != "") {
                        val splited = tdText.split("\n")
                        //println("$row,$col,$tdText")
                        val cnt = splited.size /4
                        for (i in 0 until cnt) {
                            val classMate = splited[4*i]
                            val teacher = splited[4*i+1]
                            val classRoom = splited[4*i+2]
                            val weekStr = splited[4*i+3]
                            val schedule = getWeek(weekStr, col, row, row, teacher, classRoom)
                            val course = CourseWeb(classMate, schedule)

                            to_return.add(course)
                        }
                    }

                }
            }
        }
        merge(to_return)
        return to_return
    }

    var strNum = ""
    val strWeek2 get() = "第$strNum\\-${strNum}周"
    val strWeek3 get() = "第$strNum(?:\\,$strNum)*周"
    val strWeekException get() = "(?:\\(除(?:$strWeek3)\\))"
    val strWeek2_2 get() = "$strWeek2$strWeekException?"
    //val strWeekAll get() = "(?:(?:${strWeek2_2})|(?:${strWeek3}))"

    fun getWeek(
        aSchedule: String,
        weekDay: Int,
        lessonStart: Int,
        lessonEnd: Int,
        teacher: String,
        classRoom: String
    ): CourseSchedule
            /**这里有一处利用bug形成的feature
             * 正则表达式匹配列表值,即使用*或+元字符时,返回groupValue只包含最后一项
             * 而课程几乎不出现不连续的取值,本程序也可以不考虑出现 一节课的节次为1,3,4这种情况
             * 所以可以大方地丢弃那些没有取到的数值
             * todo:在匹配排除列表时,不能使用这种逻辑.
             * */
    {
        strNum = "([1-9][0-9]*)"
        var aScheduleO: CourseSchedule

        val regWeek2 = Regex(strWeek2_2)
        val except: ArrayList<Int> = ArrayList()
        if (regWeek2.containsMatchIn(aSchedule)) {
            val regException = Regex(strWeekException)
            val matchWeek2 = regWeek2.findAll(aSchedule)
            val groupResultWeek2 = matchWeek2.elementAt(0).groupValues
            //println(groupResultWeek2)
            val weekStart = groupResultWeek2[1].toInt()
            val weekEnd = groupResultWeek2[2].toInt()

            if (regException.containsMatchIn(aSchedule)) {
                //println(aSchedule)
                val matchException = regException.findAll(aSchedule)
                val valueException = matchException.elementAt(0).value
                val regNum = Regex(strNum)
                val matchNum = regNum.findAll(valueException)

                for (aException in matchNum.iterator()) {
                    val exceptStr = aException.value
                    except.add(exceptStr.toInt())
                }
                //println(except)
            }
            aScheduleO = CourseSchedule(teacher, classRoom, weekStart, weekEnd, weekDay, except, lessonStart, lessonEnd)


        } else {
            val regWeek3 = Regex(strWeek3)
            val matchWeek3 = regWeek3.findAll(aSchedule)
            //println(aSchedule)
            if (matchWeek3.count() != 1) {
                println("错误发生在getSchedule()")
                throw Exception("错误发生在getSchedule()")
            }
            val valueWeek3 = matchWeek3.elementAt(0).value
            //println(valueWeek3)
            val regNum = Regex(strNum)
            val matchNum = regNum.findAll(valueWeek3)
            val weeks: ArrayList<Int> = ArrayList()
            for (aWeek in matchNum.iterator()) {
                weeks.add(aWeek.value.toInt())
            }
            val weekStart = weeks.min()!!
            val weekEnd = weeks.max()!!
            for (i in weekStart..weekEnd) {
                if (i !in weeks) {
                    except.add(i)
                }
            }
            aScheduleO = CourseSchedule(teacher, classRoom, weekStart, weekEnd, weekDay, except, lessonStart, lessonEnd)

        }
        return aScheduleO
    }

    fun merge(data: ArrayList<CourseWeb>) {
        var i = 0
        while (i < data.size) {
            val a = data[i]
            var j = i + 1
            while (j < data.size) {
                val b = data[j]
                if (a.classMate == b.classMate
                    && a.schedule.weekday == b.schedule.weekday
                    && a.schedule.weekStart == b.schedule.weekStart
                    && a.schedule.weekEnd == b.schedule.weekEnd
                    && a.schedule.teacher == b.schedule.teacher
                    && a.schedule.classRoom == b.schedule.classRoom
                    //&& a.schedule.except.equals(b.schedule.except)
                    && a.schedule.LessonEnd == b.schedule.LessonStart - 1
                ) {

                    a.schedule.LessonEnd = b.schedule.LessonStart
                    data.remove(b)
                } else {
                    j++
                }

            }
            i++
        }
    }
}



class CourseSchedule
    (
    val teacher: String,//授课教师
    val classRoom: String,//教室
    val weekStart: Int,//第几周开始
    val weekEnd: Int, //第几周结束
    val weekday: Int, //周几
    //val weekType: Int,//单双周 1单周 2双周 3正常
    val except: ArrayList<Int>,//第几周不上课
    val LessonStart: Int,//第几节课开始
    var LessonEnd: Int//第几节课结束

) {
}

class CourseWeb
    (
    val classMate: String,
    val schedule: CourseSchedule
) {
    override fun toString(): String {
        return "CourseWeb(classMate='$classMate', schedule=$schedule)"
    }
}