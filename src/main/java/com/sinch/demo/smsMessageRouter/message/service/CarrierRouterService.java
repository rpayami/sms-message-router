package com.sinch.demo.smsMessageRouter.message.service;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sinch.demo.smsMessageRouter.message.model.Carrier;
import com.sinch.demo.smsMessageRouter.message.model.Message;
import com.sinch.demo.smsMessageRouter.utils.PhoneNumberUtility;

@Service
public class CarrierRouterService {

    @Autowired
    private PhoneNumberUtility phoneNumberUtility;

    private Carrier resolveAustralianCarrier(String phoneNumber) {
        // We can potentially do an external service call here
        // to try to resolve Australian carrier based on the phone number

        // For now, we randomly alternate between Optus and Telstra
        return new Random().nextInt(2) == 0 ? Carrier.OPTUS : Carrier.TELSTRA;
    }

    public Carrier resolveCarrier(Message message) {
        String phoneNumber = phoneNumberUtility.validateAndNormalize(message.getDestinationNumber());
        String countryCode = phoneNumberUtility.getCountryCode(phoneNumber);
        return switch (countryCode) {
            case "AU" -> resolveAustralianCarrier(phoneNumber);
            case "NZ" -> Carrier.SPARK;
            default -> Carrier.GLOBAL;
        };
    }

}
