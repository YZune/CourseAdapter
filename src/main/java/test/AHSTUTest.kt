import java.io.File
import java.util.Scanner
import main.java.parser.AHSTUCourseProvider
import main.java.parser.supwisdom.SupwisdomParser

//获取课表html
fun getCourseHTML() {
    val xuke = AHSTUCourseProvider()
    val img = xuke.getCaptchaImage()
    val _file = File("captcha.png")
    _file.writeBytes(img)

    println("获取验证码成功")
    println("输入验证码:")
    val sc = Scanner(System.`in`)
    val cap = sc.next()
    xuke.login("", "", cap)
    println("登录成功")
    val importOPT = xuke.getImportOption()

    val html = xuke.getCourseHtml(AHSTUCourseProvider.Semester(102, "", ""))
    val c = File("course.html")
    c.writeText(html)
    println("完成")
}


fun main() {
    val file = File("course.html").readText()
    SupwisdomParser(file).generateCourseList()
}