package parser.qz

class QzCrazyParser(source: String) : QzParser(source) {
    override val webTableName: String
        get() = "kbcontent1"
}