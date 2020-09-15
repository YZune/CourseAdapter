package parser

import Common
import bean.Course
import org.jsoup.Jsoup

class AHNUParser(source: String) : Parser(source) {
    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()
        val doc = Jsoup.parse(source)
        val kbtable = doc.select("table[id=kcb]").first()
        val kcb = kbtable.getElementsByTag("tbody").first()
        for (tr in kcb.getElementsByTag("tr")) {
            if (tr.className() == "thtd") continue

            val td = tr.getElementsByTag("td")
            for (st in td) {
                if (st.childNodeSize() <= 1) continue
                var courseName: String = "";
                var day = 0;
                var room = "";
                var teacher = "";
                var startNode = 0;
                var endNode = 0;
                var endWeek = 0;
                var startWeek = 0;
                val classInfo = st.html().split("<br>")
                var cnt = 1
                var t = 0
                classInfo.forEach {
                    val info = Jsoup.parse(it).text().trim().split(' ')
                    if (cnt == 1) courseName = info.toString()
                    else if (cnt == 3) teacher = info.toString()
                    else if (cnt == 4) room = info.toString()
                    else if (cnt == 2) {
                        Common.chineseWeekList.forEachIndexed { index, s ->
                            if (index != 0)
                                if (info.toString().contains(s)) {
                                    day = index
                                    return@forEachIndexed
                                }
                        }
                        t = when {
                            info.toString().contains('单') -> 1
                            info.toString().contains('双') -> 2
                            else -> 0
                        }
                        val matchWeek = Common.weekPattern.find(info.toString())
                        if (matchWeek != null) {
                            val res = matchWeek.value
                            startWeek = res.substringBefore('-').substringAfter('第').toInt()
                            endWeek = res.substringAfter('-').substringBefore('周').toInt()
                        }
                        val p = info.toString().substringAfter('第').substringBefore('节');
                        val startTime = p.substringBefore(',')
                        val endTime = p.substringAfterLast(',')
                        startNode = startTime.toInt()
                        endNode = endTime.toInt()
                    }
                    else if (cnt == 5 && classInfo.size >= 8) {
                        courseList.add(
                            Course(
                                name = courseName.removeSurrounding("[", "]"),
                                day = day,
                                room = room.removeSurrounding("[", "]"),
                                teacher = teacher.removeSurrounding("[", "]"),
                                startNode = startNode,
                                endNode = endNode,
                                startWeek = startWeek,
                                endWeek = endWeek,
                                type = t
                            )
                        )
                    } else if (cnt == 6) courseName = info.toString()
                    else if (cnt == 7) {
                        Common.chineseWeekList.forEachIndexed { index, s ->
                            if (index != 0)
                                if (info.toString().contains(s)) {
                                    day = index
                                    return@forEachIndexed
                                }
                        }
                        t = when {
                            info.toString().contains('单') -> 1
                            info.toString().contains('双') -> 2
                            else -> 0
                        }
                        val matchWeek = Common.weekPattern.find(info.toString())
                        if (matchWeek != null) {
                            val res = matchWeek.value
                            startWeek = res.substringBefore('-').substringAfter('第').toInt()
                            endWeek = res.substringAfter('-').substringBefore('周').toInt()
                        }
                        val p = info.toString().substringAfter('第').substringBefore('节');
                        startNode = p[0].toInt() - 48
                        endNode = p[p.lastIndex].toInt() - 48
                    } else if (cnt == 8) teacher = info.toString()
                    else if (cnt == 9) room = info.toString()
                    cnt += 1
                }
                courseList.add(
                    Course(
                        name = courseName.removeSurrounding("[", "]"),
                        day = day,
                        room = room.removeSurrounding("[", "]"),
                        teacher = teacher.removeSurrounding("[", "]"),
                        startNode = startNode,
                        endNode = endNode,
                        startWeek = startWeek,
                        endWeek = endWeek,
                        type = t
                    )
                )
            }
        }
        return courseList
    }
}