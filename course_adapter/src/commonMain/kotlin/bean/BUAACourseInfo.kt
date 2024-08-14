package bean

data class BUAACourseInfo(
    val code: Int,
    val msg: String,
    val datas: Datas
) {
    data class Datas(
        val arrangedList: List<CourseItem>,
        val notArrangeList: List<CourseItem>,
        val practiceList: List<CourseItem>,
        val code: String,
        val name: String
    ) {
        data class CourseItem(
            val week: String = "",
            val courseCode: String = "",
            val credit: String = "",
            val courseName: String = "",
            val byCode: String = "",
            val beginSection: Int = 0,
            val endSection: Int = 0,
            val titleDetail: List<String> = listOf(),
            val multiCourse: String = "",
            val teachClassName: String = "",
            val placeName: String = "",
            val teachingTarget: String = "",
            val weeksAndTeachers: String = "",
            val teachClassId: String = "",
            val cellDetail: List<CellDetail> = listOf(),
            val tags: List<String> = listOf(),
            val courseSerialNo: String = "",
            val beginTime: String = "",
            val endTime: String = "",
            val color: String = "",
            val dayOfWeek: Int = 0
        ) {
            data class CellDetail(
                val color: String = "",
                val text: String = ""
            )
        }
    }
}
