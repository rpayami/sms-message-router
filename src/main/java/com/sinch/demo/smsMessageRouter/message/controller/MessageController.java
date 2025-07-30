package com.sinch.demo.smsMessageRouter.message.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sinch.demo.smsMessageRouter.message.model.MessageFactory;
import com.sinch.demo.smsMessageRouter.message.model.MessageStatus;
import com.sinch.demo.smsMessageRouter.message.model.MessageType;
import com.sinch.demo.smsMessageRouter.message.model.SMSMessage;
import com.sinch.demo.smsMessageRouter.message.service.MessageService;
import com.sinch.demo.smsMessageRouter.utils.InvalidPhoneNumberException;

@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    private SMSMessage convertToSMSMessage(SendMessageRequest request, MessageStatus status) {
        if (!request.getFormat().equalsIgnoreCase(MessageType.SMS.toString())) {
            throw new IllegalArgumentException("Invalid message format: " + request.getFormat());
        }
        return (SMSMessage)MessageFactory.getMessage(null, request.getDestinationNumber(), request.getContent(), 
            MessageType.SMS, status);
    }    

    @PostMapping
    public ResponseEntity<?> send(@RequestBody SendMessageRequest request) {
        try {
            boolean isOptedOut = messageService.isOptedOut(request.getDestinationNumber());
            SMSMessage message = convertToSMSMessage(request, isOptedOut ? MessageStatus.BLOCKED : MessageStatus.PENDING);
            SMSMessage savedMessage = messageService.routeMessage(message);
            if (isOptedOut) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot send message to this phone number as it is opted out");
            }
            SendMessageResponse response = new SendMessageResponse(savedMessage.getId(), savedMessage.getStatus().toString());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (InvalidPhoneNumberException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid phone number");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage());
        }
    }    

    @GetMapping("/{id}")
    public ResponseEntity<String> getMessageStatusById(@PathVariable Long id) {
        Optional<SMSMessage> message = messageService.getMessageById(id);
        if (message.isPresent()) {
            return ResponseEntity.ok(message.get().getStatus().toString());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    //For testing purposes, get all messages
    @GetMapping
    public ResponseEntity<List<SMSMessage>> getAllMessages() {
        List<SMSMessage> messages = messageService.getAll();
        return ResponseEntity.ok(messages);
    }

    // Webhook for carrier delivered messages
    @PostMapping("/{id}/delivered ")
    public ResponseEntity<String> delivered(@PathVariable Long id) {
    Optional<SMSMessage> message = messageService.getMessageById(id);
    if (message.isPresent()) {
        messageService.updateDeliveredStatus(message.get());
        return ResponseEntity.ok("Message status updated");
    } else {
        return ResponseEntity.notFound().build();
    }
    }
     
  
} 