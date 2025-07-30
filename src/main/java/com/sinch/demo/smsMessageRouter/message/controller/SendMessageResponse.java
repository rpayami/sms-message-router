package com.sinch.demo.smsMessageRouter.message.controller;

public class SendMessageResponse {
    Long messageId;
    String status;

    public SendMessageResponse(Long messageId, String status, String description) {
        this.messageId = messageId;
        this.status = status;
    }

    public SendMessageResponse(Long messageId, String status) {
        this(messageId, status, "");
    }

    public Long getMessageId() {
        return messageId;
    }

    public String getStatus() {
        return status;
    }

}
