package bean

data class Course(
    val name: String,            //课程名
    val day: Int,                //该课程的是星期几（7代表星期天）参数范围：1 - 7
    val room: String = "",       //教室
    val teacher: String = "",    //老师
    var startNode: Int,          //开始为第几节课
    var endNode: Int,            //结束时为第几节课
    val startWeek: Int,          //开始周
    val endWeek: Int,            //结束周
    var type: Int                //单双周，每周为0，单周为1，双周为2
)