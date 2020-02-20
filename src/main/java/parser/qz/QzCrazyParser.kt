package parser.qz

class QzCrazyParser(source: String) : QzParser(source) {
    override val tableName: String
        get() = "kbcontent1"
}