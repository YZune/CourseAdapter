import bean.Course
import bean.CourseBaseBean
import bean.WeekBean
import kotlinx.coroutines.sync.Semaphore
import java.lang.Math.abs
import java.security.MessageDigest

object Common {

    const val TYPE_HELP = "help"
    const val TYPE_ZF = "zf"
    const val TYPE_ZF_1 = "zf_1"
    const val TYPE_ZF_NEW = "zf_new"
    const val TYPE_URP = "urp"
    const val TYPE_UMOOC = "umooc"
    const val TYPE_URP_NEW = "urp_new"
    const val TYPE_URP_NEW_AJAX = "urp_new_ajax"
    const val TYPE_QZ = "qz"
    const val TYPE_QZ_OLD = "qz_old"
    const val TYPE_QZ_OLD_JLICT = "jlict_qz_old"
    const val TYPE_QZ_OLD_CCSU = "ccsu_qz_old" // 长沙学院
    const val TYPE_QZ_CRAZY = "qz_crazy"
    const val TYPE_QZ_BR = "qz_br"
    const val TYPE_QZ_BJFU = "qz_bjfu"
    const val TYPE_QZ_NJUST = "qz_njust"
    const val TYPE_QZ_WITH_NODE = "qz_with_node"
    const val TYPE_QZ_SINGLE_NODE = "qz_single_node"
    const val TYPE_QZ_2017 = "qz_2017" // 华南农业大学
    const val TYPE_CF = "cf"
    const val TYPE_VATUU = "vatuu"
    const val TYPE_PKU = "pku" // 北京大学
    const val TYPE_BNUZ = "bnuz" // 北京师范大学珠海分校
    const val TYPE_HNIU = "hniu" // 湖南信息职业技术学院
    const val TYPE_HNUST = "hnust" // 湖南科技大学
    const val TYPE_JNU = "jnu" // 暨南大学
    const val TYPE_HUNNU = "hunnu" // 湖南师范大学
    const val TYPE_ECJTU = "ecjtu" // 华东交通大学
    const val TYPE_SHU = "shu"// 上海大学
    const val TYPE_SIT = "sit"// 上海应用技术大学
    const val TYPE_XATU = "xatu_shuwei" // 西安工业大学
    const val TYPE_XSYU = "xsyu_shuwei" // 西安石油大学
    const val TYPE_UESTC = "uestc_shuwei" // 电子科技大学
    const val TYPE_SIAS = "sias_shuwei" // 郑州西亚斯学院
    const val TYPE_SNUT = "snut_shuwei" // 陕西理工大学
    const val TYPE_AHNU = "ahnu" // 安徽师范大学
    const val TYPE_SCAU = "scau" // 四川农业大学
    const val TYPE_SDU = "sdu" // 山东大学
    const val TYPE_JZ = "jz" // 金智教务
    const val TYPE_JZ_1 = "jz_1"
    const val TYPE_HAUST = "haust" // 河南科技大学
    const val TYPE_HIT = "hit" // 哈尔滨工业大学
    const val TYPE_SYSU = "sysu" // 中山大学
    const val TYPE_LOGIN = "login" // 模拟登录方式
    const val TYPE_MAINTAIN = "maintain" // 维护状态，暂不可用
    const val TYPE_CHANGZHOU = "changzhou"
    const val TYPE_SICNU = "sicnu" // 四川师范大学
    const val TYPE_WHUT = "whut" // 武汉理工大学
    const val TYPE_HFU = "hfu" // 合肥工业大学
    const val TYPE_CUMTB = "cumtb" // 中国矿业大学（北京）
    const val TYPE_TJU = "tju" // 同济大学
    const val TYPE_GZHUYJS = "gzhuyjs" // 广州大学研究生
    const val TYPE_XYTC = "xytc" // 襄阳职业技术学院
    const val TYPE_FDU = "fdu" // 复旦大学
    const val TYPE_CQUPT = "cqupt" // 重庆邮电大学
    const val TYPE_FSTVC = "fstvc" // 福州软件职业技术学院

    val nodePattern = Regex("""\(\d{1,2}[-]*\d*节""")
    val nodePattern1 = Regex("""\d{1,2}[~]*\d*节""")
    val nodePattern2 = Regex("""(^\d.*)节""")
    val singleNodePattern = Regex("""第(\d+)节""")

    val weekPattern = Regex("""\{第\d{1,2}[-]*\d*周""")
    val weekPattern1 = Regex("""\d{1,2}[-]*\d*""")
    val weekPattern2 = Regex("""\d{1,2}周""")
    val typedWeekPattern = Regex("""第(\d+)-(\d+)[单|双]?周""")


    val chineseWeekList = arrayOf("", "周一", "周二", "周三", "周四", "周五", "周六", "周日")
    val englishAbbrWeekList = arrayOf("", "mon", "tue", "wed", "thu", "fri", "sat", "sun")
    val otherHeader = arrayOf(
        "时间",
        "星期一",
        "星期二",
        "星期三",
        "星期四",
        "星期五",
        "星期六",
        "星期日",
        "早晨",
        "上午",
        "下午",
        "晚上"
    )
    val courseProperty = arrayOf(
        "任选",
        "限选",
        "实践选修",
        "必修课",
        "选修课",
        "必修",
        "选修",
        "专基",
        "专选",
        "公必",
        "公选",
        "义修",
        "选",
        "必",
        "主干",
        "专限",
        "公基",
        "值班",
        "通选",
        "思政必",
        "思政选",
        "自基必",
        "自基选",
        "语技必",
        "语技选",
        "体育必",
        "体育选",
        "专业基础课",
        "双创必",
        "双创选",
        "新生必",
        "新生选",
        "学科必修",
        "学科选修",
        "通识必修",
        "通识选修",
        "公共基础",
        "第二课堂",
        "学科实践",
        "专业实践",
        "专业必修",
        "辅修",
        "专业选修",
        "外语",
        "方向",
        "专业必修课",
        "全选"
    )

    private val headerNodePattern = Regex("""第.*节""")

    private fun toHex(byteArray: ByteArray): String {
        // 转成16进制后是32字节
        return with(StringBuilder()) {
            byteArray.forEach {
                val hex = it.toInt() and (0xFF)
                val hexStr = Integer.toHexString(hex)
                if (hexStr.length == 1) {
                    append("0").append(hexStr)
                } else {
                    append(hexStr)
                }
            }
            toString()
        }
    }

    fun sha1(str: String): String {
        val digest = MessageDigest.getInstance("SHA-1")
        val result = digest.digest(str.toByteArray())
        return toHex(result)
    }

    fun weekIntList2WeekBeanList(input: MutableList<Int>): MutableList<WeekBean> {
        var reset = 0
        var temp = WeekBean(0, 0, -1)
        val list = arrayListOf<WeekBean>()
        for (i in input.indices) {
            if (reset == 1) {
                list.add(temp)
                temp = WeekBean(0, 0, -1)
                reset = 0
            }
            if (i < input.size - 1) {
                if (temp.type == 0 && input[i + 1] - input[i] == 1) temp.end = input[i + 1]
                else if ((temp.type == 1 || temp.type == 2) && input[i + 1] - input[i] == 2)
                    temp.end = input[i + 1]
                else if (temp.type != -1) {
                    reset = 1
                }
            }
            if (i < input.size - 1 && temp.type == -1) {
                temp.start = input[i]
                when (input[i + 1] - input[i]) {
                    1 -> {
                        temp.type = 0
                        temp.end = input[i + 1]
                    }

                    2 -> {
                        temp.type = if (input[i] % 2 != 0) 1 else 2
                        temp.end = input[i + 1]
                    }

                    else -> {
                        temp.end = input[i]
                        temp.type = 0
                        reset = 1
                    }
                }
            }
            if (i == input.size - 1 && temp.type != -1) list.add(temp)
            if (i == input.size - 1 && temp.type == -1) {
                temp.start = input[i]
                temp.end = input[i]
                temp.type = 0
                list.add(temp)
            }
        }
        return list
    }

    fun findExistedCourseId(list: List<CourseBaseBean>, name: String): Int {
        val result = list.findLast {
            it.courseName == name
        }
        return result?.id ?: -1
    }

    fun parseHeaderNodeString(str: String): Int {
        var node = -1
        if (headerNodePattern.matches(str)) {
            val nodeStr = str.substring(1, str.length - 1)
            node = try {
                nodeStr.toInt()
            } catch (e: Exception) {
                getNodeInt(nodeStr)
            }
        }
        return node
    }

    fun getWeekFromChinese(chineseWeek: String): Int {
        for (i in chineseWeekList.indices) {
            if (chineseWeekList[i] == chineseWeek) {
                return i
            }
        }
        return 0
    }

    fun countStr(str1: String, str2: String): Int {
        var times = 0
        var startIndex = 0
        var findIndex = str1.indexOf(str2, startIndex)
        while (findIndex != -1 && findIndex != str1.length - 1) {
            times += 1
            startIndex = findIndex + 1
            findIndex = str1.indexOf(str2, startIndex)
        }
        if (findIndex == str1.length - 1) {
            times += 1
        }
        return times
    }

    fun getNodeStr(node: Int): String {
        return when (node) {
            1 -> "一"
            2 -> "二"
            3 -> "三"
            4 -> "四"
            5 -> "五"
            6 -> "六"
            7 -> "七"
            8 -> "八"
            9 -> "九"
            10 -> "十"
            11 -> "十一"
            12 -> "十二"
            13 -> "十三"
            14 -> "十四"
            15 -> "十五"
            16 -> "十六"
            else -> ""
        }
    }

    fun getNodeInt(nodeStr: String): Int {
        return when (nodeStr) {
            "一" -> 1
            "二" -> 2
            "三" -> 3
            "四" -> 4
            "五" -> 5
            "六" -> 6
            "七" -> 7
            "日" -> 7
            "八" -> 8
            "九" -> 9
            "十" -> 10
            "十一" -> 11
            "十二" -> 12
            "十三" -> 13
            "十四" -> 14
            "十五" -> 15
            "十六" -> 16
            "十七" -> 17
            "十八" -> 18
            "十九" -> 19
            "二十" -> 20
            else -> -1
        }
    }

    fun containNodeInt(nodeStr: String): Int {
        if (nodeStr.contains('一'))
            return 1
        if (nodeStr.contains('二'))
            return 2
        if (nodeStr.contains('三'))
            return 3
        if (nodeStr.contains('四'))
            return 4
        if (nodeStr.contains('五'))
            return 5
        if (nodeStr.contains('六'))
            return 6
        if (nodeStr.contains('七'))
            return 7
        if (nodeStr.contains('八'))
            return 8
        if (nodeStr.contains('九'))
            return 9
        if (nodeStr.contains('十'))
            return 10
        if (nodeStr.contains("十一"))
            return 11
        if (nodeStr.contains("十二"))
            return 12
        if (nodeStr.contains("十三"))
            return 13
        if (nodeStr.contains("十四"))
            return 14
        return -1
    }

    fun judgeContinuousCourse(pre: Course, current: Course): Boolean {
        return pre.name == current.name
                && pre.day == current.day
                && pre.room == current.room
                && pre.teacher == current.teacher
                && pre.startWeek == current.startWeek
                && pre.endWeek == current.endWeek
                && pre.type == current.type
                && pre.endNode == current.startNode - 1
    }

    fun judgeWeekCourse(pre: Course, current: Course): Boolean {
        return pre.name == current.name
                && pre.day == current.day
                && pre.room == current.room
                && pre.teacher == current.teacher
                && pre.startNode == current.startNode
                && pre.endNode == current.endNode
    }

    fun mergeWeekCourse(tmpList: ArrayList<Course>, courseList: ArrayList<Course>) {
        val len = tmpList.size
        val weekList = mutableListOf<Int>()
        tmpList.sortedWith(
            compareBy(
                { it.day },
                { it.name },
                { it.startTime },
                { it.teacher },
                { it.startWeek })
        ).forEachIndexed { i, course ->
            if (courseList.isEmpty()) {
                courseList.add(course)
                weekList.add(course.startWeek)
            } else {
                val pre = courseList.last()
                if (Common.judgeWeekCourse(pre, course) && i != len - 1) {
                    weekList.add(course.startWeek)
                } else {
                    if (Common.judgeWeekCourse(pre, course) && i == len - 1) {
                        weekList.add(course.startWeek)
                    }
                    Common.weekIntList2WeekBeanList(weekList).forEachIndexed { index, weekBean ->
                        if (index == 0) {
                            pre.startWeek = weekBean.start
                            pre.endWeek = weekBean.end
                            pre.type = weekBean.type
                        } else {
                            courseList.add(
                                pre.copy(
                                    startWeek = weekBean.start,
                                    endWeek = weekBean.end,
                                    type = weekBean.type
                                )
                            )
                        }
                    }
                    if (i != len - 1) {
                        weekList.clear()
                        weekList.add(course.startWeek)
                        courseList.add(course)
                    }
                    if (!Common.judgeWeekCourse(pre, course) && i == len - 1) {
                        courseList.add(course)
                    }
                }
            }
        }
    }


    /**
     * 星期字符串转 Int
     * @author [Xeu](https://github.com/ThankRain)
     * @return Int
     */
    fun getDayInt(day: String): Int {
        return getNodeInt(day.replace("星期", ""))
    }

    /*时间及其比较、距离*/
    class TimeHM(str: String) {
        var hour: Int = 0
        var minute: Int = 0

        init {
            val arr = str.split(":")
            hour = arr[0].toInt()
            minute = arr[1].toInt()
        }

        operator fun compareTo(other: TimeHM): Int {
            if (this.hour == other.hour) {
                if (this.minute == other.minute) return 0
                if (this.minute > other.minute) return 1
                return -1
            }
            if (this.hour > other.hour) return 1
            return 0
        }

        operator fun plus(m: Int): TimeHM {
            var r = this
            r.minute += m; r.hour += r.minute / 60; r.minute %= 60;
            if (r.minute < 0) {
                r.minute += 60; r.hour -= 1; }
            return r
        }

        operator fun minus(m: Int): TimeHM = this + (-m)

        fun duration(other: TimeHM): Int {
            val h: Int = this.hour - other.hour
            val m: Int = this.minute - other.minute
            return abs(60 * h + m)
        }

        fun duration(other: String): Int {
            val o = TimeHM(other)
            return duration(o)
        }

        fun timeCmp(other: String): Int {
            val o = TimeHM(other)
            return compareTo(o)
        }

        override fun toString(): String {
            fun nToString(i: Int): String = "${if (i < 0) "-" else ""}${if (abs(i) < 10) "0" else ""}${abs(i)}"
            return "${nToString(hour)}:${nToString(minute)}"
        }
    }

    suspend fun <T> Semaphore.acquireInBlock(block: suspend () -> T): T {
        acquire()
        return try {
            block()
        } finally {
            release()
        }
    }
}
