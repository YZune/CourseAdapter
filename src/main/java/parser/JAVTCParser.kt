package main.java.parser

import bean.Course
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import parser.Parser

class JAVTCParser() : Parser("") {
    override fun generateCourseList(): List<Course> {
        val courses = mutableListOf<Course>()
        val doc = Jsoup.connect("http://223.84.63.131:99/jiaowu/JWXS/pkgl/XsKB_List.aspx").get()
        val table = doc.select("table[border=1][width=98%]").first() ?: return courses

        // Iterate through each row (skip header row)
        for (row in table.select("tr:gt(0)")) {
            val cells = row.select("td")
            if (cells.size < 8) continue // Skip rows that don't have enough cells

            // Get the time slot (first cell)
            val timeSlotText = cells[0].text().trim()
            val (startNode, endNode) = when {
                timeSlotText.contains("第一二节") -> Pair(1, 2)
                timeSlotText.contains("第三四节") -> Pair(3, 4)
                timeSlotText.contains("第五六节") -> Pair(5, 6)
                timeSlotText.contains("第七八节") -> Pair(7, 8)
                else -> continue // Skip non-course rows
            }

            // Process each day's cell (columns 1-7 represent Monday to Sunday)
            for (day in 1..7) {
                val cell = cells[day]

                // Split courses by <br> tags or other separators
                val courseBlocks = cell.html().split("<br>------------<br>", "<br>")
                    .map { it.trim() }
                    .filter { it.isNotBlank() && it.contains("<a") }

                for (block in courseBlocks) {
                    val link = Jsoup.parse(block).select("a").first() ?: continue
                    val title = link.attr("title")
                    if (title.isBlank()) continue

                    // Parse course info from title attribute
                    val courseInfo = parseCourseInfo(title)
                    if (courseInfo != null) {
                        val (name, teacher, weeks, room) = courseInfo
                        val weekRanges = parseWeeks(weeks)

                        for ((startWeek, endWeek, type) in weekRanges) {
                            courses.add(
                                Course(
                                    name = name,
                                    day = day,
                                    room = room,
                                    teacher = teacher,
                                    startNode = startNode,
                                    endNode = endNode,
                                    startWeek = startWeek,
                                    endWeek = endWeek,
                                    type = type,
                                    startTime = "", // You'll need to add these based on your school's schedule
                                    endTime = ""
                                )
                            )
                        }
                    }
                }
            }
        }

        return courses
    }

    private fun parseCourseInfo(title: String): Quadruple<String, String, String, String>? {
        val lines = title.split("\n").map { it.trim() }
        if (lines.size < 8) return null

        val name = lines[2].substringAfter("课程名称：")
        val teacher = lines[3].substringAfter("授课教师：")
        val weeks = lines[5].substringAfter("上课周次：")
        val room = lines[6].substringAfter("开课地点：")

        return Quadruple(name, teacher, weeks, room)
    }

    private fun parseWeeks(weeksStr: String): List<Triple<Int, Int, Int>> {
        val weekList = mutableListOf<Triple<Int, Int, Int>>()

        // Split by commas to handle multiple week ranges
        val parts = weeksStr.split(",").map { it.trim() }

        for (part in parts) {
            when {
                // Handle single week like "12"
                part.toIntOrNull() != null -> {
                    val week = part.toInt()
                    weekList.add(Triple(week, week, 0))
                }
                // Handle range like "1-6" or "8-10"
                part.contains("-") -> {
                    val range = part.split("-")
                    if (range.size == 2 && range[0].toIntOrNull() != null && range[1].toIntOrNull() != null) {
                        val start = range[0].toInt()
                        val end = range[1].toInt()
                        weekList.add(Triple(start, end, 0))
                    }
                }
                // Handle special cases like "单周" (odd weeks) or "双周" (even weeks)
                part.contains("单周") || part.contains("双周") -> {
                    val weekType = if (part.contains("单周")) 1 else 2
                    val weekNum = part.replace("单周", "").replace("双周", "").trim()

                    if (weekNum.isEmpty()) continue

                    if (weekNum.toIntOrNull() != null) {
                        val week = weekNum.toInt()
                        weekList.add(Triple(week, week, weekType))
                    } else if (weekNum.contains("-")) {
                        val range = weekNum.split("-")
                        if (range.size == 2 && range[0].toIntOrNull() != null && range[1].toIntOrNull() != null) {
                            val start = range[0].toInt()
                            val end = range[1].toInt()
                            weekList.add(Triple(start, end, weekType))
                        }
                    }
                }
            }
        }

        return weekList
    }
}

data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)