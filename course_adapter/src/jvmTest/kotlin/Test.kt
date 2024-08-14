import parser.THUParser
import java.io.File

// 请不要提交对此文件的修改
suspend fun main() {
    THUParser(File("文件路径").readText()).saveCourse()
}