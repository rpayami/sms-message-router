package com.sinch.demo.smsMessageRouter.message.model;

public class MessageFactory {

    public static Message getMessage(Long id, String destinationNumber, String content, MessageType messageType, MessageStatus status) {
        switch (messageType) {
            case SMS:
                return new SMSMessage(id, destinationNumber, content, status, null);
            default:
                throw new IllegalArgumentException("Unsupported message type: " + messageType);
        }
    }
}
