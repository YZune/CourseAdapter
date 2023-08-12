package main.java.test

import main.java.parser.SIMCParser
import java.io.File

fun main() {
    val source = File("d:/pkjg!index.action.html").readText()
    SIMCParser(source).apply {
        saveCourse()
        val timeTable=generateTimeTable()
        var timeTableText="["
        timeTable.timeList.forEach{
            timeTableText+="{"+
                "\"endTime\":\"${it.endTime}\","+
                "\"node\":${it.node},"+
                "\"startTime\":\"${it.startTime}\","+
                "\"timeTable\":1"+
                "},"
        }
        timeTableText=timeTableText.removeSuffix(",")+"]"

        //test.wakeup_schedule
        println("replace first 3 lines with following output data:")
        println("=============================")

        //header
        println("{\"courseLen\":45,\"id\":1,\"name\":\"${timeTable.name}\",\"sameBreakLen\":false,\"sameLen\":true,\"theBreakLen\":20}")

        //timePreference
        println(timeTableText)

        //colorScheme
        println("{\"background\":\"\",\"courseTextColor\":-1,\"id\":1,\"itemAlpha\":60," +
                "\"itemHeight\":64,\"itemTextSize\":12,\"maxWeek\":${getMaxWeek()?:30},\"nodes\":${getNodes()}," +
                "\"showOtherWeekCourse\":true,\"showSat\":true,\"showSun\":true," +
                "\"showTime\":false,\"startDate\":\"${getStartDate()?:"2023-9-4"}\",\"strokeColor\":-2130706433," +
                "\"sundayFirst\":false,\"tableName\":\"${getTableName()}\",\"textColor\":-16777216," +
                "\"timeTable\":1,\"type\":0,\"widgetCourseTextColor\":-1,\"widgetItemAlpha\":60," +
                "\"widgetItemHeight\":64,\"widgetItemTextSize\":12,\"widgetStrokeColor\":-2130706433," +
                "\"widgetTextColor\":-16777216}")

        //courseList(baseList)
        //courseDetailList(detailList)

        println("=============================")
    }
}
