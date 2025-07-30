package com.sinch.demo.smsMessageRouter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinch.demo.smsMessageRouter.message.controller.SendMessageRequest;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class ApplicationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;


    //    Send to valid AU number → routes to Telstra/Optus
    @Test
    public void testAUEndToEndMessageFlow() throws Exception {
        // Setup MockMvc
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Test data
        String auNumber = "+61412345678";
        String messageContent = "Hello from SMS Router!";
        
        // Step 1: Create message request
        SendMessageRequest request = new SendMessageRequest(auNumber, messageContent, "SMS");

        // Step 2: Send message via POST
        String response = mockMvc.perform(post("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract message ID from response
        String messageId = objectMapper.readTree(response).get("messageId").asText();
        String status = objectMapper.readTree(response).get("status").asText();

        mockMvc.perform(get("/messages/" + messageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("PENDING"));

      // Step 4: Get all messages and verify the pending message is included 
      // with the correct and carrier
      mockMvc.perform(get("/messages"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$[?(@.id == " + messageId + ")]").exists())
      .andExpect(jsonPath("$[?(@.id == " + messageId + ")].status").value("PENDING"))
      .andExpect(jsonPath("$[?(@.id == " + messageId + ")].carrier",
      Matchers.anyOf(
        Matchers.contains("TELSTRA"),
        Matchers.contains("OPTUS")
    )));      
    }

    // Send to opted-out number → blocked (check via status endpoint)
    @Test
    public void testEndToEndOptedOutMessageFlow() throws Exception {
        // Setup MockMvc
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Test data
        String optedOutNumber = "+61487654321";
        String messageContent = "This should be blocked";
        
        // Step 1: First opt out the phone number
        mockMvc.perform(post("/optout/" + optedOutNumber))
                .andExpect(status().isCreated());
        
        // Step 2: Create message request
        SendMessageRequest request = new SendMessageRequest(optedOutNumber, messageContent, "SMS");

        // Step 3: Send message via POST - should return FORBIDDEN for opted-out number
        mockMvc.perform(post("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Cannot send message to this phone number as it is opted out"));

        // Step 4: Get all messages and verify the blocked message is included
        mockMvc.perform(get("/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.destinationNumber == '" + optedOutNumber + "')]").exists())
                .andExpect(jsonPath("$[?(@.destinationNumber == '" + optedOutNumber + "')].status").value("BLOCKED"));
    }

    // Send to NZ number → routes to Spark
    @Test
    public void testNZEndToEndMessageFlow() throws Exception {
        // Setup MockMvc
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Test data
        String nzNumber = "+64211234567";
        String messageContent = "Hello from SMS Router!";
        
        // Step 1: Create message request
        SendMessageRequest request = new SendMessageRequest(nzNumber, messageContent, "SMS");

        // Step 2: Send message via POST
        String response = mockMvc.perform(post("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract message ID from response
        String messageId = objectMapper.readTree(response).get("messageId").asText();
        String status = objectMapper.readTree(response).get("status").asText();

        // Step 3: Check message status via GET
        mockMvc.perform(get("/messages/" + messageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("PENDING"));

      // Step 4: Get all messages and verify the blocked message is included
      
      mockMvc.perform(get("/messages"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$[?(@.id == " + messageId + ")]").exists())
      .andExpect(jsonPath("$[?(@.id == " + messageId + ")].status").value(status))
      .andExpect(jsonPath("$[?(@.id == " + messageId + ")].carrier").value("SPARK"));

    }

    // Send to US number → routes to Global carrier
    @Test
    public void testUSEndToEndMessageFlow() throws Exception {
        // Setup MockMvc
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Test data
        String usNumber = "+14158438453"; // US phone number
        String messageContent = "Hello from SMS Router!";
        
        // Step 1: Create message request
        SendMessageRequest request = new SendMessageRequest(usNumber, messageContent, "SMS");

        // Step 2: Send message via POST
        String response = mockMvc.perform(post("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract message ID from response
        String messageId = objectMapper.readTree(response).get("messageId").asText();
        String status = objectMapper.readTree(response).get("status").asText();

        // Step 3: Check message status via GET
        mockMvc.perform(get("/messages/" + messageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("PENDING"));

        // Step 4: Get all messages and verify the message is included with GLOBAL carrier
        mockMvc.perform(get("/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.id == " + messageId + ")]").exists())
                .andExpect(jsonPath("$[?(@.id == " + messageId + ")].status").value(status))
                .andExpect(jsonPath("$[?(@.id == " + messageId + ")].carrier").value("GLOBAL"));
    }

    @Test
    public void testEndToEndMessageNotFound() throws Exception {
        // Setup MockMvc
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Try to get status of non-existent message
        mockMvc.perform(get("/messages/999999"))
                .andExpect(status().isNotFound());
    }

    // Send to invalid phone number → BAD_REQUEST
    @Test
    public void testEndToEndInvalidPhoneNumberFlow() throws Exception {
        // Setup MockMvc
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Test data
        String invalidNumber = "12345"; // Invalid phone number format
        String messageContent = "This should fail";
        
        // Step 1: Create message request
        SendMessageRequest request = new SendMessageRequest(invalidNumber, messageContent, "SMS");

        // Step 2: Send message via POST - should return BAD_REQUEST for invalid phone number
        mockMvc.perform(post("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid phone number"));
    }
} 