package parser

import bean.Course
import bean.CourseBaseBean
import bean.CourseDetailBean
import bean.TimeTable
import utils.Common
import utils.Generator

abstract class Parser(val source: String) {

    private val _baseList: MutableList<CourseBaseBean> = mutableListOf()
    private val _detailList: MutableList<CourseDetailBean> = mutableListOf()

    abstract suspend fun generateCourseList(): MutableList<Course>

    // TimeTable中的name属性将起到标识作用，如果在数据库中发现同名时间表，则不再覆盖写入
    open fun generateTimeTable(): TimeTable? = null

    open fun getTableName(): String? = null

    open fun getNodes(): Int? = null

    open fun getStartDate(): String? = null

    open fun getMaxWeek(): Int? = null

    private suspend fun convertCourse() {
        generateCourseList().forEach { course ->
            var id = Common.findExistedCourseId(_baseList, course.name)
            if (id == -1) {
                id = _baseList.size
                _baseList.add(
                    CourseBaseBean(
                        id = id, courseName = course.name,
                        color = "",
                        tableId = 0,
                        note = course.note,
                        credit = course.credit
                    )
                )
            }
            _detailList.add(
                CourseDetailBean(
                    id = id, room = course.room,
                    teacher = course.teacher, day = course.day,
                    step = course.endNode - course.startNode + 1,
                    startWeek = course.startWeek, endWeek = course.endWeek,
                    type = course.type, startNode = course.startNode,
                    credit = course.credit,
                    tableId = 0
                )
            )
        }
    }

    suspend fun saveCourse(printOut: Boolean = true): Int {
        convertCourse()
        if (printOut) {
            println("成功导入 ${_baseList.size} 门课程")
            _baseList.forEach {
                println(it)
            }
            _detailList.forEach {
                println(it)
            }
            Generator(_baseList,_detailList)
        }
        return _baseList.size
    }

}