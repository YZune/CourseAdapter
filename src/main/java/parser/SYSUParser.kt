package parser

import bean.Course

class SYSUParser(source: String) : Parser(source) {
    override fun generateCourseList(): List<Course> {
        println("Hello!")
        val courseList = ArrayList<Course>()

        val doc = org.jsoup.Jsoup.parse(source)
        val tables = doc.getElementsByClass("com-table class-schedule-table2")
        var table = tables[0]
        val ths = table.getElementsByTag("tr")[0].getElementsByTag("th")

        // day start from Sunday
        val columnSpanList = ArrayList<Int>()
        for (th in ths) {
            if (th.className() == "th2")
                continue
            columnSpanList.add(th.attr("colspan").toInt())
        }
        assert(columnSpanList.size == 7)

        table = tables[1]
        val tableTbody = table.getElementsByTag("tbody")[0]
        val trs = tableTbody.getElementsByTag("tr")
        for (i in trs.indices) {
            var currentColumnSpan = 0
            var countDay = 0
            val tds = trs[i].getElementsByTag("td")
            for (td in tds) {
                if (td.className() == "td2-1")
                    continue

                val colspan = td.attr("colspan").toInt()
                currentColumnSpan += colspan
//                println(currentColumnSpan)
                if (currentColumnSpan > columnSpanList[countDay]) {
                    countDay++
                    currentColumnSpan = 0
                }
                if (td.className() == "11111")
                    continue

                val rowspan = td.attr("rowspan").toInt()
                val infoStrings = ArrayList<String>()
                val spans = td.getElementsByTag("span")
                for (span in spans) {
                    val infoString = span.text().trim().replace("/", "")
                    infoStrings.add(infoString)
                }

                courseList.add(parseInfoStrings(infoStrings, i+1, i+rowspan, countDay))
            }
        }
        return courseList
    }

    private fun parseInfoStrings(infoStrings: ArrayList<String>, startNode: Int, endNode: Int, countDay: Int): Course {
        var info = infoStrings[0]
        var typeString = ""
        var type = 0
        when {
            info.contains("每周") -> {
                typeString = "每周"
                type = 0
            }
            info.contains("单周") -> {
                typeString = "单周"
                type = 1
            }
            info.contains("双周") -> {
                typeString = "双周"
                type = 2
            }
        }
        val weekInfo = info.replace(typeString, "").split("-")
        val startWeek = weekInfo[0].toInt()
        val endWeek = weekInfo[1].toInt()

        info = infoStrings[1]
        val nameInfo = info.split(Regex("[()]"))
        val name = nameInfo[2]

        info = infoStrings[2]
        val teacher = info

        info = infoStrings[3]
        val room = info

        // week starts from Sunday
        val cday = if (countDay == 0) 7 else countDay

        return Course(
            name = name,
            day = cday,
            room = room,
            teacher = teacher,
            startNode = startNode,
            endNode = endNode,
            startWeek = startWeek,
            endWeek = endWeek,
            type = type
        )
    }
}
