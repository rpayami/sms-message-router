package com.sinch.demo.smsMessageRouter.message.controller;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SendMessageRequest {

    @JsonProperty("destination_number")
    private String destinationNumber;

    private String content;
    
    private String format;

    public SendMessageRequest() {}

    public SendMessageRequest(String destinationNumber, String content, String format) {
        this.destinationNumber = destinationNumber;
        this.content = content;
        this.format = format;
    }

    public String getDestinationNumber() {
        return destinationNumber;
    }

    public void setDestinationNumber(String destination_number) {
        this.destinationNumber = destination_number;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
} 