package top.tonydon.tools.dnstesttool.domain

data class DnsListResult(
    var code: Int? = null,
    var msg: String? = null,
    var data: List<Dns>? = null
)