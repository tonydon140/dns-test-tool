package top.tonydon.tools.dns.util

import top.tonydon.tools.dns.constant.OSEnum

object OSUtils {
    fun getSystem(): OSEnum {
        val osName = System.getProperty("os.name")

        return if (osName.contains("Windows", true)) {
            OSEnum.Windows
        } else if (osName.contains("Linux", true)) {
            OSEnum.Linux
        } else if (osName.contains("Mac", true)) {
            OSEnum.MacOS
        } else {
            OSEnum.Other
        }

    }
}
