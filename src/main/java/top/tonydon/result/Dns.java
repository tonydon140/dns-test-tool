package top.tonydon.result;


import java.time.LocalDateTime;

public class Dns {
    private Integer id;
    private String primaryIp;
    private String assistantIp;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String primaryDelay;
    private String assistantDelay;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPrimaryIp() {
        return primaryIp;
    }

    public void setPrimaryIp(String primaryIp) {
        this.primaryIp = primaryIp;
    }

    public String getAssistantIp() {
        return assistantIp;
    }

    public void setAssistantIp(String assistantIp) {
        this.assistantIp = assistantIp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrimaryDelay() {
        return primaryDelay;
    }

    public void setPrimaryDelay(String primaryDelay) {
        this.primaryDelay = primaryDelay;
    }

    public String getAssistantDelay() {
        return assistantDelay;
    }

    public void setAssistantDelay(String assistantDelay) {
        this.assistantDelay = assistantDelay;
    }

    @Override
    public String toString() {
        return "Dns{" +
                "id=" + id +
                ", primaryIp='" + primaryIp + '\'' +
                ", assistantIp='" + assistantIp + '\'' +
                ", description='" + description + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", primaryDelay='" + primaryDelay + '\'' +
                ", assistantDelay='" + assistantDelay + '\'' +
                '}';
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
