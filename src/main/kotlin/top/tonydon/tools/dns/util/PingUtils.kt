package top.tonydon.tools.dns.util

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


    fun ping(ip: String, count: Int = 4): PingResult {
        val sb: StringBuilder = StringBuilder()
        val pro = Runtime.getRuntime().exec("ping $ip -n $count")
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
