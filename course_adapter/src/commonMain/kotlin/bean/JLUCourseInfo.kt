package bean

data class JLUCourseInfo(
    val code: String,
    val datas: Datas
) {
    data class Datas(
        val xsjxrwcx: Xsjxrwcx
    ) {
        data class Xsjxrwcx(
            val extParams: ExtParams,
            val pageNumber: Int,
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
                val DZ_SCU_EWMFJ: Any,
                val JXERM: Any,
                val KBBZ: Any,
                val KCDM: String,
                val KCFLDM: String,
                val KCMC: String,
                val KCMCYW: String,
                val KCXZDM: String,
                val KKDW: String,
                val KKDW_DISPLAY: String,
                val KSDDMS: Any,
                val KSSJMS: Any,
                val ORDERFILTER: Any,
                val PKDD: String,
                val PKSJ: String?,
                val PKSJDD: String?,
                val RKJS: String,
                val RZLBDM: Any,
                val SCSKRQ: String,
                val SKFSDM: String,
                val SKFSDM_DISPLAY: String,
                val SKXS: String,
                val SKXS_DISPLAY: String,
                val WID: String,
                val XDFSDM: String,
                val XF: Double,
                val XH: String,
                val XKBZ: String,
                val XKRS: String,
                val XNXQDM: String,
                val XNXQDM_DISPLAY: String,
                val XNXQYWMC: Any,
                val XQDM: String,
                val XQDM_DISPLAY: String,
                val XSJXFSBZ: Any,
                val XSJXFSDM: Any,
                val XSJXFSDM_DISPLAY: String,
                val YXYWMC: String,
                val ZXS: Double
            )
        }
    }
}