package bean

data class TimeDetail(
    val node: Int,
    val startTime: String, // 时间字符串，格式必须为"HH:mm"，24小时制，如"01:30"
    val endTime: String
)