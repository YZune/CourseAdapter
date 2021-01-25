package main.java.parser
import Common
import bean.Course
import bean.CourseBaseBean
import bean.CourseDetailBean
import parser.Parser

class ZjvtitParser(source: String) : Parser(source) {

    override fun generateCourseList(): List<Course> {

        val course: MutableList<Course> = mutableListOf()
        val doc = org.jsoup.Jsoup.parse(source)
        val courseTable = doc.select("table[class=scheduleTable table table-bordered table-hover]")
        val courses = courseTable.select("div.courseInfo")
        val courseNames: MutableList<String> = mutableListOf()
        val courseBaseBeans: MutableList<CourseBaseBean> = mutableListOf()
        val courseDetailBeans: MutableList<CourseDetailBean> = mutableListOf()
        var courseNum = 0
        for(courseInfo in courses){
            val courseName = courseInfo.select("span:not([class])").text().trim()
            if(courseName !in courseNames){
                courseNames.add(courseName)
                courseBaseBeans.add(CourseBaseBean(courseNum,courseName,"",0))
                courseNum++
            }
        }
        for(courseInfo in courseTable.select("[week]")) {
            var courseId: Int = -1
            var courseName: String
            var courseDay: Int
            var courseRoom: String
            var courseTeacher: String
            var courseNode: Int
            var courseStartWeek: Int
            var courseEndWeek: Int
            var courseType: Int
            if (courseInfo.text() != "") {
                courseName = courseInfo.select("span:not([class])").text().trim()
                for (i in courseBaseBeans) {
                    if (i.courseName == courseName)
                        courseId = i.id
                }
                courseDay = Common.englishAbbrWeekList.indexOf(courseInfo.select("td")[0].attr("week"))
                courseRoom = courseInfo.select(".place")[0].text()
                courseTeacher = courseInfo.select(".teacher")[0].text()
                courseNode = courseInfo.select("[lesson]")[0].attr("lesson").toInt()
                val betweenWeeks = divideName(courseInfo.select(".WeekDetail")[0].text())
                for (i in betweenWeeks) {
                    courseType = i.first
                    courseStartWeek = i.second.first
                    courseEndWeek = i.second.second
                    //添加detailNode
                    courseDetailBeans.add(
                        CourseDetailBean(
                            courseId,
                            courseDay,
                            courseRoom,
                            courseTeacher,
                            courseNode,
                            1,
                            courseStartWeek,
                            courseEndWeek,
                            courseType,
                            0
                        )
                    )
                }
            }
        }
        //合并重复课程
            for(i in 0 until courseDetailBeans.size-1){
                for (j in i+1 until  courseDetailBeans.size){
                    if(
                        (courseDetailBeans[i].id == courseDetailBeans[j].id) and
                        (courseDetailBeans[i].day == courseDetailBeans[j].day) and
                        (courseDetailBeans[i].room == courseDetailBeans[j].room) and
                        (courseDetailBeans[i].teacher == courseDetailBeans[j].teacher) and
                        (courseDetailBeans[i].startWeek == courseDetailBeans[j].startWeek) and
                        (courseDetailBeans[i].endWeek == courseDetailBeans[j].endWeek)
                    ){
                        if(courseDetailBeans[i].startNode+courseDetailBeans[i].step==courseDetailBeans[j].startNode){
                            courseDetailBeans[i].step+=courseDetailBeans[j].step
                            courseDetailBeans[j] = CourseDetailBean(-1,1,"1","",1,1,1,1,1,1)

                        }
                    }
                }
            }
        //删除多余
        val it = courseDetailBeans.iterator()
        while(it.hasNext()){
            if(it.next().id==-1)
                it.remove()
        }

        //装填课程
        for(i in courseDetailBeans){
            var name = ""
            for(j in courseBaseBeans){
                if(i.id==j.id)
                    name = j.courseName
            }
            course.add(
                Course(name,
                    i.day,
                    i.room!!,
                    i.teacher!!,
                    i.startNode,
                    i.startNode+i.step-1,
                    i.startWeek,
                    i.endWeek,
                    i.type
                )
            )
        }
        return course
    }
    private fun divideName(name:String): MutableList<Pair<Int,Pair<Int,Int>>> {
        val divide = name.split(",")
        var flag: Int
        val betweenWeeks: MutableList<Pair<Int, Pair<Int, Int>>> = mutableListOf()
        //第1-17周,第2-18周
        for (node in divide) {
            val lNode = node.split("-")[0]//第1
            val rNode = node.split("-")[1]//17周
            val startWeek = lNode.split("第")[1].toInt()
            val endWeek = rNode.split("周")[0].toInt()
            flag = when {
                Common.countStr(node, "单周") != 0 -> 1
                Common.countStr(node, "双周") != 0 -> 2
                else -> 0
            }
            betweenWeeks.add(Pair(flag, Pair(startWeek, endWeek)))
        }
        return betweenWeeks
    }
}
//fun divideName(name:String): MutableList<Pair<Int,Pair<Int,Int>>> {
//    val divide = name.split(",")
//    var flag: Int
//    val betweenWeeks: MutableList<Pair<Int, Pair<Int, Int>>> = mutableListOf()
//    //第1-17周,第2-18周
//    for (node in divide) {
//        val lNode = node.split("-")[0]//第1
//        val rNode = node.split("-")[1]//17周
//        val startWeek = lNode.split("第")[1].toInt()
//        val endWeek = rNode.split("周")[0].toInt()
//        flag = when {
//            Common.countStr(node, "单周") != 0 -> 1
//            Common.countStr(node, "双周") != 0 -> 2
//            else -> 0
//        }
//        betweenWeeks.add(Pair(flag, Pair(startWeek, endWeek)))
//    }
//    return betweenWeeks
//}
