package main.java.test

import main.java.parser.WISTParser
import bean.Course
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.measureTimeMillis

fun main() {
    // ======================== [ ⚙️配置区 - 调试开关 ] ========================
    val enableCourseDetailLog  = true     // 是否打印每门课的详细信息
    val enableConflictCheck    = true     // 是否检查课程冲突
    val enableWeekSummary      = true     // 是否汇总相同课程的所有周数
    val enableRoomSummary      = false     // 是否统计课程使用的所有教室
    val enableDurationSummary  = false     // 是否统计每门课总课时
    val enableTimeSummary      = true     // 是否统计解析课表的总时间
    // ======================== [ ⚙️CSV配置区 - 调试开关 ] ========================
    val enableCsvExport        = true     // 是否启用 CSV 导出功能
    // =====================================================================

    // ======== 1. 读取 HTML 文件内容 ========
    // 示例中用了相对路径，Windows 下可能需要修改
    // 建议从项目外引用 html 文件
    // 提交时一定不要上传 html 文件，涉及隐私问题
    val htmlFilePath = "D:/Download/Programs/WC23845.html"
    val htmlContent = try {
        File(htmlFilePath).readText()
    } catch (e: IOException) {
        println("❌ 无法读取 HTML 文件: ${e.message}")
        return
    }

    println("🕒 当前时间：${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())}")

    // ======== 2. 初始化解析器并获取课程列表 ========
    lateinit var courseList: List<Course>
    val duration = measureTimeMillis {
        try {
            val parser = WISTParser(htmlContent)
            courseList = parser.generateCourseList()
        } catch (e: Exception) {
            println("❌ 解析器异常：${e.message}")
            return
        }
    }
    if (courseList.isEmpty()) {
        println("📭 未解析出任何课程，请检查 HTML 内容是否正确。")
        return
    }

    if (enableCourseDetailLog) {
        val groupedCourses = courseList.groupBy {
            listOf(it.name, it.teacher, it.room, it.day, it.startNode, it.endNode)
        }

        println("\n📚 课程解析详情\n" + "-".repeat(50))
        groupedCourses.values.forEachIndexed { index, group ->
            val sample = group.first()

            // 解析每个课程的周次
            val weeks = group.flatMap { course ->
                // 提取原始周次数据
                val allWeeks = (course.startWeek..course.endWeek).toSet()

                // 判断是否包含单周/双周标记
                val weekType = course.type // 1为单周，0为不处理单双周

                // 根据类型处理
                val filteredWeeks = when (weekType) {
                    1 -> allWeeks.filter { it % 2 == 1 }  // 单周，保留奇数周
                    0 -> allWeeks // 不做单双周判断，直接保留所有周次
                    else -> allWeeks.filter { it % 2 == 0 }  // 默认处理为双周，保留偶数周
                }

                filteredWeeks
            }.toSet().sorted()

            // 说明课程为单周或双周（不强制，仅提示）
            val weekTypeDesc = when (sample.type) {
                1 -> if (weeks.all { it % 2 == 1 }) "（单周）"
                else if (weeks.all { it % 2 == 0 }) "（双周）" else ""
                else -> ""
            }

            // 打印课程详情
            println("🔹 第 ${index + 1} 门课".padEnd(30, '─'))
            println("📓 课程名称 : ${sample.name}")
            println("🧑🏻‍🏫 教师     : ${sample.teacher}")
            println("📅 周数     : ${weeks.joinToString(", ")} $weekTypeDesc")
            println("⏰ 节次     : 第 ${sample.startNode} - ${sample.endNode} 节")
            println("📆 星期     : 周 ${sample.day}")
            println("📍 地点     : ${sample.room}")
            println("📝 备注     : ${sample.note}")
            println()
        }
        println("📥 共解析出 ${courseList.size} 门课程。")
    }

    // ======== 4. 冲突检测（可配置） ========
    if (enableConflictCheck) {
        println("\n🔍 冲突检测结果\n" + "-".repeat(50))
        val conflicts = mutableListOf<Pair<Course, Course>>()
        for (i in courseList.indices) {
            for (j in i + 1 until courseList.size) {
                val a = courseList[i]
                val b = courseList[j]

                // 核心：比较是否为同一周、同一天、时间重叠
                val sameWeek = a.startWeek == b.startWeek
                val sameDay = a.day == b.day
                val timeOverlap = a.startNode <= b.endNode && b.startNode <= a.endNode

                if (sameWeek && sameDay && timeOverlap) {
                    conflicts += a to b
                }
            }
        }

        if (conflicts.isEmpty()) {
            println("✅ 未发现课程冲突。😄")
        } else {
            println("⚠️ 共发现 ${conflicts.size} 处冲突😔：")
            conflicts.forEachIndexed { idx, (c1, c2) ->
                println("🆘 冲突 ${idx + 1}")
                println("🅰️ ${c1.name} (周${c1.startWeek}-${c1.endWeek}, 周${c1.day}, 节${c1.startNode}-${c1.endNode})")
                println("🅱️ ${c2.name} (周${c2.startWeek}-${c2.endWeek}, 周${c2.day}, 节${c2.startNode}-${c2.endNode})\n")
            }
        }
    }

    // ======== 5. 汇总分析：周数、教室、总课时 ========
    if (enableWeekSummary || enableRoomSummary || enableDurationSummary) {
        println("\n📊 课程汇总分析\n" + "-".repeat(50))

        val weekMap = mutableMapOf<String, MutableSet<Int>>()
        val roomMap = mutableMapOf<String, MutableSet<String>>()
        val hourMap = mutableMapOf<String, Int>()

        courseList.forEach { course ->
            val key = "${course.name}__${course.teacher}"
            val allWeeks = (course.startWeek..course.endWeek).toSet()
            val room = course.room.trim()

            // 判断是否是单双周
            val weekType = course.type // 1为单双周，0为不处理单双周
            val weeks = when (weekType) {
                1 -> allWeeks.filter { it % 2 == 1 }.toSet()  // 单周，保留奇数周
                0 -> allWeeks // 不做单双周处理，保留所有周次
                else -> allWeeks.filter { it % 2 == 0 }.toSet()  // 默认处理为双周，保留偶数周
            }

            // 汇总上课周数
            if (enableWeekSummary) {
                weekMap.getOrPut(key) { mutableSetOf() }.addAll(weeks)
            }

            // 汇总教室信息
            if (enableRoomSummary && room.isNotBlank()) {
                roomMap.getOrPut(key) { mutableSetOf() }.add(room)
            }

            // 汇总课时数（按双节次为 1 节计）
            if (enableDurationSummary) {
                val duration = (course.endNode - course.startNode + 1) / 2
                hourMap[key] = (hourMap[key] ?: 0) + duration * weeks.size
            }
        }

        // 打印结果
        weekMap.keys.union(roomMap.keys).union(hourMap.keys).forEach { key ->
            val (name, teacher) = key.split("__")
            println("📓 课程名 : $name")
            println("🧑🏻‍🏫 教师   : $teacher")
            if (enableWeekSummary) {
                val weeks = weekMap[key]?.toList()?.sorted() ?: emptyList()
                println("🌤️ 上课周 : $weeks")
                println("🔢 周数段 : ${compactWeekList(weeks)}")
            }
            if (enableRoomSummary) {
                val rooms = roomMap[key]?.toList()?.sorted() ?: emptyList()
                println("🏫 教室列表 : ${rooms.joinToString("、")}")
            }
            if (enableDurationSummary) {
                val totalHours = hourMap[key] ?: 0
                println("⏱️ 总课时 : $totalHours 节")
            }
            println()
        }

        if (enableDurationSummary) {
            println("🧮 所有课程合计上课节数：${hourMap.values.sum()} 节")
            println("📌 注：每 2 个节次 node 计为 1 节课（如 node=1,2 为 1 节）\n")
        }
    }

    if (enableTimeSummary) {
        println("⏳ 本次课表汇总解析耗时：${duration}ms\n")
    }
    // CSV导出功能
    if (enableCsvExport) {
        print("📤 是否导出课程表为 CSV 到桌面？（Y/n）：")
        val answer = readLine()?.trim()?.lowercase()
        if (answer.isNullOrEmpty() || answer == "y") {
            try {
                val desktopPath = System.getProperty("user.home") + "/Desktop"
                val resultFolder = File(desktopPath, "解析结果")
                if (!resultFolder.exists()) {
                    resultFolder.mkdirs()
                }

                // 使用大写HH表示24小时制
                val date = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                val fileName = "课表导出_$date.csv"
                val csvFile = File(resultFolder, fileName)

                // 分组并整理为每组一行，包含所有周数
                val grouped = courseList.groupBy {
                    listOf(it.name, it.teacher, it.room, it.day, it.startNode, it.endNode)
                }

                csvFile.printWriter().use { writer ->
                    writer.println("课程名称,星期,开始节数,结束节数,老师,地点,周数")

                    for ((_, group) in grouped) {
                        val sample = group.first()

                        // 根据 course.type 判断单双周
                        val validWeekList = group.flatMap { course ->
                            val allWeeks = (course.startWeek..course.endWeek).toSet()

                            // 判断单双周
                            when (course.type) {
                                1 -> allWeeks.filter { it % 2 == 1 }  // 单周，保留奇数周
                                0 -> allWeeks  // 不做单双周处理，保留所有周次
                                else -> allWeeks.filter { it % 2 == 0 }  // 默认处理为双周，保留偶数周
                            }
                        }.toSet().sorted()

                        // 生成格式化的周次字符串
                        val weekTypeDesc = when (sample.type) {
                            1 -> when {
                                validWeekList.all { it % 2 == 1 } -> "（单周）"
                                validWeekList.all { it % 2 == 0 } -> "（双周）"
                                else -> ""
                            }
                            else -> ""
                        }
                        val weekStr = "${compactWeekList(validWeekList)} $weekTypeDesc".trim()

                        // 关键修复：用双引号包裹可能包含逗号的字段
                        val line = listOf(
                            escapeCsvField(sample.name),
                            sample.day.toString(),
                            sample.startNode.toString(),
                            sample.endNode.toString(),
                            escapeCsvField(sample.teacher),
                            escapeCsvField(sample.room),
                            escapeCsvField(weekStr)
                        ).joinToString(",")

                        writer.println(line)
                    }
                }

                println("✅ CSV 导出成功！文件位置：${csvFile.absolutePath}")
            } catch (e: Exception) {
                println("❌ 导出失败：${e.message}")
            }
        } else {
            println("📁 用户选择取消导出。")
        }
    }
    println("🏁 测试结束:)\n" + "-".repeat(50))
}

/**
 * 工具函数：
 * 将整数周列表如 [1,2,3,5,6,9] 转换为格式化段落字符串 "1-3,5-6,9"
 * 用于更美观地上课周数展示
 */
fun compactWeekList(weeks: List<Int>): String {
    if (weeks.isEmpty()) return ""
    val result = mutableListOf<String>()
    var start = weeks[0]
    var end = weeks[0]

    for (i in 1 until weeks.size) {
        if (weeks[i] == end + 1) {
            end = weeks[i]
        } else {
            result += if (start == end) "$start" else "$start-$end"
            start = weeks[i]
            end = weeks[i]
        }
    }
    result += if (start == end) "$start" else "$start-$end"
    return result.joinToString(",")
}

// 新增：CSV字段转义函数，处理特殊字符
fun escapeCsvField(field: String): String {
    // 如果字段包含逗号、引号或换行符，则用双引号包裹并转义内部引号
    if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
        return "\"${field.replace("\"", "\"\"")}\""
    }
    return field
}
