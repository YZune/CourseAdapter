package main.java.test

import com.google.gson.Gson
import main.java.bean.CourseForCCIBE
import main.java.bean.Response
import main.java.bean.XQForCCIBE
import main.java.parser.CCIBEParser
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

fun main() {
    val path = "C:\\Users\\Administrator\\Desktop\\kb"
    val courseList = ArrayList<CourseForCCIBE>()
    val file = File(path)
    val fs = file.listFiles()
    val gson = Gson()
    fs?.forEach { its ->
        if (its.name == "xq.json"){
//            获取当前学期
            val content = its.readText()
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val nowDate = current.format(formatter)
            var xqId = ""
            gson.fromJson(content, Array<XQForCCIBE>::class.java).forEach {
                if(it.startTime < nowDate && it.endTime > nowDate){
                    xqId = it.id
                }
            }
            //获取学期
        }else{
            val content = its.readText()
            if(content.isNotEmpty()){
                val jsonObj = gson.fromJson(content, Response::class.java)
                jsonObj.data.wdkb.forEach() {
                    it.qsz = Integer.parseInt(its.nameWithoutExtension)
                    it.jsz = it.qsz
                    courseList.add(it)
                }
            }
        }
    }
    val parser = CCIBEParser(gson.toJson(courseList))
    parser.saveCourse()
}