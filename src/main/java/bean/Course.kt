package bean

data class Course(
    val name: String,
    val day: Int, //1 - 7
    val room: String = "",
    val teacher: String = "",
    var startNode: Int,
    var endNode: Int,
    val startWeek: Int,
    val endWeek: Int,
    var type: Int
)