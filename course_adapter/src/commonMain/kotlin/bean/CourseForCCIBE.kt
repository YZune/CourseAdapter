package bean

/**

 * 重庆对外经贸 课程Bean

 * @name CourseForCCIBE

 * @author eucaly

 * @date 2022-06-08 15:51

 */
data class CourseForCCIBE(
    var xh: String,  //学号
    var xm: String,     //姓名
    var xq: String,     //学期
    var xn: String,     //学年
    var jxdd: String,   //教学地点
    var sksj: String,   //上课时间
    var jsgh: String,   //教师工号
    var kcksdwmc: String,   // 未知
    var xqmc: String,   //校区名称
    var jc: String,  //节次
    var kcmc: String,   //课程名称
    var jsxm: String,   //教师姓名
    var xqj: String,    //星期几
    var qsz: Int,    //起始周
    var jsz: Int,    //结束周
    var ksj: String,    //开始节
    var jcd: String    //未知
)

data class Calendar(
    var id: String,
    var startYear: String,
    var endYear: String,
    var startTime: String,
    var endTime: String,
    var trem: String,
    var title: String,
    var allWeek: String,
    var nowWeek: String
)

data class ResponseData(
    var calendar: Calendar,
    var wdkb: List<CourseForCCIBE>,
    var time: List<String>
)

data class Response(
    var code: String,
    var msg: String,
    var data: ResponseData,
    var success: Boolean,
    var message: String
)
