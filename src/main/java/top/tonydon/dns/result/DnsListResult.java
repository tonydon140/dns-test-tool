package top.tonydon.dns.result;

import java.util.List;

public class DnsListResult {
    private Integer code;
    private String msg;
    private List<Dns> data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<Dns> getData() {
        return data;
    }

    public void setData(List<Dns> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "DnsListResult{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
