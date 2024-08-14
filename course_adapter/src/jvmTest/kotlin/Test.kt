import kotlinx.coroutines.runBlocking
import parser.THUParser
import java.io.File
import kotlin.test.Test

// 请不要提交对此文件的修改
class JvmTest {

    @Test
    fun test() {
        runBlocking {
            THUParser(File("文件路径").readText()).saveCourse()
        }
    }
}