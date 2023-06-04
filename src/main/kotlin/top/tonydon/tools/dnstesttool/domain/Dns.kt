package top.tonydon.tools.dnstesttool.domain

data class Dns(
    val id: Int? = null,
    val ip: String? = null,
    val description: String? = null,
    var delay: Int? = null,
    var info: String? = null
)