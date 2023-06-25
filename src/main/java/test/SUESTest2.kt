package main.java.test

import main.java.parser.SUESParser2

fun main() {
    //先在浏览器登录，按下导入按钮时传入 Cookie
    //val source = "SESSION=00000000-0000-0000-0000-000000000000; __pstsid__=00000000000000000000000000000000|00000000000000000000000000000000"
    val source = "wengine_vpn_ticketwebvpn_sues_edu_cn=0000000000000000; route=00000000000000000000000000000000; show_vpn=0; show_faq=0; wrdvpn_upstream_ip=0.0.0.0; refresh=1"
    SUESParser2(source).apply {
        val list = getSemesterList() //获取学期列表
        setSemester(list[0]) //测试默认用第一个学期，如果可以弹窗选择就更好了
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
        println("{\"courseLen\":40,\"id\":1,\"name\":\"${timeTable.name}\",\"sameBreakLen\":false,\"sameLen\":true,\"theBreakLen\":20}")

        //timePreference
        println(timeTableText)

        //colorScheme
        println("{\"background\":\"\",\"courseTextColor\":-1,\"id\":1,\"itemAlpha\":60," +
                "\"itemHeight\":64,\"itemTextSize\":12,\"maxWeek\":${getMaxWeek()},\"nodes\":${getNodes()}," +
                "\"showOtherWeekCourse\":true,\"showSat\":true,\"showSun\":true," +
                "\"showTime\":false,\"startDate\":\"${getStartDate()}\",\"strokeColor\":-2130706433," +
                "\"sundayFirst\":false,\"tableName\":\"${getTableName()}\",\"textColor\":-16777216," +
                "\"timeTable\":1,\"type\":0,\"widgetCourseTextColor\":-1,\"widgetItemAlpha\":60," +
                "\"widgetItemHeight\":64,\"widgetItemTextSize\":12,\"widgetStrokeColor\":-2130706433," +
                "\"widgetTextColor\":-16777216}")

        //courseList(baseList)
        //courseDetailList(detailList)

        println("=============================")
    }
}
