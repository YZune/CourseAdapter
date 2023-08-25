package main.java.bean

data class JLUCourseInfo(
    val code: String,
    val datas: Datas
) {
    data class Datas(
        val xspkjgcx: Xspkjgcx
    ) {
        data class Xspkjgcx(
            val extParams: ExtParams,
            val pageSize: Int,
            val rows: List<Row>,
            val totalSize: Int
        ) {
            data class ExtParams(
                val code: Int,
                val logId: String,
                val totalPage: Int
            )

            data class Row(
                val BJDM: String,
                val BJMC: String,
                val BY1: Any,
                val BY10: Any,
                val BY2: Any,
                val BY3: Any,
                val BY4: Any,
                val BY5: Any,
                val BY6: Any,
                val BY7: Any,
                val BY8: Any,
                val BY9: Any,
                val BZ: Any,
                val CZR: String,
                val CZSJ: String,
                val JASDM: String,
                val JASMC: String,
                val JCFADM: String,
                val JSJCDM: Int,
                val JSSJ: Int,
                val JSXM: String,
                val KBBZ: Any,
                val KCDM: String,
                val KCMC: String,
                val KSJCDM: Int,
                val KSSJ: Int,
                val ORDERFILTER: Any,
                val QZAPYY: Any,
                val RZLBDM: Any,
                val SFQZAP: Any,
                val SKFSDM: String,
                val SKFSDM_DISPLAY: String,
                val WID: String,
                val XH: Any,
                val XM: Any,
                val XNXQDM: String,
                val XQ: Int,
                val XS: Int,
                val ZCBH: String,
                val ZCMC: String
            )
        }
    }
}