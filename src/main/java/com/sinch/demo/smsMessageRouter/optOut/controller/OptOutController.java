package com.sinch.demo.smsMessageRouter.optOut.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sinch.demo.smsMessageRouter.optOut.model.OptOut;
import com.sinch.demo.smsMessageRouter.optOut.service.OptOutService;
import com.sinch.demo.smsMessageRouter.utils.InvalidPhoneNumberException;

@RestController
@RequestMapping("/optout")
public class OptOutController {

    @Autowired
    private OptOutService optOutService;

    @PostMapping("/{phoneNumber}")
    public ResponseEntity<?> optOut(@PathVariable String phoneNumber) {
        try {
            Optional<OptOut> result = optOutService.getOptOut(phoneNumber);
            if (result.isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Phone number already opted out");
            }
            result = optOutService.optOut(phoneNumber);
            if (result.isPresent()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(result.get());
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Was not opted out successfully");
            }
        } catch (InvalidPhoneNumberException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid phone number");
        }
    }

    @DeleteMapping("/{phoneNumber}")
    public ResponseEntity<?> removeOptOut(@PathVariable String phoneNumber) {
        try {
            Optional<Long> result = optOutService.removeOptOut(phoneNumber);
            if (result.isPresent()) {
                return ResponseEntity.status(HttpStatus.OK).body(result.get());
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Phone number cannot be opted in as it is not opted out");
            }
        }
        catch (InvalidPhoneNumberException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid phone number");
        }    
    }
}
