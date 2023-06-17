package top.tonydon.tools.dns.domain

data class Version(
    var id: Long = -1,
    var projectId: Long = -1,
    var projectName: String = "",
    var version: String = "",
    var versionNumber: Int = 0,
    var description: String = ""
)