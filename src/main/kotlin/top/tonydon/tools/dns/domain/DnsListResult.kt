package top.tonydon.tools.dns.domain

data class DnsListResult(
    var code: Int? = null,
    var msg: String? = null,
    var data: List<Dns>? = null
)