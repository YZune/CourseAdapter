package bean

data class CourseBaseBean(
    var id: Int,
    var courseName: String,
    var color: String,
    var tableId: Int,
    var note: String,
    var credit: Float = 0f
)