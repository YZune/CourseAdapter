package parser
import Common.getNodeInt
import bean.Course
import org.jsoup.Jsoup

class TCUParser(source: String) : Parser(source){
    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()
        val doc = Jsoup.parse(source)
        val lessonData = doc.select(".titleTop2").get(1)
        val courses = lessonData.select("tr[onmouseout=this.className='even';]")
        var teacherSure = ""
        var courseNameSure = ""

        courses.forEach suki_continueFlag@{
            var addCtrFlag = false
            val course = it
            var courseName = ""
            var day = 0
            var type = 0
            var address = ""
            var teacher = ""
            var startNode = 0
            var endNode = 0
            var startWeek = arrayOf(1,1,1,1,1,1,1,1)
            var endWeek = arrayOf(16,16,16,16,16,16,16,16)
            var WeekCounter = 1
            if (course.select("td[rowspan]").hasText()) {
                courseNameSure = it.select("td[rowspan]").get(2).text()
                teacherSure= it.select("td[rowspan]").get(7).text()
                teacher = teacherSure
                courseName = courseNameSure
                val WeekInfo = it.select("td").get(11).text().substring(0,it.select("td").get(11).text().length-2).trim().split(',')
                WeekCounter = WeekInfo.size
                for( index in 0 until WeekCounter){
                    var WeekDataTemp = WeekInfo[index].split("-");
                    startWeek[index] = WeekDataTemp[0].toInt()
                    endWeek[index] =   if(WeekDataTemp.size>=2)
                                    WeekDataTemp[1].toInt()
                                else startWeek[index]
                }//先用”，“分割几个星期时间段，再用”-“分割始末时间，存入一个数组当中,最多允许分割为8段，不然学校真的不行


                day = it.select("td").get(12).text().toInt()

                startNode = getNodeInt(it.select("td").get(13).text())
                endNode = startNode + it.select("td").get(14).text().toInt()-1;
                address = it.select("td").get(15).text()+it.select("td").get(16).text()+it.select("td").get(17).text()
                addCtrFlag = true
            }
            else if(it.getElementsByTag("td").size<=7){
                teacher = teacherSure
                courseName = courseNameSure
                val WeekInfo = it.select("td").get(0).text().substring(0,it.select("td").get(0).text().length-2).trim().split(',')
                WeekCounter = WeekInfo.size
                for( index in 0 until WeekCounter){
                    var WeekDataTemp = WeekInfo[index].split("-");
                    startWeek[index] = WeekDataTemp[0].toInt()
                    endWeek[index] =   if(WeekDataTemp.size>=2)
                        WeekDataTemp[1].toInt()
                    else startWeek[index]
                }
                day = it.select("td").get(1).text().toInt()
                startNode = getNodeInt(it.select("td").get(2).text())
                endNode = startNode + it.select("td").get(3).text().toInt()-1;
                address = it.select("td").get(4).text()+it.select("td").get(5).text()+it.select("td").get(6).text()
                addCtrFlag = true

            }
            if(addCtrFlag) {
                for(index in 0 until WeekCounter)
                    courseList.add(
                        Course(
                        name = courseName, day = day, room = address,
                        teacher = teacher, startNode = startNode,
                        endNode = endNode, startWeek = startWeek[index],
                        endWeek = endWeek[index], type = type
                        )
                    )
            }

        }
        return courseList
    }
}