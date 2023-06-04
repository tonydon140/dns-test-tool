package top.tonydon.tools.dnstesttool.domain

import java.time.LocalDateTime

data class Version(
    var id: Long = -1,
    var projectId: Long = -1,
    var projectName: String = "",
    var version: String = "",
    var versionNumber: Int = 0,
    var description: String = ""
)