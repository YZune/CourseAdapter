package bean

import kotlinx.serialization.Serializable
import parser.THUParser

@Serializable
class THUSemesterData(
        val weekCount: Int?,
        private val reschedule: Array<Array<Int>>?,
) {
    val parsedReschedule: Array<THUParser.Reschedule>?
        get() = reschedule?.run { Array(size) { i -> this[i].toReschedule() } }

    private fun Array<Int>.toReschedule() = when (size) {
        2 -> THUParser.Reschedule(this[0], this[1])
        else -> THUParser.Reschedule(this[0], this[1], this[2], this[3])
    }
}