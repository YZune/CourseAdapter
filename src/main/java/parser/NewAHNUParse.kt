package parser

import Common
import bean.Course
import org.jsoup.Jsoup

class NewAHNUParser(source: String) : Parser(source) {
    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()
        val doc = Jsoup.parse(source)
        val kbtable = doc.select("table[id=lessons]").first()
        val kcb = kbtable.getElementsByTag("tbody").first()
        for (tr in kcb.getElementsByTag("tr")) {
            if (tr.className() == "semester_tr") continue
            val td = tr.getElementsByTag("td")
            var courseName: String = "";
            var day = 0;
            var room = "";
            var teacher = "";
            var startNode = 0;
            var endNode = 0;
            var endWeek = 0;
            var startWeek = 0;
            var cnt = 1
            var type = 0
            for (st in td) {
                if (cnt == 1) {
                    var ans = st.select(".showSchedules")
                    courseName = ans.text()
                }
                else if (cnt == 3){
                    val classInfo = st.html().split("<br>")
                    for(t in classInfo){
                        if(t == "不排课" || t == ""){
                            type = -1
                            break
                        }
                        var now = 0
                        var texts = t.split(" ")
                        while(now < 6){
                            if(now == 0){
                                startWeek = texts[0].substringBefore("~").toInt()
                                endWeek = texts[0].substringAfter("~").substringBefore("周").toInt()
                            }
                            else if(now == 1){
                                Common.chineseWeekList.forEachIndexed { index, s ->
                                    if (index != 0)
                                        if (texts[now].contains(s)) {
                                            day = index
                                            return@forEachIndexed
                                        }
                                }
                            }
                            else if(now == 2){
                                var a = texts[now].substringBefore("~")
                                var b = texts[now].substringAfter("~")
                                startNode = Common.containNodeInt(a)
                                endNode = Common.containNodeInt(b)
                            }
                            else if(now == 3){
                                if(texts[now] != "花津校区"){
                                    teacher = texts[now]
                                    break;
                                }

                            }
                            else if(now == 4){
                                room = texts[now]
                            }
                            else if(now == 5){
                                teacher = texts[now]
                            }
                            now += 1
                        }
                        if(type == -1)continue
                        if(teacher.contains(";")){
                            teacher = teacher.substringBefore(";")
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
                                type = 0
                            )
                        )
                    }
                }
                cnt += 1
            }
        }
        return courseList
    }
}