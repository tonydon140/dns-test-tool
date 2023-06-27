package top.tonydon.tools.dns.util

import top.tonydon.tools.dns.constant.OSEnum
import top.tonydon.tools.dns.domain.PingResult
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.InetAddress
import java.nio.charset.Charset
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.round

object PingUtils {
    fun testDelay(ip: String?): Int {
        return try {
            val address = InetAddress.getByName(ip)
            for (i in 0..99) {
                if (address.isReachable(i)) {
                    return i
                }
            }
            -1
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private val os = OSUtils.getSystem()


    fun ping(ip: String, count: Int = 4): PingResult {
        // 根据操作系统设置ping命令
        var pingCmd = ""
        if(os == OSEnum.Windows){
            pingCmd = "ping $ip -n $count"
        }else if(os == OSEnum.Linux || os == OSEnum.MacOS){
            pingCmd = "ping $ip -c $count"
        }

        val sb: StringBuilder = StringBuilder()
        val pro = Runtime.getRuntime().exec(pingCmd)
        val buf = BufferedReader(
            InputStreamReader(
                pro.inputStream,
                Charset.forName("GBK")
            )
        )
        while (true) {
            val line = buf.readLine()
            if (line != null) {
                sb.append(line).append('\n')
            } else {
                break
            }
        }

        // 正则表达式进行解析结果
        val pattern: Pattern = Pattern.compile("(\\d+ms)(\\s+)(TTL=\\d+)", Pattern.CASE_INSENSITIVE);
        val matcher: Matcher = pattern.matcher(sb.toString())

        // 将结果解析为 PingResult
        val result = PingResult()
        result.totalCount = count
        var delay = 0.0
        while (matcher.find()) {
            result.successCount += 1
            delay += matcher.group(1).replace("ms", "").toDouble()
        }
        // 如果没有一次成功过，则设置为999ms
        if (result.successCount == 0){
            result.meanDelay = 999
        }else{
            result.meanDelay = round(delay / result.successCount).toInt()
        }
        return result
    }
}
