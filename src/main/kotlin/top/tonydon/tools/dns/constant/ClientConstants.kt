package top.tonydon.tools.dns.constant

object ClientConstants {
    const val DNS_API = "https://tonydon.top:6515/admin/dns/list"
    const val ABOUT_URL = "https://www.tonydon.top/article/9"
    const val LATEST_VERSION_API = "https://tonydon.top:6515/admin/project/version/latest/2"
    const val VERSION = "v0.3.0"
    const val TITLE = "DNS测试工具 $VERSION"
    const val VERSION_NUMBER = 5
    const val LATEST_URL = "https://github.com/tonydon140/dns-test-tool/releases"
    val CPU_CORE_COUNT = Runtime.getRuntime().availableProcessors()
}
