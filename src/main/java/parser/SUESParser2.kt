package main.java.parser

import bean.Course
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import main.java.bean.TimeDetail
import main.java.bean.TimeTable
import main.java.exception.EmptyException
import main.java.exception.GetTermDataErrorException
import org.jsoup.Jsoup
import parser.Parser

class SUESParser2(source: String) : Parser() {

    //按照 Cookie 值判断是否使用 WebVPN
    private val baseURL =
        if (source.contains("wrdvpn")) "https://webvpn.sues.edu.cn/https/77726476706e69737468656265737421faef478b69237d556d468ca88d1b203b" else "https://jxfw.sues.edu.cn"

    //判断连接是否成功
    private val testSchoolName = let {
        try {
            JsonParser.parseString(
                Jsoup.connect("$baseURL/student/params/get-schoolName")
                    .header("Cookie", source)
                    .ignoreContentType(true)
                    .execute().body()
            )
        } catch (_: JsonSyntaxException) {
            throw GetTermDataErrorException("似乎还没有登录，请刷新并登录后再操作。")
        }
    }

    //获取课表页面 HTML
    private val tableHTML =
        Jsoup.connect("$baseURL/student/for-std/course-table")
            .header("Cookie", source)
            .execute().body()

    private var personID = Regex("var personId = ([0-9]*?);").find(tableHTML)?.groupValues?.get(1)

    //从课表页面读取学期信息
    private val courseTableText =
        Regex("var semesters = JSON\\.parse\\(\\s*'([^']*)'\\s*\\)").find(tableHTML)?.groupValues?.get(1)
            ?: throw EmptyException("没有获取到课表配置，请检查课表中是否有课。")

    data class Semester(
        val name: String, val id: String
    )

    private var semester: Semester? = null

    fun getSemesterList(): List<Semester> {
        return JsonParser.parseString(courseTableText.replace("\\\"", "\""))
            .asJsonArray.map {
                Semester(
                    name = it.asJsonObject.get("name").asString,
                    id = it.asJsonObject.get("id").asInt.toString()
                )
            }
    }

    fun setSemester(s: Semester) {
        semester = s
    }

    private var json: JsonObject? = null

    private var timetable: JsonObject = JsonParser.parseString("{\"nameZh\": \"新课表布局\",\"nameEn\": null,\"id\": 21,\"enabled\": true,\"changeMonth\": null,\"changeDayOfMonth\": null,\"courseUnitList\": [{\"nameZh\": \"第一节\",\"nameEn\": \"unit1\",\"timeTableLayoutAssoc\": 21,\"indexNo\": 1,\"startTime\": 815,\"endTime\": 855,\"dayPart\": \"MORNING\",\"color\": \"#ffffff\",\"changeStartTime\": 0,\"changeEndTime\": 0,\"segmentIndex\": 1,\"name\": \"第一节\"},{\"nameZh\": \"第二节\",\"nameEn\": \"unit2\",\"timeTableLayoutAssoc\": 21,\"indexNo\": 2,\"startTime\": 855,\"endTime\": 935,\"dayPart\": \"MORNING\",\"color\": \"#ffffff\",\"changeStartTime\": 0,\"changeEndTime\": 0,\"segmentIndex\": 1,\"name\": \"第二节\"},{\"nameZh\": \"第三节\",\"nameEn\": \"unit3\",\"timeTableLayoutAssoc\": 21,\"indexNo\": 3,\"startTime\": 955,\"endTime\": 1040,\"dayPart\": \"MORNING\",\"color\": \"#ffffff\",\"changeStartTime\": 0,\"changeEndTime\": 0,\"segmentIndex\": 2,\"name\": \"第三节\"},{\"nameZh\": \"第四节\",\"nameEn\": \"unit4\",\"timeTableLayoutAssoc\": 21,\"indexNo\": 4,\"startTime\": 1040,\"endTime\": 1135,\"dayPart\": \"MORNING\",\"color\": \"#ffffff\",\"changeStartTime\": 0,\"changeEndTime\": 0,\"segmentIndex\": 2,\"name\": \"第四节\"},{\"nameZh\": \"第五节\",\"nameEn\": \"unit5\",\"timeTableLayoutAssoc\": 21,\"indexNo\": 5,\"startTime\": 1135,\"endTime\": 1220,\"dayPart\": \"MORNING\",\"color\": \"#ffffff\",\"changeStartTime\": 0,\"changeEndTime\": 0,\"segmentIndex\": 2,\"name\": \"第五节\"},{\"nameZh\": \"第六节\",\"nameEn\": \"unit6\",\"timeTableLayoutAssoc\": 21,\"indexNo\": 6,\"startTime\": 1320,\"endTime\": 1400,\"dayPart\": \"AFTERNOON\",\"color\": \"#ffffff\",\"changeStartTime\": 0,\"changeEndTime\": 0,\"segmentIndex\": 3,\"name\": \"第六节\"},{\"nameZh\": \"第七节\",\"nameEn\": \"unit7\",\"timeTableLayoutAssoc\": 21,\"indexNo\": 7,\"startTime\": 1400,\"endTime\": 1440,\"dayPart\": \"AFTERNOON\",\"color\": \"#ffffff\",\"changeStartTime\": 0,\"changeEndTime\": 0,\"segmentIndex\": 3,\"name\": \"第七节\"},{\"nameZh\": \"第八节\",\"nameEn\": \"unit8\",\"timeTableLayoutAssoc\": 21,\"indexNo\": 8,\"startTime\": 1500,\"endTime\": 1540,\"dayPart\": \"AFTERNOON\",\"color\": \"#ffffff\",\"changeStartTime\": 0,\"changeEndTime\": 0,\"segmentIndex\": 4,\"name\": \"第八节\"},{\"nameZh\": \"第九节\",\"nameEn\": \"unit9\",\"timeTableLayoutAssoc\": 21,\"indexNo\": 9,\"startTime\": 1540,\"endTime\": 1620,\"dayPart\": \"AFTERNOON\",\"color\": \"#ffffff\",\"changeStartTime\": 0,\"changeEndTime\": 0,\"segmentIndex\": 4,\"name\": \"第九节\"},{\"nameZh\": \"第十节\",\"nameEn\": \"unit10\",\"timeTableLayoutAssoc\": 21,\"indexNo\": 10,\"startTime\": 1635,\"endTime\": 1715,\"dayPart\": \"EVENING\",\"color\": \"#ffffff\",\"changeStartTime\": 0,\"changeEndTime\": 0,\"segmentIndex\": 5,\"name\": \"第十节\"},{\"nameZh\": \"第十一节\",\"nameEn\": \"unit11\",\"timeTableLayoutAssoc\": 21,\"indexNo\": 11,\"startTime\": 1715,\"endTime\": 1755,\"dayPart\": \"EVENING\",\"color\": \"#ffffff\",\"changeStartTime\": 0,\"changeEndTime\": 0,\"segmentIndex\": 5,\"name\": \"第十一节\"},{\"nameZh\": \"第十二节\",\"nameEn\": \"unit12\",\"timeTableLayoutAssoc\": 21,\"indexNo\": 12,\"startTime\": 1810,\"endTime\": 1850,\"dayPart\": \"EVENING\",\"color\": \"#ffffff\",\"changeStartTime\": 0,\"changeEndTime\": 0,\"segmentIndex\": 6,\"name\": \"第十二节\"},{\"nameZh\": \"第十三节\",\"nameEn\": \"unit13\",\"timeTableLayoutAssoc\": 21,\"indexNo\": 13,\"startTime\": 1850,\"endTime\": 1930,\"dayPart\": \"EVENING\",\"color\": \"#ffffff\",\"changeStartTime\": 0,\"changeEndTime\": 0,\"segmentIndex\": 6,\"name\": \"第十三节\"},{\"nameZh\": \"第十四节\",\"nameEn\": \"unit14\",\"timeTableLayoutAssoc\": 21,\"indexNo\": 14,\"startTime\": 1935,\"endTime\": 2015,\"dayPart\": \"EVENING\",\"color\": \"#ffffff\",\"changeStartTime\": 0,\"changeEndTime\": 0,\"segmentIndex\": 7,\"name\": \"第十四节\"},{\"nameZh\": \"第十五节\",\"nameEn\": \"unit15\",\"timeTableLayoutAssoc\": 21,\"indexNo\": 15,\"startTime\": 2020,\"endTime\": 2100,\"dayPart\": \"EVENING\",\"color\": \"#ffffff\",\"changeStartTime\": 0,\"changeEndTime\": 0,\"segmentIndex\": 7,\"name\": \"第十五节\"}],\"maxEndTime\": 2100,\"minStartTime\": 815,\"minIndexNo\": 1,\"maxIndexNo\": 15,\"transient\": false,\"name\": \"新课表布局\"}").asJsonObject

    override fun generateTimeTable(): TimeTable {
        fun getTime(time: Int): String {
            var txt = time.toString()
            if (txt.length == 3) {
                txt = "0$txt"
            }
            return "${txt.slice(0..1)}:${txt.slice(2..3)}"
        }
        return TimeTable(
            name = timetable.get("name").asString,
            timeList = timetable.getAsJsonArray("courseUnitList").map {
                TimeDetail(
                    node = it.asJsonObject.get("indexNo").asInt,
                    startTime = getTime(it.asJsonObject.get("startTime").asInt),
                    endTime = getTime(it.asJsonObject.get("endTime").asInt)
                )
            })
    }

    override fun getTableName(): String = "课表"

    override fun getNodes(): Int = timetable.getAsJsonArray("courseUnitList").size()

    override fun getStartDate(): String =
        json!!.getAsJsonArray("studentTableVms")[0].asJsonObject
            .getAsJsonArray("arrangedLessonSearchVms")[0].asJsonObject
            .getAsJsonObject("semester").get("startDate").asString ?: "2021-9-6"

    override fun getMaxWeek(): Int {
        var weeks = 30
        if (personID != null) {
            val weeksDataUrl = "$baseURL/student/for-std/course-table/get-data?semesterId=${semester!!.id}&dataId=$personID&bizTypeId=2"
            val weeksDataJson = Jsoup.connect(weeksDataUrl).header("Cookie", source)
                .ignoreContentType(true)
                .execute().body()
            val weeksDataObject = JsonParser.parseString(weeksDataJson).asJsonObject
            if (weeksDataObject.getAsJsonArray("weekIndices").size() > 0) {
                val weekIndices = weeksDataObject.getAsJsonArray("weekIndices")
                weeks = weekIndices[weekIndices.size() - 1].asInt
            }
        }
        return weeks
    }

    override fun generateCourseList(): List<Course> {
        if (semester == null)
            throw GetTermDataErrorException("还未选择导入学期，请选择后继续。")
        json = JsonParser.parseString(Jsoup.connect(baseURL
                + "/student/for-std/course-table/semester/${semester!!.id}/print-data"
                + "?semesterId=${semester!!.id}&hasExperiment=true"
            ).header("Cookie", source)
                .ignoreContentType(true)
                .execute().body()
        ).asJsonObject
        if (json == null)
            throw EmptyException("没有获取到课表，请检查课表中是否有课。")
        timetable = json!!.getAsJsonArray("studentTableVms")[0].asJsonObject.getAsJsonObject("timeTableLayout")
        return json!!.getAsJsonArray("studentTableVms")[0]
            .asJsonObject.getAsJsonArray("activities")
            .filter { e -> !e.asJsonObject.get("room").isJsonNull }
            .map { it ->
                fun getTime(room: String, node: Int, end: Boolean, fallback: String): String {
                    return if (Regex("""([AF][0-9]{3}|J301)多""").matches(room)) {
                        when (node) {
                            3 -> if (!end) "09:55" else "10:35"
                            4 -> if (!end) "10:40" else "11:20"
                            5 -> if (!end) "11:20" else "12:00"
                            else -> if (fallback.length == 4) "0$fallback" else fallback
                        }
                    } else if (Regex("""([DE][0-9]{3}|J303)(多|\(中外教室）)""").matches(room)) {
                        when (node) {
                            3 -> if (!end) "10:15" else "10:55"
                            4 -> if (!end) "10:55" else "11:35"
                            5 -> if (!end) "11:40" else "12:20"
                            else -> if (fallback.length == 4) "0$fallback" else fallback
                        }
                    } else {
                        when (node) {
                            3 -> if (!end) "09:55" else "10:35"
                            4 -> if (!end) "10:35" else "11:15"
                            5 -> if (!end) "11:20" else "12:00"
                            else -> if (fallback.length == 4) "0$fallback" else fallback
                        }
                    }
                }
                Common.weekIntList2WeekBeanList(it.asJsonObject.getAsJsonArray("weekIndexes").map { i ->
                    i.asInt
                }.toMutableList()).map { week ->
                    val startNode = it.asJsonObject.get("startUnit").asInt
                    val endNode = it.asJsonObject.get("endUnit").asInt
                    if (startNode <= 5 && endNode >= 6) {
                        arrayListOf(
                            Course(
                                name = it.asJsonObject.get("courseName").asString,
                                day = it.asJsonObject.get("weekday").asInt,
                                room = it.asJsonObject.get("room").asString,
                                teacher = it.asJsonObject.getAsJsonArray("teachers").map { it.asString }
                                    .joinToString(" "),
                                startNode = startNode,
                                endNode = 5,
                                startWeek = week.start,
                                endWeek = week.end,
                                type = week.type,
                                credit = it.asJsonObject.get("credits").asFloat,
                                note = it.asJsonObject.get("lessonRemark")
                                    .let { n -> if (n.isJsonNull) "" else n.asString },
                                startTime = getTime(
                                    it.asJsonObject.get("room").asString,
                                    startNode, false,
                                    it.asJsonObject.get("startTime").asString
                                ),
                                endTime = getTime(
                                    it.asJsonObject.get("room").asString,
                                    5, true, "12:20"
                                ),
                            ), Course(
                                name = it.asJsonObject.get("courseName").asString,
                                day = it.asJsonObject.get("weekday").asInt,
                                room = it.asJsonObject.get("room").asString,
                                teacher = it.asJsonObject.getAsJsonArray("teachers").map { it.asString }
                                    .joinToString(" "),
                                startNode = 6,
                                endNode = endNode,
                                startWeek = week.start,
                                endWeek = week.end,
                                type = week.type,
                                credit = it.asJsonObject.get("credits").asFloat,
                                note = it.asJsonObject.get("lessonRemark")
                                    .let { n -> if (n.isJsonNull) "" else n.asString },
                                startTime = getTime(
                                    it.asJsonObject.get("room").asString,
                                    6, false, "13:20"
                                ),
                                endTime = getTime(
                                    it.asJsonObject.get("room").asString,
                                    endNode, true,
                                    it.asJsonObject.get("endTime").asString
                                ),
                            )
                        )
                    } else {
                        arrayListOf(
                            Course(
                                name = it.asJsonObject.get("courseName").asString,
                                day = it.asJsonObject.get("weekday").asInt,
                                room = it.asJsonObject.get("room").asString,
                                teacher = it.asJsonObject.getAsJsonArray("teachers").map { it.asString }
                                    .joinToString(" "),
                                startNode = startNode,
                                endNode = endNode,
                                startWeek = week.start,
                                endWeek = week.end,
                                type = week.type,
                                credit = it.asJsonObject.get("credits").asFloat,
                                note = it.asJsonObject.get("lessonRemark")
                                    .let { n -> if (n.isJsonNull) "" else n.asString },
                                startTime = getTime(
                                    it.asJsonObject.get("room").asString,
                                    startNode, false,
                                    it.asJsonObject.get("startTime").asString
                                ),
                                endTime = getTime(
                                    it.asJsonObject.get("room").asString,
                                    endNode, true,
                                    it.asJsonObject.get("endTime").asString
                                ),
                            )
                        )
                    }
                }.flatten()
            }.flatten()
    }
}
