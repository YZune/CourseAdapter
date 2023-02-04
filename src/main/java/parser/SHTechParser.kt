package main.java.parser

import bean.Course
import main.java.bean.TimeDetail
import main.java.bean.TimeTable
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import parser.Parser


class SHTechParser(source: String) : Parser(source) {
    /**@author mhk
     * @date 20220821
     * @update 20230204
     * 上海科技大学研究生教务导入
     * https://grad.shanghaitech.edu.cn/public/WitMis_LookCourseTable.aspx
     */
    /*
欢迎使用上海科技大学研究生课表导入工具,本科生小朋友请出门左转用树维系统导入工具导入.
登录后,请打开 我的培养-查看课表 再导入.如果右上角用户角色为 答辩秘书,还需要先切换为 研究生.
1.对于研究生选修本科生课的情况,教务系统中显示的课表中可能没有课程的标题信息.
2.对于SIST/SLST/SPST以外的其他学院开设的课程,教务系统中显示的课表中可能没有课程的标题信息.
对于这些情况,课程标题暂且展示为班级+教师信息.
这些问题均出自教务系统的bug,对于未有明确修正说明的情况本工具均“依样”输出.
<>建议自行在我的培养-排课结果查询 利用教室信息查询并手动修正.<>
如果你遇到其他问题,可以带上截图及课表页面HTML发邮件到 y@wanghy.gq .
     */
    override fun getNodes(): Int = 13

    override fun getTableName(): String = "上科大导入"

    override fun generateTimeTable(): TimeTable {
        val timeList: ArrayList<TimeDetail> = arrayListOf(
            TimeDetail(1, "08:15", "09:00"),
            TimeDetail(2, "09:10", "09:55"),
            TimeDetail(3, "10:15", "11:00"),
            TimeDetail(4, "11:10", "11:55"),
            TimeDetail(5, "13:00", "13:45"),
            TimeDetail(6, "13:55", "14:40"),
            TimeDetail(7, "15:00", "15:45"),
            TimeDetail(8, "15:55", "16:40"),
            TimeDetail(9, "16:50", "17:35"),
            TimeDetail(10, "18:00", "18:45"),
            TimeDetail(11, "18:55", "19:40"),
            TimeDetail(12, "19:50", "20:35"),
            TimeDetail(13, "20:45", "21:30")
        )
        return TimeTable("上科大作息", timeList)
    }

    override fun generateCourseList(): List<Course> {
        val courseWebs = getCourseWeb(source)
        val to_return = courseWebs.flatMap { transform(it) }
        return to_return
    }


    fun transform(courseWeb: CourseWeb): ArrayList<Course> {
        val to_return: ArrayList<Course> = ArrayList()


        val name = courseWeb.getName(isEndUser)
        val note = ""
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
                if (i !in courseWeb.schedule.except) {
                    weekList.add(i)
                }
            }
            val weeks = Common.weekIntList2WeekBeanList(weekList)
            for (week in weeks) {
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

    val isWakeUp = true
    val isEndUser = true

    fun getCourseWeb(html: String): ArrayList<CourseWeb> {
        val to_return = ArrayList<CourseWeb>()
        val document = Jsoup.parse(html)
        val frame = document.getElementsByAttributeValue("src", "./inputSelf2_files/WitMis_LookCourseTable.html")
        val frameHtml = frame.text()
        val frameDocument = Jsoup.parse(frameHtml)
        val table = frameDocument.getElementById("div-table")
        val trs = table?.select("tr")!!

        val addTd = ArrayList<ArrayList<Int>>(14)
        for (i in 1..14) {
            addTd.add(ArrayList<Int>())
        }
        for ((row, tr) in trs.withIndex()) {
            //println("$row,$tr")
            addTd[row].sort()
            val tds = tr.select("td")
            for (add in addTd[row]) {
                tds.add(add, Element("td"))
            }
            for ((col, td) in tds.withIndex()) {
                if (row in 1..13 && col in 1..7) {
                    val tdText = td.html()?.trim()
                    val rowSpan = td.attr("rowspan").toIntOrNull()
                    var step = 0
                    //println(rowSpan)
                    //println(tdText)
                    if (rowSpan != null) {
                        step = rowSpan - 1
                        for (i in 1 until rowSpan) {
                            addTd[row + i].add(col)
                        }
                    }

                    if (tdText != null && tdText != "") {
                        val splited = tdText.split("<br>")
                        //println("$row,$col,$tdText")
                        val cnt = splited.size / 4
                        for (i in 0 until cnt) {
                            val classMate = splited[4 * i]
                            val teacher = splited[4 * i + 1]
                            val classRoom = splited[4 * i + 2]
                            val weekStr = splited[4 * i + 3]
                            val schedule = getWeek(weekStr, col, row, row + step, teacher, classRoom)
                            val schedule2 = if (schedule.weekStart < schedule.weekEnd) schedule else
                                CourseSchedule(
                                    schedule.teacher,
                                    schedule.classRoom,
                                    1,
                                    17,
                                    schedule.weekday,
                                    ArrayList<Int>(),
                                    schedule.LessonStart,
                                    schedule.LessonEnd
                                )
                            //如果课表上的时间出错,就设置为1-17周
                            val course = CourseWeb(classMate, schedule2)
                            //完成的todo:同一个格子有多门课的情况没有考虑
                            //println(course)
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
        val aScheduleO: CourseSchedule

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
            val weekStart = weeks.minOrNull()!!
            val weekEnd = weeks.maxOrNull()!!
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
                    a.schedule.LessonEnd = b.schedule.LessonEnd
                    data.remove(b)
                } else {
                    j++
                }
            }
            i++
        }
    }


    class CourseSchedule
        (
        var teacher: String,//授课教师
        val classRoom: String,//教室
        val weekStart: Int,//第几周开始
        val weekEnd: Int, //第几周结束
        val weekday: Int, //周几
        //val weekType: Int,//单双周 1单周 2双周 3正常
        val except: ArrayList<Int>,//第几周不上课
        val LessonStart: Int,//第几节课开始
        var LessonEnd: Int//第几节课结束

    ) {


        override fun toString(): String {
            val exceptStr = if (except.size > 0) "除${except}周," else ""
            return "教师:$teacher 地点:$classRoom,$weekStart-${weekEnd}周$weekday,$exceptStr$LessonStart-${LessonEnd}节"
        }

    }


    class CourseWeb
        (
        val classMate: String,
        val schedule: CourseSchedule
    ) {
        var isNeedCheck: Boolean = false

        val strClassMate = "(.*)\\d+班"
        val otherAdd = "班级:"
        val otherAdd2 = ",教师:"
        val mutltTeacherAdd = "请务必手动检查周数:"

        fun getNameOrNull(): String? {
            val regClassMate = Regex(strClassMate)
            val matchClassMate = regClassMate.findAll(classMate)
            val name = matchClassMate.elementAtOrNull(0)?.groupValues?.get(1)
            return name
        }

        fun getName(isEndUser: Boolean): String {
            val partName = getNameOrNull() ?: (otherAdd + classMate + otherAdd2 + schedule.teacher)
            val checkAdding = if (isEndUser && isNeedCheck) mutltTeacherAdd else ""
            return checkAdding + partName
        }

        override fun toString(): String {
            return "CourseWeb(classMate='$classMate', schedule=$schedule)"
        }
    }

}