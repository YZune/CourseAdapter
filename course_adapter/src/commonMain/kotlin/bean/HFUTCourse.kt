package bean

data class HFUTCourse (val result: result)

data class result(val lessonList : List<lessonList>,
                  val scheduleList : List<scheduleList>,
                  val scheduleGroupList: List<scheduleGroupList>)

data class lessonList(val courseName : String,
                      val name : String,
                      val id : String,
                      val suggestScheduleWeekInfo : String,
                      val courseTypeName : String,val remark : String?,val teacherAssignmentList : List<teacherAssignmentList>)

data class scheduleList(val lessonId: Int,
                        val room : room,
                        val weekday : Int,
                        val personName : String,
                        val weekIndex : Int,
                        val startTime : Int,
                        val periods : Int,
                        var endTime : Int,
                        val date : String,
                        var lessonType : String)

data class room(val nameZh : String)

data class scheduleGroupList(val lessonId: Int,
                             val stdCount : Int)

data class teacherAssignmentList(val code : String,
                                 val name : String,
                                 val age : Int?,
                                 val titleName : String?)

