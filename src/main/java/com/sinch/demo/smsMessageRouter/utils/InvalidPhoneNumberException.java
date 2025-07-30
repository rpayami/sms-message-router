package com.sinch.demo.smsMessageRouter.utils;

/**
 * Exception thrown when a phone number format is invalid.
 */
public class InvalidPhoneNumberException extends RuntimeException {
    
    public InvalidPhoneNumberException(String message) {
        super(message);
    }
    
    public InvalidPhoneNumberException(String message, Throwable cause) {
        super(message, cause);
    }
} 