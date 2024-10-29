package main.java

import bean.CourseBaseBean
import bean.CourseDetailBean
import com.google.gson.GsonBuilder
import main.java.bean.TimeTable
import java.io.File
import java.nio.file.Paths

class Generator(
    private val baseList: ArrayList<CourseBaseBean>,
    private val detailList: ArrayList<CourseDetailBean>,
    private val timeTable: TimeTable? = null,
    private val tableName: String? = null,
    private val nodes: Int? = null,
    private val startDate: String? = null,
    private val maxWeek: Int? = null,
) {
    private data class Header(
        val courseLen: Int = 50,
        val id: Int = 1,
        val name: String = "默认",
        val sameBreakLen: Boolean = false,
        val sameLen: Boolean = true,
        val theBreakLen: Int = 10,
    )

    private data class TimePreferenceItem(
        val startTime: String,
        val endTime: String,
        val node: Int,
        val timeTable: Int = 1,
    )

    private data class ColorScheme(
        val background: String = "",
        val courseTextColor: Int = -1,
        val id: Int = 1,
        val itemAlpha: Int = 60,
        val itemHeight: Int = 64,
        val itemTextSize: Int = 12,
        val maxWeek: Int = 20,
        val nodes: Int = 20,
        val showOtherWeekCourse: Boolean = true,
        val showSat: Boolean = true,
        val showSun: Boolean = true,
        val showTime: Boolean = false,
        val startDate: String = "2021-9-6",
        val strokeColor: Int = -2130706433,
        val sundayFirst: Boolean = false,
        val tableName: String = "未命名",
        val textColor: Int = -16777216,
        val timeTable: Int = 1,
        val type: Int = 0,
        val widgetCourseTextColor: Int = -1,
        val widgetItemAlpha: Int = 60,
        val widgetItemHeight: Int = 64,
        val widgetItemTextSize: Int = 12,
        val widgetStrokeColor: Int = -2130706433,
        val widgetTextColor: Int = -16777216,
    )

    private data class CourseDetailItem(
        val id: Int,
        val day: Int,
        val room: String,
        val teacher: String,
        val startNode: Int,
        val step: Int,
        val startWeek: Int,
        val endWeek: Int,
        val type: Int,
        val tableId: Int,
        val endTime: String = "",
        val startTime: String = "",
        val ownTime: Boolean = false,
        val level: Int = 0,
    )

    private fun getHeader() = Header(
        name = timeTable?.name ?: "默认"
    )

    private fun getDefaultTimePreference() = listOf(
        TimePreferenceItem("08:00", "08:50", 1),
        TimePreferenceItem("09:00", "09:50", 2),
        TimePreferenceItem("10:10", "11:00", 3),
        TimePreferenceItem("11:10", "12:00", 4),
        TimePreferenceItem("13:30", "14:20", 5),
        TimePreferenceItem("14:30", "15:20", 6),
        TimePreferenceItem("15:40", "16:30", 7),
        TimePreferenceItem("16:40", "17:30", 8),
        TimePreferenceItem("18:30", "19:20", 9),
        TimePreferenceItem("19:30", "20:20", 10),
        TimePreferenceItem("20:30", "21:20", 11),
        TimePreferenceItem("21:25", "21:30", 12),
        TimePreferenceItem("21:35", "21:40", 13),
        TimePreferenceItem("21:45", "21:50", 14),
        TimePreferenceItem("21:55", "22:00", 15),
        TimePreferenceItem("22:05", "22:10", 16),
        TimePreferenceItem("22:15", "22:20", 17),
        TimePreferenceItem("22:25", "22:30", 18),
        TimePreferenceItem("22:35", "22:40", 19),
        TimePreferenceItem("22:45", "22:50", 20),
        TimePreferenceItem("22:55", "23:00", 21),
        TimePreferenceItem("23:05", "23:10", 22),
        TimePreferenceItem("23:15", "23:20", 23),
        TimePreferenceItem("23:25", "23:30", 24),
        TimePreferenceItem("23:35", "23:40", 25),
        TimePreferenceItem("23:45", "23:50", 26),
        TimePreferenceItem("23:51", "23:55", 27),
        TimePreferenceItem("23:56", "00:00", 28),
        TimePreferenceItem("00:00", "00:00", 29),
        TimePreferenceItem("00:00", "00:00", 30)
    )

    private fun getTimePreference() = if (timeTable?.timeList == null) {
        getDefaultTimePreference()
    } else {
        timeTable.timeList.map { TimePreferenceItem(it.startTime, it.endTime, it.node) }
    }

    private fun getColorScheme() = ColorScheme(
        tableName = tableName ?: "未命名",
        nodes = nodes ?: 20,
        maxWeek = maxWeek ?: 20,
        startDate = startDate ?: "2021-9-6",
    )

    private fun getCourseBase() = baseList

    private fun getCourseDetail() = detailList.map {
        CourseDetailItem(
            it.id,
            it.day,
            it.room ?: "",
            it.teacher ?: "",
            it.startNode,
            it.step,
            it.startWeek,
            it.endWeek,
            it.type,
            it.tableId
        )
    }

    private fun generate(): String {
        val gson = GsonBuilder().create()
        val builder = StringBuilder()
        builder.append(gson.toJson(getHeader()))
        builder.append("\n")
        builder.append(gson.toJson(getTimePreference()))
        builder.append("\n")
        builder.append(gson.toJson(getColorScheme()))
        builder.append("\n")
        builder.append(gson.toJson(getCourseBase()))
        builder.append("\n")
        builder.append(gson.toJson(getCourseDetail()))
        return builder.toString()
    }

    init {
        println(
            """
            保存以下内容为 [name].wakeup_schedule 文件可直接导入WakeUp
            注意：文件第一行不能是空格/换行
            注意：不允许出现特殊影响Json格式的符号如：" \n等
            否则会导入失败
            =============================
            
            """.trimIndent()
        )
        val scheduleFileRaw = generate()
        println(scheduleFileRaw)
        println("\n=============================")

        val privateDir = File("./private")
        if (!privateDir.exists()) privateDir.mkdirs()
        val scheduleFileName = "${tableName}.wakeup_schedule"
        val scheduleFile = Paths.get(privateDir.path, scheduleFileName).toFile()
        scheduleFile.writeText(scheduleFileRaw, Charsets.UTF_8)
        println("文件已保存到 ${scheduleFile.path}")
    }
}



