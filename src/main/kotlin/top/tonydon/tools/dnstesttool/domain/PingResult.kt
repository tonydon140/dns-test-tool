package top.tonydon.tools.dnstesttool.domain

data class PingResult(
    var meanDelay: Int = 0,
    var successCount: Int = 0,
    var totalCount: Int = 0
)