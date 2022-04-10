package main.java.test

import main.java.parser.NWPUParser

fun main() {
    //NWPUParser 里面有维护小提示，希望可以帮助到你。
    //秋春夏对应0、1、2
    val parser = NWPUParser("2019000000", "password", "2021", 1)
    parser.saveCourse(true)
}
