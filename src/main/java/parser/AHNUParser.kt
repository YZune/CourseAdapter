package parser

import bean.Course
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements


class AHNUParser (source: String) : Parser(source) {
    override fun generateCourseList(): List<Course> {
        val CourseList = arrayListOf<Course> ()
        val doc = Jsoup.parse(source)
        val kbtable = doc.select("table[id=kcb]").first()
        val kcb= kbtable.getElementsByTag("tbody").first()
        for (tr in kcb.getElementsByTag("tr"))
        {
            if(tr.className() == "thtd")continue

            val td = tr.getElementsByTag("td")
            for(st in td)
            {
                if(st.childNodeSize() <= 1)continue
                //println(st)
                var CourseName : String =""; var day = 0 ;var room = "" ;var teacher = "";var startNode = 0;var endNode = 0;var endWeek = 0;var startWeek = 0;
                val ClassInfo = st.html().split("<br>")
                var cnt = 1; var t : Int = 0;
                ClassInfo.forEach{
                    val info = Jsoup.parse(it).text().trim().split(' ')
                        if(cnt == 1)CourseName = info.toString()
                        else if(cnt == 3)teacher = info.toString()
                        else if(cnt == 4)room = info.toString()
                        else if(cnt == 2) {
                            Common.chineseWeekList.forEachIndexed { index, s ->
                                if (index != 0)
                                    if (info.toString().contains(s)) {
                                        day = index
                                        return@forEachIndexed
                                    }
                            }
                            if(info.toString().contains('单')) t = 1;
                            else if(info.toString().contains('双'))t = 2;
                            else t = 0;
                            val MatchWeek = Common.weekPattern.find(info.toString())
                            if (MatchWeek != null)
                            {
                                val res = MatchWeek.value
                                startWeek = res.substringBefore('-').substringAfter('第').toInt()
                                endWeek = res.substringAfter('-').substringBefore('周').toInt()
                            }
                            val p = info.toString().substringAfter('第').substringBefore('节');
                            startNode = p[0].toInt() - 48
                            endNode = p[p.lastIndex].toInt() - 48
                        }
                        else if(cnt == 5 && ClassInfo.size >= 8)
                        {
                            CourseList.add(
                                Course(
                                    name = CourseName.removeSurrounding("[","]"),day = day,room = room.removeSurrounding("[","]"),teacher = teacher.removeSurrounding("[","]"),startNode = startNode,
                                    endNode = endNode,startWeek = startWeek,endWeek = endWeek,type = t
                                )
                            )
                        }
                        else if(cnt == 6)CourseName = info.toString()
                        else if(cnt == 7) {
                            Common.chineseWeekList.forEachIndexed { index, s ->
                                if (index != 0)
                                    if (info.toString().contains(s)) {
                                        day = index
                                        return@forEachIndexed
                                    }
                            }
                            if(info.toString().contains('单')) t = 1;
                            else if(info.toString().contains('双'))t = 2;
                            else t = 0;
                            val MatchWeek = Common.weekPattern.find(info.toString())
                            if (MatchWeek != null)
                            {
                                val res = MatchWeek.value
                                startWeek = res.substringBefore('-').substringAfter('第').toInt()
                                endWeek = res.substringAfter('-').substringBefore('周').toInt()
                            }
                            val p = info.toString().substringAfter('第').substringBefore('节');
                            startNode = p[0].toInt() - 48
                            endNode = p[p.lastIndex].toInt() - 48
                        }
                        else if(cnt == 8)teacher = info.toString()
                        else if(cnt == 9)room = info.toString()
                        cnt += 1
                }
                CourseList.add(
                    Course(
                        name = CourseName.removeSurrounding("[","]"),day = day,room = room.removeSurrounding("[","]"),teacher = teacher.removeSurrounding("[","]"),startNode = startNode,
                        endNode = endNode,startWeek = startWeek,endWeek = endWeek,type = t
                    )
                )
            }
        }
        return CourseList
    }
}