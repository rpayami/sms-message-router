package com.sinch.demo.smsMessageRouter.message.model;

import org.springframework.data.relational.core.mapping.Table;

@Table("SMSMessage")
public class SMSMessage extends Message {
    
    private String content;

    public SMSMessage() {
    }

    public SMSMessage(Long id, String destinationNumber, String content, MessageStatus status, Carrier carrier) {
        super(id, destinationNumber, MessageType.SMS, status, carrier);
        this.content = content;
    }

    public SMSMessage(String destinationNumber, String content, MessageStatus status) {
        super(null, destinationNumber, MessageType.SMS, status, null);
        this.content = content;
    }

    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    @Override
    public String toString() {
        return super.toString() + "Content: " + content;
    }
}
