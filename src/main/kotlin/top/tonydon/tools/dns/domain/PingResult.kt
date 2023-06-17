package top.tonydon.tools.dns.domain

data class PingResult(
    var meanDelay: Int = 0,
    var successCount: Int = 0,
    var totalCount: Int = 0
)