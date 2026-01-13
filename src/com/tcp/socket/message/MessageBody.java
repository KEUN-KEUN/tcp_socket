package com.tcp.socket.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON 메시지의 "body" 영역 매핑 클래스.
 * 센서값, 상태, 알람 등 공통 필드 정의.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageBody {

    @JsonProperty("temperature")
    private Double temperature;

    @JsonProperty("speed")
    private Integer speed;

    @JsonProperty("status")
    private String status;

    // 필요한 경우 알람용 필드 추가
    @JsonProperty("alarmCode")
    private String alarmCode;

    @JsonProperty("message")
    private String message;

    @JsonProperty("severity")
    private String severity;

    // 기본 생성자
    public MessageBody() {}

    // getter / setter
    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getSpeed() {
        return speed;
    }

    public void setSpeed(Integer speed) {
        this.speed = speed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAlarmCode() {
        return alarmCode;
    }

    public void setAlarmCode(String alarmCode) {
        this.alarmCode = alarmCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
}
