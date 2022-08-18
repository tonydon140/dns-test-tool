package top.tonydon.dns.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.charset.Charset;

public class PingUtils {
    public static int testDelay(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            for (int i = 0; i < 100; i++) {
                if (address.isReachable(i)) {
                    return i;
                }
            }
            return -1;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String ping(String ip) {
        return ping(ip, 4);
    }

    public static String ping(String ip, int count) {
        try {
            String line;
            StringBuilder sb = new StringBuilder();

            Process pro = Runtime.getRuntime().exec("ping " + ip + " -n " + count);
            BufferedReader buf = new BufferedReader(new InputStreamReader(pro.getInputStream(),
                    Charset.forName("GBK")));

            while ((line = buf.readLine()) != null)
                sb.append(line).append("\n");

            String[] split = sb.toString().split("平均 = ");
            if (split.length == 1)
                return "请求超时！";
            return split[1].trim();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
