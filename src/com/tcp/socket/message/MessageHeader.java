package com.tcp.socket.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageHeader {

    @JsonProperty("type")
    private String type;

    @JsonProperty("equipmentId")
    private String equipmentId;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("command")
    private String command; // 추가: 요청 로직 식별용

    public MessageHeader() {}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(String equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public ZonedDateTime getParsedTimestamp() {
        return ZonedDateTime.parse(timestamp);
    }

    @Override
    public String toString() {
        return "MessageHeader{" +
                "type='" + type + '\'' +
                ", equipmentId='" + equipmentId + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", command='" + command + '\'' +
                '}';
    }
}
