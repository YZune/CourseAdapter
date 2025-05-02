package main.java.parser
import Common
import bean.Course
import bean.CourseBaseBean
import bean.CourseDetailBean
import parser.Parser

class ZjvtitParser(source: String) : Parser() {

    override fun generateCourseList(): List<Course> {
        val course: MutableList<Course> = mutableListOf()
        val doc = org.jsoup.Jsoup.parse(source)
        val courseTable = doc.select("table[class=scheduleTable table table-bordered table-hover]")//整个课程表
        val courses = courseTable.select("div.courseInfo")
        val courseNames: MutableList<String> = mutableListOf()
        val courseBaseBeans: MutableList<CourseBaseBean> = mutableListOf()
        val courseDetailBeans: MutableList<CourseDetailBean> = mutableListOf()
        var courseNum = 0
        for(courseInfo in courses){
            val courseName = courseInfo.select("span:not([class])").text().trim()
            if(courseName !in courseNames){
                courseNames.add(courseName)
                courseBaseBeans.add(CourseBaseBean(courseNum,courseName,"",0,""))
                courseNum++
            }
        }
        for(i in courseBaseBeans)
            println(i)

        for(courseInfoList in courseTable.select("[week]")) {
            var courseId: Int = -1
            var courseName: String
            var courseDay: Int
            var courseRoom: String
            var courseTeacher: String
            var courseNode: Int
            var courseStartWeek: Int
            var courseEndWeek: Int
            var courseType: Int
            for(courseInfo in courseInfoList.select(".courseInfo")){
                if (courseInfo.text() != ""){
                    println("-----------------------------------------------------")
                    println(courseInfo)
                    println("=====================================================")
                    courseName = courseInfo.select("span:not([class])").text().trim()
                    println("课程名称: $courseName")
                    //寻找课程名字对应的id
                    for (i in courseBaseBeans) {
                        if (i.courseName == courseName)
                            courseId = i.id
                    }
                    courseDay = Common.englishAbbrWeekList.indexOf(courseInfoList.select("td")[0].attr("week"))
                    courseRoom = courseInfo.select(".place")[0].text()
                    courseTeacher = courseInfo.select(".teacher")[0].text()
                    courseNode = courseInfoList.select("[lesson]")[0].attr("lesson").toInt()
                    val betweenWeeks = divideName(courseInfo.select(".WeekDetail")[0].text())
                    for (i in betweenWeeks) {
                        courseType = i.first
                        courseStartWeek = i.second.first
                        courseEndWeek = i.second.second
                        //添加detailNode

                        println("添加detailNode: $courseId ,$courseTeacher ")
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
                Course(
                    name,
                    i.day,
                    i.room!!,
                    i.teacher!!,
                    i.startNode,
                    i.startNode+i.step-1,
                    i.startWeek,
                    i.endWeek,
                    i.type,
                )
            )
        }
        return course
    }
    private fun divideName(name:String): MutableList<Pair<Int,Pair<Int,Int>>> {
        val divide = name.split(",")
        var flag: Int
        val betweenWeeks: MutableList<Pair<Int, Pair<Int, Int>>> = mutableListOf()
        var startWeek: Int
        var endWeek: Int
        for (node in divide) {
            if(node==node.split("-")[0]){
                //第18周
                if(node.indexOf("第")==-1 && node.indexOf("周")==-1){
                    startWeek =  node.toInt()
                    endWeek = startWeek
                    betweenWeeks.add(Pair(0,Pair(startWeek,endWeek)))
                }
                else if(node.indexOf("第")==-1 && node.indexOf("周")!=-1){
                    startWeek =  node.split("周")[0].toInt()
                    endWeek = startWeek
                    betweenWeeks.add(Pair(0,Pair(startWeek,endWeek)))
                }
                else if(node.indexOf("第")!=-1 && node.indexOf("周")==-1){
                    startWeek =  node.split("第")[1].toInt()
                    endWeek = startWeek
                    betweenWeeks.add(Pair(0,Pair(startWeek,endWeek)))
                }else if(node.indexOf("第")!=-1 && node.indexOf("周")!=-1){
                    startWeek = node.split("第")[1].split("周")[0].toInt()
                    endWeek = startWeek
                    betweenWeeks.add(Pair(0,Pair(startWeek,endWeek)))
                }

            }
            else {
                //8-18 第8-18周 8-18周 第8-18 18周 8-18(单周)
                val lNode = node.split("-")[0]//第8 8
                startWeek = if(lNode.indexOf("第")==-1){//8
                    //["8","18周"]
                    lNode.toInt()
                }else {
                    lNode.split("第")[1].toInt()
                }
                //18 18周 18(单周)
                val rNode = node.split("-")[1]//17周
                endWeek = if (rNode.indexOf("周")>2){
                    rNode.split("(")[0].toInt()
                }else {
                    //18周 18(单周)
                    rNode.split("周")[0].toInt()
                }

                flag = when {
                    Common.countStr(node, "单周") != 0 -> 1
                    Common.countStr(node, "双周") != 0 -> 2
                    else -> 0
                }
                betweenWeeks.add(Pair(flag, Pair(startWeek, endWeek)))
            }
        }
        return betweenWeeks
    }
}

