package main.java.parser

import bean.Course
import parser.Parser
import java.util.*


class HUNNUParser(source: String?) : Parser() {


    /*
    object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            Log.d("page finished", "onPageFinished: $url")
            if (url != null) {
                if (url.contains("courseTableForStd.action")) {
                    //从courseTableForStd.action中获取ids
                    wv.loadUrl("javascript:window.local_obj.getIds(document.getElementsByTagName('html')[0].innerHTML)")
                }
                if (url.contains("dataQuery.action?dataType=semesterCalendar")) {
                    //从dataQuery.action?dataType=semesterCalendar中获取学期信息，让用户选择学期
                    wv.loadUrl("javascript:window.local_obj.selectSemester(document.getElementsByTagName('html')[0].innerText)")
                }
                if (url.contains("!courseTable.action")) {
                    //wv.loadUrl("javascript:window.local_obj.showSource(document.getElementsByTagName('body')[0].innerHTML)")
                    //到达具有课程信息的页面，导入课程
                    wv.loadUrl("javascript:window.local_obj.importCourse(document.getElementsByTagName('html')[0].innerHTML)")
                }
            }
        }
    }
    //进入教务系统
    val jwglHost = "https://jwglnew.hunnu.edu.cn"
    wv.loadUrl(jwglHost)
    //导入按钮点击事件，提示用户登录成功后再点击;用户点击导入后跳转courseTableForStd.action获取ids
    findViewById<View>(R.id.btn_import).setOnClickListener {
        wv.loadUrl("$jwglHost/eams/courseTableForStd.action")
    }



    var ids = ""
    inner class InJavaScriptLocalObj {
        @JavascriptInterface
        fun showSource(html: String) {
            MaterialAlertDialogBuilder(this@ImportActivity).setMessage(html).show()
        }

        @JavascriptInterface
        fun importCourse(html: String) {
            //导入
            parseTest(html)
        }

        @JavascriptInterface
        fun getIds(html: String) {
            //获取ids
            val find = Regex("bg\\.form\\.addInput\\(form,\"ids\",\"(\\d+)\"\\);").find(html)
            //showSource(html)
            Log.d("find", "selectSemester: $find")
            if (find != null) {
                ids = find.groupValues[1]
            }

            //跳转到选择学期
            runOnUiThread {
                wv.loadUrl(
                    "javascript:var tagId = /<label for=\"(.+)\" class=\"title\">学年学期/.exec(document.getElementsByTagName(\"html\")[0].innerHTML)[1];"
                            + "window.location.href = '/eams/dataQuery.action?dataType=semesterCalendar&tagId='+tagId+'&value=&empty=false'"
                )
            }
        }

        @JavascriptInterface
        fun selectSemester(html: String) {
            //{id:3,schoolYear:"2003-2004",name:"1"}
            //解析学期信息，拿到对应的学期和semesterId
            val findAll =
                Regex("\\{id:(\\d+),schoolYear:\"(\\d+-\\d+)\",name:\"(\\d+)\"\\}").findAll(html)
            val semesterIds = ArrayList<String>()
            val labels = ArrayList<String>()
            for (result in findAll) {
                semesterIds.add(result.groupValues[1])
                labels.add("${result.groupValues[2]}学期${result.groupValues[3]}")
            }
            //让用户对学期进行选择
            MaterialAlertDialogBuilder(this@ImportActivity)
                .setSingleChoiceItems(
                    labels.toTypedArray(),
                    semesterIds.size - 1
                ) { dialog, which ->
                    val semester = semesterIds[which]
                    dialog.dismiss()
                    Log.d("TAG", "selectSemester: $semester")
                    //通过ids和semester两个参数跳转到具有课程信息的页面
                    runOnUiThread {
                        wv.loadUrl("javascript:window.location.href = '/eams/courseTableForStd!courseTable.action?ignoreHead=1&setting.kind=std&ids=$ids&semester.id=$semester'")
                    }
                }.show()
        }
    }

    */

    var name: String = ""
    var teacher: String = ""
    var startNode: Int = -1
    var endNode: Int = 0
    var startWeek: Int = -1
    var endWeek: Int = 0
    var day: Int = 0
    var room: String = ""
    var weekStr = ""
    val courseList = ArrayList<Course>()
    private lateinit var courseAdd: Course

    override fun generateCourseList(): List<Course> {

        val lines = source.split("\n")

        val teacherPattern = Regex("var actTeachers = \\[\\{.+name:\"(.+)\".+\\}\\]")
        val activityPattern =
            Regex("new TaskActivity\\(.+\\),.+\\),.+,\"(.+)\",.*,\"(.*)\",\"([10]+).+\"\\)")
        val nodePattern = Regex("index =(\\d+)\\*unitCount\\+(\\d+);")

        var isFirst = true
        for (line in lines) {
            var find = teacherPattern.find(line)
            if (find != null) {
                if (isFirst) {
                    isFirst = false
                } else {
                    newCourse()
                    startNode = -1
                    endNode = 0
                }

                teacher = find.groupValues[1]
                continue
            }

            find = activityPattern.find(line)
            if (find != null) {
                name = find.groupValues[1].replace(Regex("\\(.+\\)"), "")
                room = find.groupValues[2]
                weekStr = find.groupValues[3]
                continue
            }

            find = nodePattern.find(line)
            if (find != null) {
                if (startNode == -1) {
                    startNode = find.groupValues[2].toInt() + 1
                }
                endNode = find.groupValues[2].toInt() + 1
                day = find.groupValues[1].toInt() + 1
                continue
            }
        }

        newCourse()

        var i = 0
        while (i < courseList.size) {
            var j = i + 1
            var noChange = true
            while (j < courseList.size) {
                if (courseList[i].name == courseList[j].name
                    && courseList[i].room == courseList[j].room
                    && courseList[i].teacher == courseList[j].teacher
                    && courseList[i].day == courseList[j].day
                ) {
                    if (
                        courseList[i].startNode == courseList[j].startNode
                        && courseList[i].endNode == courseList[j].endNode
                    ) {
                        if (courseList[i].endWeek + 1 == courseList[j].startWeek) {
                            courseList[i].endWeek = courseList[j].endWeek
                            courseList.removeAt(j)
                            j--
                            noChange = false
                            continue
                        }
                        if (courseList[i].startWeek - 1 == courseList[j].endWeek) {
                            courseList[i].startWeek = courseList[j].startWeek
                            courseList.removeAt(j)
                            j--
                            noChange = false
                            continue
                        }
                    }

                    if (courseList[i].startWeek == courseList[j].startWeek
                        && courseList[i].endWeek == courseList[j].endWeek
                    ) {
                        if (courseList[i].endNode + 1 == courseList[j].startNode) {
                            courseList[i].endNode = courseList[j].endNode
                            courseList.removeAt(j)
                            j--
                            noChange = false
                            continue
                        }
                        if (courseList[i].startNode - 1 == courseList[j].endNode) {
                            courseList[i].startNode = courseList[j].startNode
                            courseList.removeAt(j)
                            j--
                            noChange = false
                            continue
                        }
                    }

                }

                j++
            }
            if (noChange) {
                i++
            }
        }


        return courseList
    }

    private fun newCourse() {
        var credit = 0f
        val find = Regex("<td>$name</td><td>([\\d\\.]+)</td>").find(source)
        if (find != null) {
            credit = find.groupValues[1].toFloat()
        }
        startWeek = -1

        var i = weekStr.length - 1
        while (i > 0 && weekStr[i] == '0') {
            i--
        }
        endWeek = i
        var type = if (i % 2 == 1) 1 else 2
        var j = i
        while (j > 0 && weekStr[j] == '1' && weekStr[j - 1] == '0') {
            j -= 2
        }
        startWeek = j + 2
        while (j > 0 && weekStr[j] == '0') {
            j--
        }
        if (j <= 0) {
            courseAdd = Course(name, day, room, teacher, startNode, endNode, startWeek, endWeek, type, credit,)
            courseList.add(courseAdd)
            return
        }

        type = 0
        startWeek = -1
        for (index in weekStr.indices) {
            if (weekStr[index] == '1') {
                if (startWeek == -1) {
                    startWeek = index
                    endWeek = index
                } else {
                    endWeek++
                }
            } else {
                if (startWeek != -1) {
                    courseAdd = Course(name, day, room, teacher, startNode, endNode, startWeek, endWeek, type, credit,)
                    courseList.add(courseAdd)
                    startWeek = -1
                }
            }
        }

        if (startWeek != -1) {
            courseAdd = Course(name, day, room, teacher, startNode, endNode, startWeek, endWeek, type, credit,)
            courseList.add(courseAdd)
            startWeek = -1
        }
    }
}
