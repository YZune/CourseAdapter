package test

import main.java.parser.FSTVCParser
import java.io.FileReader

fun main() {
    val p = FSTVCParser(FileReader("./private/fstvc.txt").readText().trim())
    p.saveCourse(true)
}
