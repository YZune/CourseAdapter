package parser

import Common
import bean.Course
import org.jsoup.Jsoup

//https://jw.sdpei.edu.cn/

class SDPEIParser(source: String) : Parser(source) {
    override fun generateCourseList(): List<Course> {
        val courseList = arrayListOf<Course>()
        val doc = Jsoup.parse(source)
        val kbtable = doc.select("table[id=tableMain]").first()
        if (kbtable == null) {
            println("错误：找不到课程表，尝试查找其他表格...")
            val allTables = doc.select("table")
            println("找到 ${allTables.size} 个表格")
            allTables.forEachIndexed { index, table ->
                println("表格 $index: id=${table.id()}, class=${table.className()}")
            }
            return courseList
        }
        val kcb = kbtable.getElementsByTag("tbody").first()
        
        // 遍历表格行，跳过表头
        var actualRowIndex = 0 // 实际的课程行计数器
        for ((rowIndex, tr) in kcb.getElementsByTag("tr").withIndex()) {
            // 跳过表头行（包含th标签的行）
            if (tr.select("th.thNewTitle").isNotEmpty()) continue
            // 跳过休息时间行
            if (tr.select("th.thRest").isNotEmpty()) continue
            
            // 遍历表格列
            val td = tr.getElementsByTag("td")
            for ((colIndex, cell) in td.withIndex()) {
                // td已经不包含第一列的时间列了，所以colIndex直接对应星期
                val dayOfWeek = colIndex + 1 // 1=星期一, 2=星期二, ...
                
                // 解析每个单元格中的课程
                val courses = parseCourseFromCell(cell, actualRowIndex, dayOfWeek)
                courseList.addAll(courses)
            }
            actualRowIndex++ // 只有有效的课程行才增加计数器
        }
        return courseList
    }
    
    private fun parseCourseFromCell(cell: org.jsoup.nodes.Element, row: Int, col: Int): List<Course> {
        val courses = mutableListOf<Course>()
        
        // 查找所有课程div
        val courseDivs = cell.select("div.divOneClass")
        
        for (courseDiv in courseDivs) {
            try {
                val course = extractCourseInfo(courseDiv, row, col)
                if (course != null) {
                    courses.add(course)
                }
            } catch (e: Exception) {
                println("解析课程信息失败: ${e.message}")
            }
        }
        
        return courses
    }
    
    private fun extractCourseInfo(courseDiv: org.jsoup.nodes.Element, row: Int, col: Int): Course? {
        var courseName = ""
        var teacher = ""
        var room = ""
        var weekInfo = ""
        var building = ""
        
        // 使用CSS选择器精确提取信息
        courseDiv.select("span.spLUName").firstOrNull()?.let {
            courseName = it.text().removeSurrounding("《", "》")
        }
        
        courseDiv.select("span.spTeacherName").firstOrNull()?.let {
            teacher = it.text()
        }
        
        courseDiv.select("span.spWeekInfo").firstOrNull()?.let {
            weekInfo = it.text()
        }
        
        courseDiv.select("span.spBuilding").firstOrNull()?.let {
            building = it.text()
        }
        
        courseDiv.select("span.spClassroom").firstOrNull()?.let {
            room = it.text()
        }
        
        // 如果没有找到classroom，尝试组合building
        if (room.isEmpty() && building.isNotEmpty()) {
            room = building
        } else if (room.isNotEmpty() && building.isNotEmpty()) {
            room = "${building}-${room}"
        }
        
        // 解析周次信息
        val (startWeek, endWeek, type) = parseWeekInfo(weekInfo)
        
        // 根据行列位置计算星期和节次
        val day = col // 列对应星期（1-7）
        val (startNode, endNode) = calculateTimeSlot(row)
        
        // 获取rowspan来确定课程跨度
        val parentTd = courseDiv.parent()
        val rowspan = parentTd?.attr("rowspan")?.toIntOrNull() ?: 1
        val actualEndNode = startNode + rowspan - 1
        
        if (courseName.isNotEmpty()) {
            return Course(
                name = courseName,
                day = day,
                room = room,
                teacher = teacher,
                startNode = startNode,
                endNode = actualEndNode,
                startWeek = startWeek,
                endWeek = endWeek,
                type = type
            )
        }
        
        return null
    }
    
    private fun parseWeekInfo(weekInfo: String): Triple<Int, Int, Int> {
        var startWeek = 1
        var endWeek = 16
        var type = 0 // 0=每周, 1=单周, 2=双周
        
        if (weekInfo.isNotEmpty()) {
            // 解析单双周
            type = when {
                weekInfo.contains("单周") -> 1
                weekInfo.contains("双周") -> 2
                else -> 0
            }
            
            // 解析周次范围 "1-16周"
            val weekPattern = Regex("""(\d+)-(\d+)周""")
            weekPattern.find(weekInfo)?.let { match ->
                startWeek = match.groupValues[1].toIntOrNull() ?: 1
                endWeek = match.groupValues[2].toIntOrNull() ?: 16
            }
        }
        
        return Triple(startWeek, endWeek, type)
    }
    
    private fun calculateTimeSlot(row: Int): Pair<Int, Int> {
        // 根据表格行位置计算对应的节次
        // 从HTML可以看到：①②③④ ⑤⑥⑦⑧ ⑨⑩⑪⑫
        // 实际行索引映射到节次（从1开始）
        val startNode = row + 1
        
        // 这里返回单节，实际的endNode会在extractCourseInfo中根据rowspan计算
        return Pair(startNode, startNode)
    }
}