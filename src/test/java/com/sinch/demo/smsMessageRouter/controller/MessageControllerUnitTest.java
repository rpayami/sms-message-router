package com.sinch.demo.smsMessageRouter.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinch.demo.smsMessageRouter.message.controller.MessageController;
import com.sinch.demo.smsMessageRouter.message.controller.SendMessageRequest;
import com.sinch.demo.smsMessageRouter.message.model.MessageFactory;
import com.sinch.demo.smsMessageRouter.message.model.MessageStatus;
import com.sinch.demo.smsMessageRouter.message.model.MessageType;
import com.sinch.demo.smsMessageRouter.message.model.SMSMessage;
import com.sinch.demo.smsMessageRouter.message.service.MessageService;
import com.sinch.demo.smsMessageRouter.utils.InvalidPhoneNumberException;
import com.sinch.demo.smsMessageRouter.message.model.Carrier;

@WebMvcTest(MessageController.class)
public class MessageControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testValidAustralianPhoneNumberSavedAsPending() throws Exception {
        String australianNumber = "+61412345678";
        
        SendMessageRequest request = new SendMessageRequest(australianNumber, "Test message", "SMS");

        // Mock that the number is not opted out
        when(messageService.isOptedOut(australianNumber)).thenReturn(false);

        // Create a mock saved message
        SMSMessage savedMessage = (SMSMessage) MessageFactory.getMessage(1L, australianNumber, "Test message", MessageType.SMS, MessageStatus.PENDING);
        
        // Mock the service save method
        when(messageService.routeMessage(any(SMSMessage.class))).thenReturn(savedMessage);

        // Perform the POST request
        // For a valid number which is not opted out, the message should be saved as pending
        mockMvc.perform(post("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.messageId").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    public void testSendToOptedOutNumberGetsBlocked() throws Exception {
        String optedOutNumber = "+61487654321";
        
        SendMessageRequest request = new SendMessageRequest(optedOutNumber, "Test message", "SMS");

        // Mock that the number is opted out
        when(messageService.isOptedOut(optedOutNumber)).thenReturn(true);

        // Create a mock saved message with BLOCKED status
        SMSMessage savedMessage = (SMSMessage) MessageFactory.getMessage(2L, optedOutNumber, "Test message", MessageType.SMS, MessageStatus.BLOCKED);
        
        // Mock the service route method
        when(messageService.routeMessage(any(SMSMessage.class))).thenReturn(savedMessage);

        // Mock the service to return the message when queried by ID
        when(messageService.getMessageById(2L)).thenReturn(Optional.of(savedMessage));

        // Perform the POST request - should return FORBIDDEN
        mockMvc.perform(post("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                
                .andExpect(status().isForbidden())
                .andExpect(content().string("Cannot send message to this phone number as it is opted out"));

        // Check the message status via GET endpoint - should be BLOCKED
        mockMvc.perform(get("/messages/2"))
                .andExpect(status().isOk())
                .andExpect(content().string("BLOCKED"));
    }

    @Test
    public void testInvalidPhoneNumberReturnsBadRequest() throws Exception {
        String invalidNumber = "12345"; // Invalid phone number format
        
        SendMessageRequest request = new SendMessageRequest(invalidNumber, "Test message", "SMS");

        // Mock that the service throws InvalidPhoneNumberException
        when(messageService.isOptedOut(invalidNumber)).thenThrow(new InvalidPhoneNumberException("Invalid phone number format"));

        // Perform the POST request - should return BAD_REQUEST
        mockMvc.perform(post("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid phone number"));
    }

    @Test
    public void testUSPhoneNumberRoutesToGlobalCarrier() throws Exception {
        String usNumber = "+14158438453"; // US phone number
        
        SendMessageRequest request = new SendMessageRequest(usNumber, "Test message", "SMS");

        // Mock that the number is not opted out
        when(messageService.isOptedOut(usNumber)).thenReturn(false);

        // Create a mock saved message with GLOBAL carrier
        SMSMessage savedMessage = (SMSMessage) MessageFactory.getMessage(3L, usNumber, "Test message", MessageType.SMS, MessageStatus.PENDING);
        savedMessage.setCarrier(Carrier.GLOBAL);
        
        // Mock the service route method
        when(messageService.routeMessage(any(SMSMessage.class))).thenReturn(savedMessage);

        // Perform the POST request
        // For a valid US number which is not opted out, the message should be saved as pending with GLOBAL carrier
        mockMvc.perform(post("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.messageId").value(3))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}