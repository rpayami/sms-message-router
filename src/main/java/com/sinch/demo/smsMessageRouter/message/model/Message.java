package com.sinch.demo.smsMessageRouter.message.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;

public abstract class Message {
    
    @Id
    private Long id;
    
    private String destinationNumber;
        
    private LocalDateTime createdAt;

    private MessageStatus status;

    private MessageType type;

    private Carrier carrier;

    public Message() {
    }

    public Message(Long id, String destinationNumber, MessageType type, MessageStatus status, Carrier carrier) {
        this.id = id;
        this.destinationNumber = destinationNumber;
        this.createdAt = LocalDateTime.now();
        this.type = type;
        this.status = status;   
        this.carrier = carrier;
    }
    
    public Long getId() {
        return id;
    }
    
    public String getDestinationNumber() {
        return destinationNumber;
    }
    
    public void setDestinationNumber(String destinationNumber) {
        this.destinationNumber = destinationNumber;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public Carrier getCarrier() {
        return carrier;
    }

    public void setCarrier(Carrier carrier) {
        this.carrier = carrier;
    }

    @Override
    public String toString() {
        return "SMSMessage{" +
                "id=" + id +
                ", destinationNumber='" + destinationNumber + '\'' +
                ", createdAt=" + createdAt +
                ", status=" + status +
                '}';
    }
}
